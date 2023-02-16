package com.flok22.android.agicent.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.LayoutTimesOutBinding
import com.flok22.android.agicent.model.permanentChat.PermanentChatModel
import com.flok22.android.agicent.model.permanentChat.PermanentChatResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.MyGestureDetector
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.TapListener
import com.flok22.android.agicent.utils.showToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TimeOverDialog(/*private val senderName: String?, private val chatId: Int?*/) :
    DialogFragment() {

    private val className = TimeOverDialog::class.java.simpleName
    private lateinit var binding: LayoutTimesOutBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var senderName: String? = null
    private var chatId: Int? = null


    companion object {
        private const val SENDER_NAME = "name"
        private const val CHAT_ID = "-1"
        var isAcceptOrReject = false
        private var tapListener: TapListener? = null

        fun newInstance(
            senderName: String?, chatId: Int?, tap: TapListener
        ): TimeOverDialog {
            val timeOverDialog = TimeOverDialog()
            val bundle = Bundle().apply {
                putString(SENDER_NAME, senderName)
                putInt(CHAT_ID, chatId!!)
            }
            tapListener = tap
            timeOverDialog.arguments = bundle
            return timeOverDialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = View.inflate(context, R.layout.layout_times_out, null)
        binding = LayoutTimesOutBinding.bind(view)
        dialog.setContentView(binding.root)
        prefHelper = SharedPreferenceManager(requireContext())

        //get info from args
        senderName = arguments?.getString(SENDER_NAME)
        chatId = arguments?.getInt(CHAT_ID)


        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setGravity(Gravity.TOP)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        view.setOnTouchListener(object : MyGestureDetector(requireContext()) {
            override fun onSwipeUp() {
                super.onSwipeUp()
                dismiss()
            }
        })

        //ui changes
        binding.keepConnectedMsg.text = getString(R.string.keep_connected_ques, senderName)
        val permanentChatModel = PermanentChatModel(chatId.toString())

        binding.acceptPermanentRequest.setOnClickListener {
            val call = RetrofitBuilder.apiService.acceptPermanentRequest(
                prefHelper.authKey, permanentChatModel
            )
            call?.enqueue(object : Callback<PermanentChatResponse?> {
                override fun onResponse(
                    call: Call<PermanentChatResponse?>,
                    response: Response<PermanentChatResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            response.body()?.let { it1 -> context?.showToast(it1.message) }
                            isAcceptOrReject = true
                            tapListener?.onTapped()
                            dismiss()
                        }
                        500 -> {
                            response.body()?.message?.let { context?.showToast(it) }
                        }
                    }
                }

                override fun onFailure(call: Call<PermanentChatResponse?>, t: Throwable) {
                    Log.e(TAG, "$className acceptPermanentRequest onFailure: $t")
                }
            })
        }

        binding.rejectPermanentRequest.setOnClickListener {
            val call = RetrofitBuilder.apiService.rejectPermanentRequest(
                prefHelper.authKey, permanentChatModel
            )
            call?.enqueue(object : Callback<PermanentChatResponse?> {
                override fun onResponse(
                    call: Call<PermanentChatResponse?>,
                    response: Response<PermanentChatResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            response.body()?.let { it1 -> context?.showToast(it1.message) }
                            isAcceptOrReject = true
                            dismiss()
                        }
                        500 -> {
                            response.body()?.message?.let { context?.showToast(it) }
                        }
                    }
                }

                override fun onFailure(call: Call<PermanentChatResponse?>, t: Throwable) {
                    Log.e(TAG, "$className acceptPermanentRequest onFailure: $t")
                }
            })
        }

        return dialog
    }
}