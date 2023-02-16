package com.flok22.android.agicent.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.LayoutConectionRequestBinding
import com.flok22.android.agicent.model.acceptRequest.RequestModel
import com.flok22.android.agicent.model.acceptRequest.RequestResponse
import com.flok22.android.agicent.model.blockUser.BlockUserModel
import com.flok22.android.agicent.model.blockUser.BlockUserResponse
import com.flok22.android.agicent.model.pendingRequest.Data
import com.flok22.android.agicent.model.rejectRequest.RejectModel
import com.flok22.android.agicent.model.rejectRequest.RejectResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.ui.Connected
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.MyGestureDetector
import com.flok22.android.agicent.utils.aws.S3Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConnectionRequestDialog(
    val userList: Data?,
    private val tapListener: TapListener
) : DialogFragment() {

    private val className = ConnectionRequestDialog::class.java.simpleName
    private lateinit var binding: LayoutConectionRequestBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var userName: String? = null
    private var dob: String? = null
    private var profilePic: String? = null
    private var placeId: Int? = -1
    private var userId: Int? = -1

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = View.inflate(context, R.layout.layout_conection_request, null)
        binding = LayoutConectionRequestBinding.bind(view)
        dialog.setContentView(binding.root)

        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setGravity(Gravity.TOP)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        prefHelper = SharedPreferenceManager(requireContext())
        view.setOnTouchListener(object : MyGestureDetector(requireContext()) {
            override fun onSwipeUp() {
                super.onSwipeUp()
                dismiss()
            }
        })

        /**
         * setting data to UI
         */
        userName = userList?.full_name ?: arguments?.getString("fullName")
        dob = userList?.dob ?: arguments?.getString("dob")
        profilePic = userList?.profile_pic ?: arguments?.getString("pic")
        placeId = userList?.place_id ?: arguments?.getInt("placeId")
        userId = userList?.request_by ?: arguments?.getInt("userId")

        binding.connectionRequest.text =
            getString(R.string.request_user_name, prefHelper.name)

        binding.requestedUsername.text = getString(R.string.requested_user_name, userName)

        Glide.with(requireContext())
            .load(S3Utils.generateS3ShareUrl(requireContext(), profilePic)).centerCrop()
            .into(binding.userIconConnect)
        binding.userNameConnect.text = userName
        binding.userAge.text = getDOB(dob)

        binding.optionBu.setOnClickListener {
            showOptionMenu(it)
        }

        binding.acceptConnectionRequest.setOnClickListener {
            val requestModel = RequestModel(userId!!, placeId!!)
            acceptRequest(prefHelper.authKey, requestModel)
        }

        binding.rejectConnectionRequest.setOnClickListener {
            val rejectModel = RejectModel(userId.toString())
            rejectRequest(rejectModel)
        }

        return dialog
    }

    @SuppressLint("RestrictedApi")
    private fun showOptionMenu(view: View) {
        val menuBuilder = MenuBuilder(context)
        MenuInflater(requireContext()).inflate(R.menu.connection_request_menu, menuBuilder)
        val optionsMenu = MenuPopupHelper(requireContext(), menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.report -> {
                        requireContext().showToast("User is reported")
                    }
                    R.id.block -> {
                        blockUser(BlockUserModel(userId.toString()))
                    }
                }
                return true
            }

            override fun onMenuModeChange(menu: MenuBuilder) {
            }
        })
        optionsMenu.show()
    }

    private fun blockUser(blockUserModel: BlockUserModel) {
        val call =
            RetrofitBuilder.apiService.blockUser(prefHelper.authKey, blockUserModel)
        call?.enqueue(object : Callback<BlockUserResponse?> {
            override fun onResponse(
                call: Call<BlockUserResponse?>,
                response: Response<BlockUserResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        response.body()?.message?.let { requireContext().showToast(it) }
                        tapListener.onTapped()
                        dismiss()
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<BlockUserResponse?>, t: Throwable) {
                Log.e(TAG, "$className blockUnblockUser onFailure: ")
            }
        })
    }

    private fun rejectRequest(rejectModel: RejectModel) {
        val call = RetrofitBuilder.apiService.rejectRequest(prefHelper.authKey, rejectModel)
        call?.enqueue(object : Callback<RejectResponse?> {
            override fun onResponse(
                call: Call<RejectResponse?>,
                response: Response<RejectResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        response.body()?.let { requireContext().showToast(it.message) }
                        tapListener.onTapped()
                        dismiss()
                    }
                    405 -> {
                        tapListener.onTapped()
                        dismiss()
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<RejectResponse?>, t: Throwable) {
                Log.e(TAG, "$className rejectRequest() onFailure: ")
            }
        })
    }

    private fun acceptRequest(authKey: String, requestModel: RequestModel) {
        val call = RetrofitBuilder.apiService.acceptRequest(authKey, requestModel)
        call?.enqueue(object : Callback<RequestResponse?> {
            override fun onResponse(
                call: Call<RequestResponse?>,
                response: Response<RequestResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        val info = response.body()?.data
                        val intent = Intent(requireActivity(), Connected::class.java)
                        intent.putExtra("userName", info?.full_name)
                        intent.putExtra("profilePic", info?.profile_pic)
                        intent.putExtra("createdDatetime", info?.start_datetime)
                        intent.putExtra("chatId", info?.chat_id)
                        intent.putExtra("chatType", info?.chat_type)
                        startActivity(intent)
                        tapListener.onTapped()
//                        requireContext().showToast("Request Accepted, Swipe Refresh Message screen to view user chat")
                        dismiss()

                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                    else -> {
                        tapListener.onTapped()
                        dismiss()
                        requireContext().showToast("Cannot connect now send request again to connect")
                    }
                }
            }

            override fun onFailure(call: Call<RequestResponse?>, t: Throwable) {
                Log.e(TAG, "$className acceptRequest() onFailure: ")
            }
        })
    }

}