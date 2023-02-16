package com.flok22.android.agicent.bottomsheet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.BottomSheetUserSettingBinding
import com.flok22.android.agicent.model.checkOut.CheckOutModel
import com.flok22.android.agicent.model.checkOut.CheckOutResponse
import com.flok22.android.agicent.model.privateAccount.AccountPrivateModel
import com.flok22.android.agicent.model.privateAccount.AccountPrivateResponse
import com.flok22.android.agicent.model.signOut.ClearTokenResponse
import com.flok22.android.agicent.model.signOut.TokenModel
import com.flok22.android.agicent.model.updateProfile.UpdateResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.ui.BlockedUsersActivity
import com.flok22.android.agicent.ui.LoginActivity
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.DeviceTokenPref
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserSettingsBottomSheet : BottomSheetDialogFragment() {

    private val className = UserSettingsBottomSheet::class.java.simpleName
    private lateinit var binding: BottomSheetUserSettingBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private lateinit var deviceTokenPref: DeviceTokenPref

    companion object {
        fun newInstance() = UserSettingsBottomSheet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        prefHelper = SharedPreferenceManager(requireContext())
        deviceTokenPref = DeviceTokenPref(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetUserSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDialog()

        //for account privacy
        binding.accountSwitch.isChecked = prefHelper.isPrivate == "1"
        binding.accountSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                makePublicPrivateAccount(AccountPrivateModel("1"))
            } else if (!isChecked) {
                makePublicPrivateAccount(AccountPrivateModel("0"))
            }
        }

        //for blocked user list
        binding.blocked.setOnClickListener {
            startActivity(Intent(requireContext(), BlockedUsersActivity::class.java))
            dismiss()
        }

        //for logging out
        binding.signOut.setOnClickListener {
            showSignOutDialog()
        }

        //for account deletion
        binding.deleteAccountTv.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    private fun makePublicPrivateAccount(accountPrivateModel: AccountPrivateModel) {
        val call =
            RetrofitBuilder.apiService.makeAccountPrivate(prefHelper.authKey, accountPrivateModel)

        call?.enqueue(object : Callback<AccountPrivateResponse?> {
            override fun onResponse(
                call: Call<AccountPrivateResponse?>,
                response: Response<AccountPrivateResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        prefHelper.isPrivate = accountPrivateModel.is_private
                        response.body()?.message?.let { requireContext().showToast(it) }
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<AccountPrivateResponse?>, t: Throwable) {
                Log.e(TAG, "$className makePublicPrivateAccount() onFailure: $t")
            }
        })
    }

    private fun showSignOutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.sign_out))
            .setMessage(resources.getString(R.string.signout_supporting_text))
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.sign_out)) { _, _ ->
                if (prefHelper.isCheckedIn) {
                    checkOutUser()
                } else clearDeviceToken("Logged Out successfully")
            }
            .show()
    }

    private fun showDeleteAccountDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_account))
            .setMessage(resources.getString(R.string.delete_account_text))
            .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete_account)) { _, _ ->
                deleteAccount()
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
                        response.body()?.message?.let { clearDeviceToken(it) }
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CheckOutResponse?>, t: Throwable) {
                Log.e(TAG, "$className checkOutUser() onFailure: $t")
            }
        })
    }

    private fun deleteAccount() {
        val call = RetrofitBuilder.apiService.deleteAccount(prefHelper.authKey)
        call?.enqueue(object : Callback<UpdateResponse?> {
            override fun onResponse(
                call: Call<UpdateResponse?>,
                response: Response<UpdateResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        response.body()?.message?.let { requireContext().showToast(it) }
                        prefHelper.clearData()
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        activity?.finishAffinity()
                    }
                    else -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<UpdateResponse?>, t: Throwable) {
                Log.e(TAG, "$className deleteAccount onFailure: $t")
            }
        })
    }

    private fun clearDeviceToken(responseText: String) {
        val authKey = prefHelper.authKey
        val deviceType = prefHelper.deviceType
        val deviceToken = deviceTokenPref.deviceToken
        val token = TokenModel(device_type = deviceType, device_token = deviceToken)
        val call = RetrofitBuilder.apiService.clearDeviceToken(authKey, token)
        call?.enqueue(object : Callback<ClearTokenResponse?> {
            override fun onResponse(
                call: Call<ClearTokenResponse?>,
                response: Response<ClearTokenResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        requireContext().showToast(responseText)
                        prefHelper.clearData()
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        activity?.finishAffinity()
                    }
                    500 -> {
                        response.body()?.message?.let { requireContext().showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<ClearTokenResponse?>, t: Throwable) {
                Log.e(TAG, "clearDeviceToken() onFailure: $t")
            }
        })
    }

    private fun initDialog() {
        requireDialog().window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        requireDialog().window?.statusBarColor =
            requireContext().getColor(android.R.color.transparent)
    }
}