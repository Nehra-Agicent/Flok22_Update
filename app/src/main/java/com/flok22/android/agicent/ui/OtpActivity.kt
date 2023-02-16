package com.flok22.android.agicent.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.flok22.android.agicent.MainActivity
import com.flok22.android.agicent.databinding.ActivityOtpBinding
import com.flok22.android.agicent.model.otp.OtpModel
import com.flok22.android.agicent.model.otp.OtpResult
import com.flok22.android.agicent.model.signUp.UpdateDeviceTokenResponse
import com.flok22.android.agicent.model.signUp.UpdateTokenModel
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.github.ybq.android.spinkit.SpinKitView
import com.poovam.pinedittextfield.PinField
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpActivity : AppCompatActivity() {

    private val className = OtpActivity::class.java.simpleName
    private lateinit var binding: ActivityOtpBinding
    private var countryCode: String? = null
    private var phoneNumber: String? = null
    private var otp: String? = null
    private lateinit var prefHelper: SharedPreferenceManager
    private var spinKitView: SpinKitView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper = SharedPreferenceManager(this)
        Log.d(TAG, "$className onCreate: ${DeviceTokenPref(this).deviceToken}")
        binding.otpField.onTextCompleteListener = object : PinField.OnTextCompleteListener {
            override fun onTextComplete(enteredText: String): Boolean {
                return true
            }
        }
        val extras = intent.extras
        if (extras != null) {
            countryCode = extras.getString("countryCode")
            phoneNumber = extras.getString("phoneNumber")
            otp = extras.getString("otp")
        }
        spinKitView = binding.spinKit

        binding.otpField.setText(otp)
        binding.loginBu.setOnClickListener {
            if (isNetworkAvailable()) {
                val otpText = (binding.otpField.text).toString()
                if (isOtpEntered(otpText, application)) {
                    val offset = getTimeOffset()
                    prefHelper.timeOffset = offset
                    val otpModel = OtpModel(countryCode!!, phoneNumber!!, otpText, offset)
                    verifyOtp(otpModel)
                }
            } else {
                showNetworkToast()
            }
        }
        binding.resendText.setOnClickListener {
            showToast("OTP sent")
        }
    }

    private fun verifyOtp(otpModel: OtpModel) {
        spinKitView?.visibility = View.VISIBLE
        if (spinKitView!!.isVisible) {
            binding.loginBu.isEnabled = false
        }
        val call = RetrofitBuilder.apiService.verifyOtp(otpModel)
        call?.enqueue(object : Callback<OtpResult?> {
            override fun onResponse(call: Call<OtpResult?>, response: Response<OtpResult?>) {
                val otpResult: OtpResult? = response.body()
                when (response.code()) {
                    200 -> {
                        val authKey = otpResult!!.data.auth_Key
                        prefHelper.authKey = authKey
                        Log.d(TAG, "$className verifyOtp() onResponse: $authKey")
                        val userData = response.body()?.data
                        prefHelper.isPrivate = userData?.is_private.toString()
                        prefHelper.profilePic = userData!!.profile_pic
                        prefHelper.userId = userData.user_id
                        prefHelper.name = userData.full_name
                        prefHelper.dob = userData.dob
                        prefHelper.gender = userData.gender
                        prefHelper.bio = userData.bio
                        updateDeviceToken(
                            authKey,
                            UpdateTokenModel(DeviceTokenPref(this@OtpActivity).deviceToken, "A")
                        )
                        prefHelper.isFromOtpScreen = true
                    }
                    201 -> {
                        spinKitView?.visibility = View.GONE
                        if (!spinKitView!!.isVisible) {
                            binding.loginBu.isEnabled = true
                        }
                        val intent = Intent(this@OtpActivity, SignUpActivity::class.java)
                        intent.putExtra("countryCode", countryCode)
                        intent.putExtra("phoneNumber", phoneNumber)
                        startActivity(intent)
                    }
                    400 -> {
                        spinKitView?.visibility = View.GONE
                        if (!spinKitView!!.isVisible) {
                            binding.loginBu.isEnabled = true
                        }
                        otpResult?.message?.let { showToast(it) }
                    }
                    500 -> {
                        spinKitView?.visibility = View.GONE
                        if (!spinKitView!!.isVisible) {
                            binding.loginBu.isEnabled = true
                        }
                        otpResult?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<OtpResult?>, t: Throwable) {
                Log.i(TAG, "$className verifyOtp() onFailure: $t")
                spinKitView?.visibility = View.GONE
                if (!spinKitView!!.isVisible) {
                    binding.loginBu.isEnabled = true
                }
            }
        })
    }

    private fun updateDeviceToken(authKey: String, updateTokenModel: UpdateTokenModel) {
        val call = RetrofitBuilder.apiService.updateDeviceToken(authKey, updateTokenModel)
        call?.enqueue(object : Callback<UpdateDeviceTokenResponse?> {
            override fun onResponse(
                call: Call<UpdateDeviceTokenResponse?>,
                response: Response<UpdateDeviceTokenResponse?>
            ) {
                if (response.isSuccessful) {
                    spinKitView?.visibility = View.GONE
                    Log.i(TAG, "$className updateDeviceToken() ${response.isSuccessful}")
                    startActivity(Intent(this@OtpActivity, MainActivity::class.java))
                    finishAffinity()
                }
            }

            override fun onFailure(call: Call<UpdateDeviceTokenResponse?>, t: Throwable) {
                Log.d(TAG, "$className updateDeviceToken onFailure: $t")
            }
        })
    }
}