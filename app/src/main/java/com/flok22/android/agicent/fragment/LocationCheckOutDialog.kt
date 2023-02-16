package com.flok22.android.agicent.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import com.flok22.android.agicent.databinding.CheckoutCustomDialogBinding
import com.flok22.android.agicent.model.checkOut.CheckOutModel
import com.flok22.android.agicent.model.checkOut.CheckOutResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.Constants
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.TapListener
import com.flok22.android.agicent.utils.showToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationCheckOutDialog : DialogFragment() {

    private lateinit var prefHelper: SharedPreferenceManager
    private var _binding: CheckoutCustomDialogBinding? = null
    private val binding get() = _binding!!
    var isDialogVisible = false
    internal lateinit var tapListener: TapListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext()/*, R.style.Theme_Dialog_Rounded*/)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.apply {
                setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.CENTER_HORIZONTAL)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, b: Bundle?): View {
        _binding = CheckoutCustomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefHelper = SharedPreferenceManager(requireContext())
        binding.placeName.text = prefHelper.placeName
        binding.checkOutButton.setOnClickListener {
            checkOutUser()
        }
    }

    private fun checkOutUser() {
        val call = RetrofitBuilder.apiService.checkOutUser(
            prefHelper.authKey,
            CheckOutModel(prefHelper.placeId)
        )
        call?.enqueue(object : Callback<CheckOutResponse?> {
            override fun onResponse(
                call: Call<CheckOutResponse?>,
                response: Response<CheckOutResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        requireContext().showToast("You are checked out from the place")
                        prefHelper.placeId = -1
                        prefHelper.placeLat = ""
                        prefHelper.placeLng = ""
                        prefHelper.placeName = ""
                        prefHelper.isCheckedIn = false
                        tapListener.onTapped()
                        dismiss()
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                    else -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CheckOutResponse?>, t: Throwable) {
                Log.e(Constants.TAG, "$TAG checkOutUser() onFailure: $t")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        isDialogVisible = true
    }

    override fun onPause() {
        super.onPause()
        isDialogVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LocationCheckOutDialog"

        @JvmStatic
        fun newInstance(tap: TapListener) =
            LocationCheckOutDialog().apply {
                tapListener = tap
                isCancelable = false
            }
    }

}