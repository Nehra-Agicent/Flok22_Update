package com.flok22.android.agicent.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.ActivityLoginBinding
import com.flok22.android.agicent.model.aws.AwsCredentialResponse
import com.flok22.android.agicent.model.login.LogInModel
import com.flok22.android.agicent.model.login.OtpResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.DeviceTokenPref
import com.flok22.android.agicent.utils.isNetworkAvailable
import com.flok22.android.agicent.utils.showNetworkToast
import com.flok22.android.agicent.utils.showToast
import com.github.ybq.android.spinkit.SpinKitView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val className = LoginActivity::class.java.simpleName
    private lateinit var binding: ActivityLoginBinding
    private var spinKitView: SpinKitView? = null
    private lateinit var tokenPref: DeviceTokenPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tokenPref = DeviceTokenPref(this)

        val ccp = binding.ccp
        spinKitView = binding.spinKit
        ccp.registerCarrierNumberEditText(binding.mobileNumber)
        binding.loginBu.setOnClickListener {
            if (isNetworkAvailable()) {
                if (ccp.isValidFullNumber) {
                    val countryCode = binding.ccp.selectedCountryCodeWithPlus
                    val phoneNumber = binding.mobileNumber.text.toString()

                    val logInModel = LogInModel(countryCode, phoneNumber)

                    Log.d(TAG, "$className onCreate: $countryCode & $phoneNumber")
                    Log.i(
                        TAG,
                        "onCreate: tokenPref.accessKey.isEmpty(): ${tokenPref.accessKey.isEmpty()}"
                    )
                    if (tokenPref.accessKey.isEmpty()) {
                        getAwsCredentials()
                    }
                    sendOtp(logInModel)
                } else {
                    showToast(getString(R.string.valid_phone_number))
                }
            } else {
                showNetworkToast()
            }
        }
    }

    private fun getAwsCredentials() {
        val call = RetrofitBuilder.apiService.getAwsCredentials()
        call?.enqueue(object : Callback<AwsCredentialResponse?> {
            override fun onResponse(
                call: Call<AwsCredentialResponse?>,
                response: Response<AwsCredentialResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        val data = response.body()?.data
                        if (data != null) {
                            tokenPref.bucketName = data.aws_bucket_name
                            tokenPref.accessKey = data.aws_access_key
                            tokenPref.secretKey = data.aws_secret_key
                            Log.i(TAG, "$className getAwsCredentials onResponse: $data")
                        }
                    }
                    else -> {
                        showToast("Data not received")
                    }
                }
            }

            override fun onFailure(call: Call<AwsCredentialResponse?>, t: Throwable) {
                Log.e(TAG, "$className getAwsCredentials() onFailure: $t")
            }
        })
    }

    private fun sendOtp(logInModel: LogInModel) {
        spinKitView?.visibility = View.VISIBLE
        if (spinKitView!!.isVisible) {
            binding.loginBu.isEnabled = false
        }
        val call = RetrofitBuilder.apiService.sendOtp(logInModel)
        call?.enqueue(object : Callback<OtpResponse?> {
            var otpResponse: OtpResponse? = null
            override fun onResponse(call: Call<OtpResponse?>, response: Response<OtpResponse?>) {
                when (response.code()) {
                    200 -> {
                        spinKitView?.visibility = View.GONE
                        if (!spinKitView!!.isVisible) {
                            binding.loginBu.isEnabled = true
                        }
                        otpResponse = response.body()
                        val otp = otpResponse?.OTP?.OTP
                        Log.i(TAG, "$className sendOtp: $otp")
                        val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                        intent.putExtra("countryCode", logInModel.countryCode)
                        intent.putExtra("phoneNumber", logInModel.phoneNumber)
                        intent.putExtra("otp", otp)
                        startActivity(intent)
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<OtpResponse?>, t: Throwable) {
                spinKitView?.visibility = View.GONE
                if (!spinKitView!!.isVisible) {
                    binding.loginBu.isEnabled = true
                }
                Log.i(TAG, "$className onFailure: $t")
            }
        })
    }
}