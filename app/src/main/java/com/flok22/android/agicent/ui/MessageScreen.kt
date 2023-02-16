package com.flok22.android.agicent.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Log.d
import android.view.LayoutInflater
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.adapter.MessageAdapter
import com.flok22.android.agicent.databinding.ActivityMessageScreenBinding
import com.flok22.android.agicent.fragment.TimeOverDialog
import com.flok22.android.agicent.model.allChatMsg.AllChatMessageModel
import com.flok22.android.agicent.model.allChatMsg.AllChatMessageResponse
import com.flok22.android.agicent.model.allChatMsg.GetChatMessage
import com.flok22.android.agicent.model.blockUser.BlockUserModel
import com.flok22.android.agicent.model.blockUser.BlockUserResponse
import com.flok22.android.agicent.model.chatInfo.ChatInfoModel
import com.flok22.android.agicent.model.chatInfo.ChatInfoResponse
import com.flok22.android.agicent.model.disconnect.DisconnectModel
import com.flok22.android.agicent.model.disconnect.DisconnectResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.aws.S3Utils
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.EmojiTextView
import com.vanniktech.emoji.google.GoogleEmojiProvider
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Runnable
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.TimeUnit

class MessageScreen : AppCompatActivity(), TapListener {

    private val className = MessageScreen::class.java.simpleName
    private lateinit var binding: ActivityMessageScreenBinding
    private var mSocket: Socket? = null
    private var senderName: String? = null
    private var profilePic: String? = null
    private var createdDatetime: String? = null
    private var chatId: Int? = null
    private var chatType: Int? = null
    private var otherUserId: Int? = null
    private var chatAcceptedByMe: Int? = null
    private var chatAcceptedByOther: Int? = null
    private var messageCount: Int? = null
    private var isBlocked = false
    private var isDisconnected = false
    private var msgList = ArrayList<GetChatMessage>()
    private var adapter: MessageAdapter? = null
    private lateinit var prefHelper: SharedPreferenceManager
    private var timer: CountDownTimer? = null
    private var chatAcceptedByUser: Boolean = false

    companion object {
        var messageCounter = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper = SharedPreferenceManager(this)
        EmojiManager.install(GoogleEmojiProvider())

        val extras = intent.extras
        if (extras != null) {
            senderName = extras.getString("senderName")
            profilePic = extras.getString("profilePic")
            chatId = extras.getInt("chatId")
            otherUserId = extras.getInt("otherUserId")
            messageCount = extras.getInt("messageCount")
        }

        //uI update
        binding.chatterName.text = senderName
        Glide.with(this).load(
            S3Utils.generateS3ShareUrl(applicationContext, profilePic)
        ).into(binding.iconChat)

        adapter = MessageAdapter(this, msgList)
        binding.chatScreen.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.chatScreen.adapter = adapter

        val emojiPopup = EmojiPopup(binding.rootView, binding.messageEditText)
        binding.emojiButton.setOnClickListener {
            emojiPopup.toggle()
        }

        binding.messageEditText.setOnClickListener {
            if (emojiPopup.isShowing) {
                emojiPopup.dismiss()
            }
        }


        //implementing socket
        try {
            mSocket = IO.socket(Constants.SOCKET_URL)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

        mSocket?.connect()
        socketEvents()

        chatId?.let { getChatInfo(it) }
        getUserChatMessageApi(chatId!!)
        binding.messageEditText.addTextChangedListener(messageTextWatcher)

        // buttons action
        binding.sendAction.setOnClickListener { view ->
            if (binding.messageEditText.text.toString().isNotEmpty()) {
                val emojiTV = LayoutInflater.from(view.context)
                    .inflate(
                        R.layout.emoji_textview_layout, binding.chatScreen, false
                    ) as EmojiTextView
                emojiTV.text = binding.messageEditText.text.toString()
                if (chatType == 3) {
                    if (messageCounter <= 21) {
                        messageCounter++
                        binding.remTime.text =
                            getString(R.string.message_counter, messageCounter.toString())
                        sentMessageEvent(binding.messageEditText.text.toString().trim())
                    } else {
                        showToast("Message limit exceeds")
                        binding.messageEditText.isEnabled = false
                    }
                } else {
                    binding.messageEditText.isEnabled = true
                    sentMessageEvent(emojiTV.text.toString().trim())
                }
            }
            binding.messageEditText.text.clear()
        }

        binding.backButton.setOnClickListener {
            if (isBlocked || isDisconnected) {
                val intent = Intent()
                intent.putExtra("isBlocked", true)
                setResult(Activity.RESULT_OK, intent)
            }
            onBackPressedDispatcher.onBackPressed()
        }

        binding.profileMenu.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)
            val menu = popupMenu.menu
            popupMenu.menuInflater.inflate(R.menu.chat_menu, menu)

            if (isBlocked) {
                menu.getItem(2).title = getString(R.string.unblock)
            } else {
                menu.getItem(2).title = getString(R.string.block)
            }
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.view_profile -> {
                        val intent = Intent(this@MessageScreen, UserProfile::class.java)
                        intent.putExtra("otherUserId", otherUserId)
                        startActivity(intent)
                        true
                    }
                    R.id.disconnect -> {
                        if (isNetworkAvailable())
                            disconnectChat()
                        else showNetworkToast()
                        true
                    }
                    R.id.block -> {
                        if (isNetworkAvailable())
                            blockUser(BlockUserModel(otherUserId.toString()))
                        else showNetworkToast()
                        true
                    }
                    R.id.report -> {
                        showToast("Profile Reported")
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

//        startTimer(getRemainingTime(createdDatetime))
    }

    private fun getChatInfo(chatId: Int) {
        val call = RetrofitBuilder.apiService.getChatInfo(prefHelper.authKey, ChatInfoModel(chatId))
        if (isNetworkAvailable()) {
            call?.enqueue(object : Callback<ChatInfoResponse?> {
                override fun onResponse(
                    call: Call<ChatInfoResponse?>,
                    response: Response<ChatInfoResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            val chatInfo = response.body()?.data
                            chatType = chatInfo?.chat_type
                            createdDatetime = chatInfo?.created_datetime
                            chatAcceptedByMe = chatInfo?.is_first_user_accept_permanant_chat
                            chatAcceptedByOther = chatInfo?.is_second_user_accept_permanant_chat
                            chatAcceptedByUser =
                                if (prefHelper.userId.toInt() == chatInfo?.first_user_id) {
                                    (chatInfo.is_first_user_accept_permanant_chat == 1 || chatInfo.is_first_user_accept_permanant_chat == 2)
                                } else if (prefHelper.userId.toInt() == chatInfo?.second_user_id) {
                                    (chatInfo.is_second_user_accept_permanant_chat == 1 || chatInfo.is_second_user_accept_permanant_chat == 2)
                                } else {
                                    false
                                }
                            messageCount =
                                if (prefHelper.userId.toInt() == chatInfo?.first_user_id) {
                                    chatInfo.first_user_message_count
                                } else chatInfo?.second_user_message_count
                            Log.i(TAG, "$className onResponse: timer")
                            messageCounter = messageCount!!
                            chatInfo?.chat_type?.let { handleTimerView(it) }
                        }
                        500 -> {
                            showToast("Data base error")
                        }
                    }
                }

                override fun onFailure(call: Call<ChatInfoResponse?>, t: Throwable) {
                    Log.e(TAG, "$className getChatInfo() onFailure: $t")
                }
            })
        } else showNetworkToast()
    }

    private fun handleTimerView(chatType: Int) {
        when (chatType) {
            0 -> {
                binding.messageEditText.isEnabled = false
                binding.clockIcon.invisible()
                showToast("Send request to connect again.")
            }
            1 -> {
                binding.remTime.show()
                binding.clockIcon.show()
                startTimer(getRemainingTime(createdDatetime))
            }
            2 -> {
                binding.remTime.hide()
                binding.clockIcon.invisible()
                binding.messageEditText.isEnabled = true
            }
            3 -> {
                binding.clockIcon.invisible()
                binding.remTime.show()
                binding.remTime.text =
                    getString(R.string.message_counter, messageCounter.toString())
            }
        }
    }

    private val disconnectListener = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            val isConnected: Int?
            try {
                isConnected = data.getInt("is_connected")
            } catch (ex: JSONException) {
                return@Runnable
            }
            val connected: Boolean?
            connected = isConnected != 2
            enableDisableKeyboard(connected)
        })
    }

    private val blockListener = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            val isBlocked: Int?
            try {
                isBlocked = data.getInt("is_blocked")
            } catch (ex: JSONException) {
                return@Runnable
            }
            val unblocked: Boolean?
            unblocked = isBlocked != 1
            enableDisableKeyboard(unblocked)
        })
    }

    private val requestListener = Emitter.Listener { args ->
        runOnUiThread(Runnable {
            val data = args[0] as JSONObject
            val requestStatus: Int?
            val statusBy: Int?
            try {
                requestStatus = data.getInt("request_status")
                statusBy = data.getInt("request_status_by")
            } catch (ex: JSONException) {
                return@Runnable
            }
            if (requestStatus == 1 && otherUserId == statusBy) {
                chatAcceptedByMe = 1
            } else chatAcceptedByOther = 1
            if (chatAcceptedByMe == 1 && chatAcceptedByOther == 1) {
                enableDisableKeyboard(true)
            }
        })
    }

    private fun enableDisableKeyboard(value: Boolean) {
        binding.clockIcon.hide()
        binding.remTime.hide()
        binding.messageEditText.isEnabled = value
    }

    private fun disconnectChat() {
        val call = RetrofitBuilder.apiService.disconnect(
            prefHelper.authKey, DisconnectModel(chatId.toString())
        )
        call?.enqueue(object : Callback<DisconnectResponse?> {
            override fun onResponse(
                call: Call<DisconnectResponse?>,
                response: Response<DisconnectResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        isDisconnected = true
                        response.body()?.message?.let { showToast(it) }
                        val sendJsonObject = JSONObject()
                        sendJsonObject.put("chat_id", chatId)
                        sendJsonObject.put("connection_status_by", otherUserId)
                        sendJsonObject.put("is_connected", 2)
                        mSocket?.emit("chat_connection_status", sendJsonObject)
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<DisconnectResponse?>, t: Throwable) {
                Log.e(TAG, "$className disconnect() onFailure: $t")
            }
        })
    }

    private fun blockUser(blockUserModel: BlockUserModel) {
        if (!isDisconnected) disconnectChat()
        val call =
            RetrofitBuilder.apiService.blockUser(prefHelper.authKey, blockUserModel)
        call?.enqueue(object : Callback<BlockUserResponse?> {
            override fun onResponse(
                call: Call<BlockUserResponse?>,
                response: Response<BlockUserResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        isBlocked = true
                        response.body()?.message?.let { showToast(it) }
                        val blockJsonObject = JSONObject()
                        blockJsonObject.put("chat_id", chatId)
                        blockJsonObject.put("blocked_status_by", otherUserId)
                        blockJsonObject.put("is_blocked", 1)
                        mSocket?.emit("user_blocked_status", blockJsonObject)
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<BlockUserResponse?>, t: Throwable) {
                Log.e(TAG, "$className blockUnblockUser onFailure: ")
            }
        })
    }

    private fun socketEvents() {
        // when connect error
        mSocket?.on(Socket.EVENT_CONNECT_ERROR) {
            runOnUiThread {
                d("Socket", "EVENT_CONNECT_ERROR: ${it[0]}")
                //context?.showToast("Socket Connection Error :  ${it[0]}")
            }
        }

        // when disconnecting
        mSocket?.on(Socket.EVENT_DISCONNECT) {
            runOnUiThread {
                d("Socket", "Socket Disconnected")
            }
        }

        // when connecting
        mSocket?.on(Socket.EVENT_CONNECT) {
            runOnUiThread {
                joinSocketEvent(chatId!!)
            }
        }

        mSocket?.on("chat_connection_status", disconnectListener)

        mSocket?.on("user_blocked_status", blockListener)

        mSocket?.on("permanent_chat_request_status", requestListener)

        mSocket?.on("receive_message") { args ->
            if (args[0] != null) {
                val data = args[0] as JSONObject

                val getChatMessage = GetChatMessage(
                    chatId!!,
                    data["messaged_at"] as String,
                    data["message"] as String,
                    0,
                    data["receiver_id"].toString().toInt(),
                    data["sender_id"].toString().toInt()
                )

                d("chatDetail", "$getChatMessage")
                runOnUiThread {
                    msgList.add(0, getChatMessage)
                    binding.chatScreen.scrollToPosition(/*messageAdapter.itemCount-1*/ 0)
                    adapter!!.notifyItemInserted(0)
                }
            }
        }
    }

    private fun sentMessageEvent(message: String) {
        val sendJsonObject = JSONObject()
        sendJsonObject.put("chat_id", chatId)
        sendJsonObject.put("sender_id", prefHelper.userId.toInt())
        sendJsonObject.put("receiver_id", otherUserId)
        sendJsonObject.put("message", message)
        mSocket?.emit("send_message", sendJsonObject)
    }

    private fun joinSocketEvent(chatId: Int) {
        val joinJsonObject = JSONObject()
        joinJsonObject.put("chat_id", chatId)
        mSocket?.emit("join", joinJsonObject)
    }

    /*private fun scrollOnClickKeyboard() {
        binding.messageRecycler.viewTreeObserver.addOnGlobalLayoutListener{
            try {
                val r = Rect()
                binding.messageRecycler.getWindowVisibleDisplayFrame(r)
                val screenHeight: Int = binding.messageRecycler.rootView.height
                val keypadHeight = screenHeight - r.bottom
                if (keypadHeight > screenHeight * 0.15) {
                    if (!scrollingToBottom) {
                        scrollingToBottom = true
                        binding.messageRecycler.scrollToPosition(0)
                    }
                } else {
                    scrollingToBottom = false
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }*/

    private fun getUserChatMessageApi(chatId: Int) {

        val call = RetrofitBuilder.apiService.getAllChatMessages(
            prefHelper.authKey,
            AllChatMessageModel(chatId.toString())
        )

        /*if (i == 0) {
            binding.progressBar.isVisible = true
            binding.chatProgressBar.isVisible = false
        } else {
            binding.chatProgressBar.isVisible = true
            binding.progressBar.isVisible = false
        }*/

        if (isNetworkAvailable()) {
            call?.enqueue(object : Callback<AllChatMessageResponse?> {
                override fun onResponse(
                    call: Call<AllChatMessageResponse?>,
                    response: Response<AllChatMessageResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            response.body()?.let { chatResponse ->
                                chatResponse.data.let { list ->
                                    list.reverse()
                                    msgList.addAll(list)
                                    d("msgList", "${list.map { it.chat_id }}")
                                    adapter?.notifyDataSetChanged()
                                }
                            }
                        }
                        500 -> {
                            response.body()?.message?.let { showToast(it) }
                        }
                    }
                }

                override fun onFailure(call: Call<AllChatMessageResponse?>, t: Throwable) {
                    d(TAG, "onFailure: $t")
                }
            })
        } else showNetworkToast()
    }

    private val messageTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, count: Int) {
            binding.sendAction.isEnabled = p0.toString().trim().isNotEmpty()
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private fun startTimer(remainingTime: Long) {
        binding.clockIcon.show()
        val remTime = 22 * 60000 - remainingTime // chat for 22 minutes
        val dialogVisibilityTime = 27 * 60000 - remainingTime
        if (remTime < -1) {
            binding.remTime.invisible()
            binding.clockIcon.invisible()
            binding.messageEditText.isEnabled = false
            if (!chatAcceptedByUser) {
                if (dialogVisibilityTime > 0) {
                    TimeOverDialog.newInstance(senderName, chatId, this)
                        .show(supportFragmentManager, "TimeOverDialog")
                } else {
                    showToast("Time over to connect permanently")
                }
            }
//            TimeOverDialog(senderName, chatId).show(supportFragmentManager, "")
        } else {
            timer = object : CountDownTimer(remTime, 1000) {
                override fun onTick(p0: Long) {
                    val text = String.format(
                        Locale.getDefault(), "%02d:%02ds",
                        TimeUnit.MILLISECONDS.toMinutes(p0) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(p0) % 60
                    )
                    binding.remTime.text = text
                }

                override fun onFinish() {
                    val dialog = TimeOverDialog.newInstance(senderName, chatId, this@MessageScreen)
                    dialog.show(supportFragmentManager, "TimeOverDialog")
                    binding.messageEditText.isEnabled = false
                    binding.remTime.invisible()
                    binding.clockIcon.invisible()
                }

            }.start()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "$className onResume:")
        chatType?.let { handleTimerView(it) }
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
        mSocket?.off()
    }

    override fun onTapped() {
        val sendJsonObject = JSONObject()
        sendJsonObject.put("chat_id", chatId)
        sendJsonObject.put("request_status_by", otherUserId)
        sendJsonObject.put("request_status", 1)
        mSocket?.emit("permanent_chat_request_status", sendJsonObject)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isBlocked || isDisconnected) {
            val intent = Intent()
            intent.putExtra("isBlocked", true)
            setResult(Activity.RESULT_OK, intent)
        }
    }
}