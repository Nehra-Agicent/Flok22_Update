package com.flok22.android.agicent.bottomsheet

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.flok22.android.agicent.R
import com.flok22.android.agicent.adapter.UserProfileAdapter
import com.flok22.android.agicent.databinding.CheckInUserTopDialogBinding
import com.flok22.android.agicent.model.checkIn.Data
import com.flok22.android.agicent.model.checkIn.PlaceDetailResponse
import com.flok22.android.agicent.model.checkIn.PlaceIdModel
import com.flok22.android.agicent.model.checkedIn.CheckedInModel
import com.flok22.android.agicent.model.checkedIn.CheckedInResponse
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailModel
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.ui.ConnectionRequestActivity
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckInUserTopFragment(
    c: Context,
    private val tapListener: TapListener
) : DialogFragment() {

    private val className = CheckInUserTopFragment::class.java.simpleName

    private lateinit var binding: CheckInUserTopDialogBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var placeName: String? = null
    private var placeId = -1
    private var _context = c
    private var adapter: UserProfileAdapter? = null
    private var checkInUser: Data? = null

    companion object {
        var isDialogVisible = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = context?.let { Dialog(it, R.style.Theme_Dialog) }
        val view = View.inflate(context, R.layout.check_in_user_top_dialog, null)
        binding = CheckInUserTopDialogBinding.bind(view)
        dialog!!.setContentView(binding.root)
        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setGravity(Gravity.TOP)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        view.setOnTouchListener(object : MyGestureDetector(requireContext()) {
            override fun onSwipeUp() {
                super.onSwipeUp()
                dismiss()
            }
        })

        prefHelper = SharedPreferenceManager(_context)
        placeId = arguments?.getInt("placeId")!!

        getPlaceDetail()

        //Tap to check into place
        binding.checkIntoPlace.setOnClickListener {
            if (checkInUser?.place_info?.is_checked_in != 1) {
                Log.d(TAG, "$className onCreateDialog: checkIn")
                prefHelper.placeLat = checkInUser?.place_info?.place_latitude.toString()
                prefHelper.placeLng = checkInUser?.place_info?.place_longtitude.toString()
                checkedIntoPlace(CheckedInModel(checkInUser?.place_info!!.place_id))
            }
        }
        return dialog
    }

    private fun getPlaceDetail() {
        val call =
            RetrofitBuilder.apiService.getPlaceDetail(prefHelper.authKey, PlaceIdModel(placeId))
        call?.enqueue(object : Callback<PlaceDetailResponse?> {
            override fun onResponse(
                call: Call<PlaceDetailResponse?>,
                response: Response<PlaceDetailResponse?>
            ) {
                val placeDetailResponse: PlaceDetailResponse?
                when (response.code()) {
                    200 -> {
                        placeDetailResponse = response.body()
                        checkInUser = placeDetailResponse!!.data
                        Log.d(TAG, "Prashant $response")
                        setUpUi()
                        setRecyclerView()
                    }
                    500 -> {
                        _context.showToast("Database error")
                        binding.checkedInText.show()
                        binding.userProfileRv.invisible()
                    }
                }
            }

            override fun onFailure(call: Call<PlaceDetailResponse?>, t: Throwable) {
                Log.i(TAG, "NearbyPlaceAdapter() getPlaceDetail() onFailure: $t")
            }
        })
    }

    private fun setRecyclerView() {
        if (checkInUser?.place_info?.checked_in_count == 0) {
            binding.checkedInText.show()
            binding.userProfileRv.hide()
        } else {
            binding.checkedInText.hide()
            binding.userProfileRv.show()
            adapter = UserProfileAdapter(checkInUser!!, context) { userProfile ->
                onItemClick(userProfile.user_id.toString(), checkInUser!!.place_info.place_id)
            }
            binding.userProfileRv.layoutManager = GridLayoutManager(requireContext(), 4)
            binding.userProfileRv.adapter = adapter
        }
    }

    private fun setUpUi() {
        if (checkInUser?.place_info?.is_checked_in == 1) {
            binding.peopleInPlace.hide()
            binding.checkIntoPlace.hide()

            //only show checked in view
            binding.tapProfile.show()
            binding.checkedIntoPlace.show()

        } else {
            binding.peopleInPlace.show()
            binding.checkIntoPlace.show()

            binding.tapProfile.hide()
            binding.checkedIntoPlace.hide()
        }

        placeName = checkInUser?.place_info?.place_name
        binding.placeName.text = placeName

        binding.peopleInPlace.text = requireContext().getString(
            R.string.people_count,
            checkInUser?.place_info?.checked_in_count.toString()
        )
    }

    private fun onItemClick(userId: String, placeId: Int) {
        val userDetail = CheckedInUserDetailModel(userId, placeId)
        val call = RetrofitBuilder.apiService.getCheckedInUserDetail(prefHelper.authKey, userDetail)
        call?.enqueue(object : Callback<CheckedInUserDetailResponse?> {
            override fun onResponse(
                call: Call<CheckedInUserDetailResponse?>,
                response: Response<CheckedInUserDetailResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        val userDetailResponse = response.body()?.data
                        if (userDetailResponse == null) {
                            _context.showToast("User has checked out from the place")
                        } else {
                            val intent = Intent(_context, ConnectionRequestActivity::class.java)
                            intent.putExtra("placeId", placeId)
                            intent.putExtra("userId", userDetailResponse.user_id)
                            intent.putExtra("placeName", placeName)
                            intent.putExtra("userName", userDetailResponse.full_name)
                            intent.putExtra("profilePic", userDetailResponse.profile_pic)
                            intent.putExtra("bio", userDetailResponse.bio)
                            intent.putExtra("dob", userDetailResponse.dob)
                            intent.putExtra("isPrivate", userDetailResponse.is_private)
                            intent.putExtra("isBusy", userDetailResponse.is_busy)
                            intent.putExtra("isBusyWithMe", userDetailResponse.is_busy_with_me)
                            intent.putExtra("ChatId", userDetailResponse.chat_id)
                            intent.putExtra("chatType", userDetailResponse.chat_type)
                            intent.putExtra("startDateTime", userDetailResponse.start_datetime)
                            intent.putExtra("isRequestSent", userDetailResponse.is_request_sent)
                            intent.putExtra(
                                "isRequestReceived",
                                userDetailResponse.is_request_received
                            )

                            startActivity(intent)
                        }
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CheckedInUserDetailResponse?>, t: Throwable) {
                Log.e(TAG, "getCheckedInUserDetail() onFailure: $t")
            }
        })
    }

    private fun checkedIntoPlace(checkedInModel: CheckedInModel) {
        val call = RetrofitBuilder.apiService.checkedIntoPlace(
            auth_key = prefHelper.authKey,
            checkedInModel = checkedInModel
        )
        call?.enqueue(object : Callback<CheckedInResponse?> {
            override fun onResponse(
                call: Call<CheckedInResponse?>,
                response: Response<CheckedInResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        placeId = checkedInModel.place_id
                        prefHelper.placeId = placeId
                        prefHelper.placeName = placeName.toString()
                        prefHelper.isCheckedIn = true
                        context?.showToast("Checked in successfully")
                        binding.tapProfile.show()
                        binding.checkedIntoPlace.show()
                        binding.peopleInPlace.hide()
                        binding.checkIntoPlace.hide()
                        tapListener.onTapped()
                    }
                    405 -> {
                        requireContext().showToast("You are already checked in.")
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CheckedInResponse?>, t: Throwable) {
                Log.i(TAG, "$className checkedIntoPlace() onFailure: $t")
            }
        })
    }

    override fun onResume() {
        super.onResume()
        isDialogVisible = true
        LocalBroadcastManager.getInstance(_context)
            .registerReceiver(requestReceiver, IntentFilter("newCheckedInUser"))
    }

    private val requestReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            placeId = intent?.getStringExtra("placeId")?.toInt()!!
            getPlaceDetail()
        }
    }

    override fun onPause() {
        super.onPause()
        isDialogVisible = false
        LocalBroadcastManager.getInstance(_context).unregisterReceiver(requestReceiver)
    }
}