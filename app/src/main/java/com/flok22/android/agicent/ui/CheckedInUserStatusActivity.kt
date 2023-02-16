package com.flok22.android.agicent.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.flok22.android.agicent.R
import com.flok22.android.agicent.adapter.CheckedInUserAdapter
import com.flok22.android.agicent.databinding.ActivityCheckedInUserStatusBinding
import com.flok22.android.agicent.model.checkIn.CheckInUser
import com.flok22.android.agicent.model.checkIn.PlaceDetailResponse
import com.flok22.android.agicent.model.checkIn.PlaceIdModel
import com.flok22.android.agicent.model.checkOut.CheckOutModel
import com.flok22.android.agicent.model.checkOut.CheckOutResponse
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailModel
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckedInUserStatusActivity : AppCompatActivity() {

    private val className = CheckedInUserStatusActivity::class.java.simpleName

    private lateinit var binding: ActivityCheckedInUserStatusBinding
    private var placeId: Int? = null
    private lateinit var prefHelper: SharedPreferenceManager
    private lateinit var adapter: CheckedInUserAdapter
    private var list = ArrayList<CheckInUser>()
    private var placeName = ""

    companion object {
        var isActivityVisible = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckedInUserStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper = SharedPreferenceManager(this)

        val extras = intent.extras
        if (extras != null) {
            placeId = extras.getInt("placeId")
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        placeId?.let { getPlaceDetail(it) }

        binding.checkOutButton.setOnClickListener {
            showCheckOutDialog()
        }

        adapter = CheckedInUserAdapter(
            this@CheckedInUserStatusActivity,
            list
        ) { pos, userData ->
            onItemClick(
                pos,
                userData.user_id.toString(),
                placeId!!,
                placeName
            )
        }
        binding.userDataRv.layoutManager =
            GridLayoutManager(this@CheckedInUserStatusActivity, 4)
        binding.userDataRv.adapter = adapter
    }

    private fun showCheckOutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.check_out))
            .setMessage(resources.getString(R.string.checkout_supporting_text))
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.check_out)) { _, _ ->
                checkOutUser()
            }
            .show()
    }

    private fun checkOutUser() {
        Log.d(TAG, "checkOutUser: ")
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
                        showToast("Checking Out from the place.")
                        prefHelper.placeId = -1
                        prefHelper.placeLat = ""
                        prefHelper.placeLng = ""
                        prefHelper.placeName = ""
                        prefHelper.isCheckedIn = false
                        finish()
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CheckOutResponse?>, t: Throwable) {
                Log.e(TAG, "$className checkOutUser() onFailure: $t")
            }
        })
    }

    private fun getPlaceDetail(placeId: Int) {
        val call = RetrofitBuilder.apiService.getPlaceDetail(
            auth_key = SharedPreferenceManager(this).authKey,
            placeIdModel = PlaceIdModel(placeId)
        )
        call?.enqueue(object : Callback<PlaceDetailResponse?> {
            override fun onResponse(
                call: Call<PlaceDetailResponse?>,
                response: Response<PlaceDetailResponse?>
            ) {
                val placeDetailResponse: PlaceDetailResponse?
                when (response.code()) {
                    200 -> {
                        placeDetailResponse = response.body()
                        val checkInUser = placeDetailResponse!!.data

                        list.clear()
                        list.addAll(checkInUser.check_in_users)
                        adapter.notifyDataSetChanged()

                        Log.d(TAG, "checkInUSer: $checkInUser")
                        placeName = checkInUser.place_info.place_name
                        binding.placeName.text = placeName
                    }
                    500 -> {
                        showToast("Database error")
                    }
                }
            }

            override fun onFailure(call: Call<PlaceDetailResponse?>, t: Throwable) {
                Log.i(TAG, "$className getPlaceDetail() onFailure: $t")
            }
        })
    }

    private fun onItemClick(pos: Int, userId: String, placeId: Int, placeName: String) {
        val userDetail = CheckedInUserDetailModel(userId, placeId)
        val call = RetrofitBuilder.apiService.getCheckedInUserDetail(prefHelper.authKey, userDetail)
        call?.enqueue(object : Callback<CheckedInUserDetailResponse?> {
            override fun onResponse(
                call: Call<CheckedInUserDetailResponse?>,
                response: Response<CheckedInUserDetailResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        val detailResponse = response.body()?.data
                        if (detailResponse == null) {
                            list.removeAt(pos)
                            adapter.notifyItemRemoved(pos)
                            showToast("User has checked from the place")
                            getPlaceDetail(placeId)
                        } else {
                            val intent = Intent(
                                this@CheckedInUserStatusActivity,
                                ConnectionRequestActivity::class.java
                            )
                            intent.putExtra("placeId", placeId)
                            intent.putExtra("userId", detailResponse.user_id)
                            intent.putExtra("placeName", placeName)
                            intent.putExtra("userName", detailResponse.full_name)
                            intent.putExtra("profilePic", detailResponse.profile_pic)
                            intent.putExtra("bio", detailResponse.bio)
                            intent.putExtra("dob", detailResponse.dob)
                            intent.putExtra("isPrivate", detailResponse.is_private)
                            intent.putExtra("isBusy", detailResponse.is_busy)
                            intent.putExtra("isBusyWithMe", detailResponse.is_busy_with_me)
                            intent.putExtra("ChatId", detailResponse.chat_id)
                            intent.putExtra("chatType", detailResponse.chat_type)
                            intent.putExtra("startDateTime", detailResponse.start_datetime)
                            intent.putExtra("isRequestSent", detailResponse.is_request_sent)
                            intent.putExtra("isRequestReceived", detailResponse.is_request_received)
                            startActivity(intent)
                        }
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CheckedInUserDetailResponse?>, t: Throwable) {
                Log.e(TAG, "getCheckedInUserDetail() onFailure: $t")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(requestReceiver, IntentFilter("newCheckedInUser"))
    }

    private val requestReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val placeId = intent?.getStringExtra("placeId")
            placeId?.toInt()?.let { getPlaceDetail(it) }
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestReceiver)
    }
}