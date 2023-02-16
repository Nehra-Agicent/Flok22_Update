package com.flok22.android.agicent.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.ActivityConnectionRequestBinding
import com.flok22.android.agicent.model.acceptRequest.Data
import com.flok22.android.agicent.model.acceptRequest.RequestModel
import com.flok22.android.agicent.model.acceptRequest.RequestResponse
import com.flok22.android.agicent.model.cancelRequest.CancelRequestModel
import com.flok22.android.agicent.model.cancelRequest.CancelRequestResponse
import com.flok22.android.agicent.model.connectionRequest.ConnectionRequestModel
import com.flok22.android.agicent.model.connectionRequest.ConnectionRequestResponse
import com.flok22.android.agicent.model.rejectRequest.RejectModel
import com.flok22.android.agicent.model.rejectRequest.RejectResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.aws.S3Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConnectionRequestActivity : AppCompatActivity() {
    private val className = ConnectionRequestActivity::class.java.simpleName

    private lateinit var binding: ActivityConnectionRequestBinding
    private var placeName: String? = null
    private var profilePic: String? = null
    private var userName: String? = null
    private var bio: String? = null
    private var dob: String? = null
    private var startDateTime: String? = null
    private var userId: Int? = null
    private var placeId: Int? = null
    private var isPrivate: Int? = null
    private var isBusy: Int? = null
    private var isBusyWithMe: Int? = null
    private var chatId: Int? = null
    private var chatType: Int? = null
    private var isRequestSent: Int? = null
    private var isRequestReceived: Int? = null
    private lateinit var prefHelper: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val extras = intent.extras
        if (extras != null) {
            placeId = extras.getInt("placeId")
            userId = extras.getInt("userId")
            placeName = extras.getString("placeName")
            userName = extras.getString("userName")
            profilePic = extras.getString("profilePic")
            bio = extras.getString("bio")
            dob = extras.getString("dob")
            startDateTime = extras.getString("startDateTime")
            isPrivate = extras.getInt("isPrivate")
            isBusy = extras.getInt("isBusy")
            isBusyWithMe = extras.getInt("isBusyWithMe")
            chatId = extras.getInt("ChatId")
            chatType = extras.getInt("chatType")
            isRequestSent = extras.getInt("isRequestSent")
            isRequestReceived = extras.getInt("isRequestReceived")

        }
        prefHelper = SharedPreferenceManager(this)

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.connectingButton.hide()
        binding.connectionButton.show()
        binding.handleRequestContainer.hide()
        /**
         * Load data
         */
        binding.placeNameConnect.text = placeName
        binding.userNameConnect.text = userName
        when (isPrivate) {
            0 -> {
                binding.age.show()
                binding.bio.show()
                binding.bioUser.text = bio
                binding.userAge.text = getString(R.string.ageInYears, getDOB(dob))
            }
            1 -> {
                binding.age.invisible()
                binding.bio.invisible()
                binding.bioUser.invisible()
                binding.userAge.invisible()
            }
        }

        Glide.with(this@ConnectionRequestActivity)
            .load(S3Utils.generateS3ShareUrl(applicationContext, profilePic)).centerCrop()
            .into(binding.userIconConnect)

        //send connection request
        binding.connectionButton.setOnClickListener {
            val connectionRequestModel =
                ConnectionRequestModel(placeId!!, userId!!)
            if (prefHelper.placeId == placeId) {
                sendConnectionRequest(prefHelper.authKey, connectionRequestModel)
            } else if(!prefHelper.isCheckedIn){
                sendConnectionRequest(prefHelper.authKey, connectionRequestModel)
            }
            else {
                showToast("Must be checked into same place to send request")
            }
        }

        if (isRequestSent == 1) {
            //show connecting button and reject
            binding.connectionButton.hide()
            if (isBusyWithMe == 1) {
                binding.connectingButton.hide()
                binding.cancelRequest.invisible()
                binding.startChat.show()
                binding.startChat.setOnClickListener {
                    val intent =
                        Intent(this@ConnectionRequestActivity, MessageScreen::class.java)
                    intent.putExtra("senderName", userName)
                    intent.putExtra("profilePic", profilePic)
                    intent.putExtra("createdDatetime", startDateTime)
                    intent.putExtra("chatId", chatId)
                    intent.putExtra("otherUserId", userId)
                    intent.putExtra("chatType", chatType)
                    startActivity(intent)
                }
            } else {
                binding.connectingButton.show()
                binding.cancelRequest.show()
                binding.cancelRequest.setOnClickListener {
                    cancelConnectionRequest(userId!!)
                }
            }
        } else if (isRequestReceived == 1) {
            // cancel request or accept
            binding.buttonContainer.invisible()
            binding.cancelRequest.invisible()

            if (isBusyWithMe == 1) {
                binding.handleRequestContainer.hide()
                binding.startChat.show()
                binding.startChat.setOnClickListener {
                    val intent =
                        Intent(this@ConnectionRequestActivity, MessageScreen::class.java)
                    intent.putExtra("senderName", userName)
                    intent.putExtra("profilePic", profilePic)
                    intent.putExtra("createdDatetime", startDateTime)
                    intent.putExtra("chatId", chatId)
                    intent.putExtra("otherUserId", userId)
                    intent.putExtra("chatType", chatType)
                    startActivity(intent)
                }
            } else {
                binding.startChat.hide()

                binding.handleRequestContainer.show()
                binding.acceptRequest.setOnClickListener {
                    acceptConnectionRequest(userId!!)
                }
                binding.rejectRequest.setOnClickListener {
                    rejectRequest(prefHelper.authKey, RejectModel(userId!!.toString()))
                }
            }
        }
    }

    private fun rejectRequest(authKey: String, rejectModel: RejectModel) {
        val call = RetrofitBuilder.apiService.rejectRequest(authKey, rejectModel)
        call?.enqueue(object : Callback<RejectResponse?> {
            override fun onResponse(
                call: Call<RejectResponse?>,
                response: Response<RejectResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        binding.handleRequestContainer.hide()
                        binding.buttonContainer.show()
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<RejectResponse?>, t: Throwable) {
                Log.e(TAG, "$className rejectRequest() onFailure: ")
            }
        })
    }

    private fun acceptConnectionRequest(userId: Int) {
        val call =
            RetrofitBuilder.apiService.acceptRequest(
                auth_key = prefHelper.authKey,
                requestModel = RequestModel(userId, placeId!!)
            )
        call?.enqueue(object : Callback<RequestResponse?> {
            override fun onResponse(
                call: Call<RequestResponse?>,
                response: Response<RequestResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        binding.handleRequestContainer.hide()
                        binding.startChat.show()
                        var acceptResponse: Data? = null
                        if (response.body()?.data != null) {
                            acceptResponse = response.body()?.data
                        }
                        binding.startChat.setOnClickListener {
                            val intent =
                                Intent(this@ConnectionRequestActivity, MessageScreen::class.java)
                            intent.putExtra("senderName", acceptResponse?.full_name)
                            intent.putExtra("profilePic", acceptResponse?.profile_pic)
                            intent.putExtra("createdDatetime", acceptResponse?.start_datetime)
                            intent.putExtra("chatId", acceptResponse?.chat_id)
                            intent.putExtra("otherUserId", userId)
                            intent.putExtra("chatType", acceptResponse?.chat_type)
                            startActivity(intent)
                        }
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                    else -> {
                        showToast("Cannot connect now send request again to connect")
                    }
                }
            }

            override fun onFailure(call: Call<RequestResponse?>, t: Throwable) {
                Log.e(TAG, "$className acceptRequest() onFailure: ")
            }
        })
    }

    private fun sendConnectionRequest(authKey: String, model: ConnectionRequestModel) {
        if (isNetworkAvailable()) {
            val call =
                RetrofitBuilder.apiService.sendConnectionRequest(authKey, model)
            call?.enqueue(object : Callback<ConnectionRequestResponse?> {
                override fun onResponse(
                    call: Call<ConnectionRequestResponse?>,
                    response: Response<ConnectionRequestResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            showToast(response.body()!!.message)
                            binding.connectingButton.show()
                            binding.connectionButton.hide()
                            binding.cancelRequest.show()
                            binding.cancelRequest.setOnClickListener {
                                cancelConnectionRequest(model.request_to)
                            }
                        }
                        405 -> {
                            response.body()?.let { showToast(it.message) }
                        }
                        500 -> {
                            response.body()?.message?.let { showToast(it) }
                        }
                    }
                }

                override fun onFailure(call: Call<ConnectionRequestResponse?>, t: Throwable) {
                    Log.i(TAG, "$className sendConnectionRequest() onFailure: $t")
                }
            })
        } else showNetworkToast()
    }

    private fun cancelConnectionRequest(requestTo: Int) {
        val model = CancelRequestModel(requestTo.toString(), placeId.toString())
        val call = RetrofitBuilder.apiService.cancelConnectionRequest(prefHelper.authKey, model)
        call?.enqueue(object : Callback<CancelRequestResponse?> {
            override fun onResponse(
                call: Call<CancelRequestResponse?>,
                response: Response<CancelRequestResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        binding.connectingButton.hide()
                        binding.connectionButton.show()
                        binding.cancelRequest.invisible()
                        showToast("Request Canceled")
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CancelRequestResponse?>, t: Throwable) {
                Log.d(TAG, "$className cancelConnectionRequest onFailure:$t")
            }
        })
    }
}