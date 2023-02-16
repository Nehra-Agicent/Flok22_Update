package com.flok22.android.agicent.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.flok22.android.agicent.adapter.ChatAdapter
import com.flok22.android.agicent.databinding.FragmentMessageBinding
import com.flok22.android.agicent.model.chat.AllChatsModel
import com.flok22.android.agicent.model.chat.AllChatsResponse
import com.flok22.android.agicent.model.chat.Data
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.ui.MessageScreen
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageFragment : Fragment(), TapListener {

    private val className = MessageFragment::class.java.simpleName

    private lateinit var binding: FragmentMessageBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var chatData: List<Data>? = null
    private var position = -1
    private var adapter: ChatAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefHelper = SharedPreferenceManager(requireContext())
        chatData = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAllChats(prefHelper.authKey, AllChatsModel(0))
        binding.refreshMessages.setOnRefreshListener {
            binding.refreshMessages.isRefreshing = false

            if (requireContext().isNetworkAvailable()) {
                binding.chatView.show()
                getAllChats(prefHelper.authKey, AllChatsModel(0))
            } else {
                binding.noConnectionView.noConnectionLayout.show()
                binding.chatView.invisible()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(requestReceiver, IntentFilter("outOfPlaceRadius"))
        if (TimeOverDialog.isAcceptOrReject) {
            getAllChats(prefHelper.authKey, AllChatsModel(0))
            TimeOverDialog.isAcceptOrReject = false
        }
    }

    private fun getAllChats(authKey: String, allChatsModel: AllChatsModel) {
        if (requireContext().isNetworkAvailable()) {
            binding.noConnectionView.noConnectionLayout.hide()
            binding.spinKit.visibility = View.VISIBLE
            val call = RetrofitBuilder.apiService.getAllChats(authKey, allChatsModel)
            call?.enqueue(object : Callback<AllChatsResponse?> {
                override fun onResponse(
                    call: Call<AllChatsResponse?>,
                    response: Response<AllChatsResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            (chatData as ArrayList<Data>).clear()
                            binding.spinKit.visibility = View.GONE
                            chatData = response.body()!!.data
                            if (chatData!!.isEmpty()) {
                                binding.startChatMessage.show()
                            } else {
                                binding.startChatMessage.hide()
                                adapter =
                                    ChatAdapter(chatData!!, context) { pos, chatList, src ->
                                        onItemClick(pos, chatList, src)
                                    }
                                binding.messageRecyclerView.layoutManager =
                                    LinearLayoutManager(requireContext())
                                binding.messageRecyclerView.adapter = adapter
                            }
                        }
                        404 -> {
                            binding.spinKit.visibility = View.GONE
                            try {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                val errorInString = jObjError.getString("message")
                                requireContext().showToast(errorInString)
                            } catch (e: Exception) {
                                e.message?.let { requireContext().showToast(it) }
                            }
                        }
                        500 -> {
                            binding.spinKit.visibility = View.GONE
                            response.body()?.message?.let { context?.showToast(it) }
                        }
                    }
                }

                override fun onFailure(call: Call<AllChatsResponse?>, t: Throwable) {
                    binding.spinKit.visibility = View.GONE
                    context?.showToast("Failed To Connect to API")
                    Log.e(TAG, "$className getAllChats() onFailure: $t")
                }
            })
        } else binding.noConnectionView.noConnectionLayout.show()
    }

    private val startForBlockResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val isBLocked = result.data?.getBooleanExtra("isBlocked", false)
            if (result.resultCode == Activity.RESULT_OK) {
                getAllChats(prefHelper.authKey, AllChatsModel(0))
            }
        }

    private fun onItemClick(pos: Int, chatList: Data, src: String) {
        when (src) {
            "root" -> {
                position = pos
                val intent = Intent(requireActivity(), MessageScreen::class.java)
                intent.putExtra("senderName", chatList.user_name)
                intent.putExtra("profilePic", chatList.profile_pic)
                intent.putExtra("chatId", chatList.chat_id)
                intent.putExtra("chatType", chatList.chat_type)
                intent.putExtra("otherUserId", chatList.other_user_id)
                intent.putExtra("createdDatetime", chatList.created_datetime)
                intent.putExtra("chatAcceptedByMe", chatList.is_permanent_chat_accepted_by_me)
                intent.putExtra("chatAcceptedByOther", chatList.is_permanent_chat_accepted_by_other)
                intent.putExtra("messageCount", chatList.message_count)

                startForBlockResult.launch(intent)
            }
        }
    }

    private val requestReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val dialog = LocationCheckOutDialog.newInstance(this@MessageFragment)
            if (!dialog.isDialogVisible)
                dialog.show(childFragmentManager, LocationCheckOutDialog.TAG)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(requestReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(requestReceiver)
    }

    override fun onTapped() {}

}