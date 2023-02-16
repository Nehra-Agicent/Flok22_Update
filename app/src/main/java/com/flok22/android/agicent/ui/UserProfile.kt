package com.flok22.android.agicent.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.flok22.android.agicent.databinding.ActivityUserProfileBinding
import com.flok22.android.agicent.model.userInfo.UserInfoModel
import com.flok22.android.agicent.model.userInfo.UserInfoResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.aws.S3Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfile : AppCompatActivity() {

    private val className = UserProfile::class.java.simpleName
    private lateinit var binding: ActivityUserProfileBinding
    private var otherUserId: Int? = null
    private lateinit var prefHelper: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper = SharedPreferenceManager(this)

        val extras = intent.extras
        if (extras != null) {
            otherUserId = extras.getInt("otherUserId")
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        val call = RetrofitBuilder.apiService.getCheckedInUserInfo(
            prefHelper.authKey,
            UserInfoModel(otherUserId.toString())
        )
        call?.enqueue(object : Callback<UserInfoResponse?> {
            override fun onResponse(
                call: Call<UserInfoResponse?>,
                response: Response<UserInfoResponse?>
            ) {
                val data = response.body()?.data?.get(0)

                when (response.code()) {
                    200 -> {
                        Glide.with(this@UserProfile).load(
                            S3Utils.generateS3ShareUrl(applicationContext, data?.profile_pic)
                        ).centerCrop().into(binding.userImage)
                        binding.userName.text = data?.full_name
                        binding.bioUser.text = data?.bio
                        binding.userAge.text = getDOB(data?.dob)
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<UserInfoResponse?>, t: Throwable) {
                Log.e(TAG, "$className getUserInfo onFailure: $t")
            }
        })
    }
}