package com.flok22.android.agicent.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.flok22.android.agicent.adapter.BlockedUserAdapter
import com.flok22.android.agicent.databinding.ActivityBlockedUsersBinding
import com.flok22.android.agicent.model.blockedUser.BlockedUsers
import com.flok22.android.agicent.model.blockedUser.Data
import com.flok22.android.agicent.model.unblock.UnBlockResponse
import com.flok22.android.agicent.model.unblock.UnBlockUserBody
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.Constants.TAG
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.showToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BlockedUsersActivity : AppCompatActivity() {

    private val className = BlockedUsersActivity::class.java.simpleName
    private lateinit var binding: ActivityBlockedUsersBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var adapter: BlockedUserAdapter? = null
    private var blockedUserData: ArrayList<Data>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        blockedUserData = ArrayList()
        prefHelper = SharedPreferenceManager(this)

        adapter = BlockedUserAdapter(this, blockedUserData!!) { pos, userId ->
            unBlockUser(pos, userId.toString())
        }
        binding.blockedUSerRV.layoutManager = LinearLayoutManager(this)
        binding.blockedUSerRV.adapter = adapter

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        getBlockedUser(prefHelper.authKey)
    }

    private fun unBlockUser(pos: Int, userId: String) {
        val call =
            RetrofitBuilder.apiService.unBlockUser(prefHelper.authKey, UnBlockUserBody(userId))
        call?.enqueue(object : Callback<UnBlockResponse?> {
            override fun onResponse(
                call: Call<UnBlockResponse?>,
                response: Response<UnBlockResponse?>
            ) {
                when (response.code()) {
                    200 -> {
                        if (blockedUserData!!.isNotEmpty()) {
                            blockedUserData!!.removeAt(pos)
                            adapter?.notifyItemRemoved(pos)
                        }
                        response.body()?.message?.let { showToast(it) }
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                    else -> {
                        showToast("Unable to get response")
                    }
                }
            }

            override fun onFailure(call: Call<UnBlockResponse?>, t: Throwable) {
                Log.e(TAG, "$className unBlockUser() onFailure: $t")
            }
        })
    }

    private fun getBlockedUser(authKey: String) {
        binding.spinKit.visibility = View.VISIBLE
        val call = RetrofitBuilder.apiService.getBlockedUsers(authKey = authKey)
        call?.enqueue(object : Callback<BlockedUsers?> {
            override fun onResponse(call: Call<BlockedUsers?>, response: Response<BlockedUsers?>) {
                when (response.code()) {
                    200 -> {
                        binding.spinKit.visibility = View.GONE
                        val data = response.body()?.data
                        blockedUserData?.clear()
                        data?.let { blockedUserData?.addAll(it) }
                        adapter?.notifyDataSetChanged()
                    }
                    500 -> {
                        response.body()?.message?.let { showToast(it) }
                    }
                    else -> {
                        binding.spinKit.visibility = View.GONE
                        showToast("Unable to get response")
                    }
                }
            }

            override fun onFailure(call: Call<BlockedUsers?>, t: Throwable) {
                binding.spinKit.visibility = View.GONE
                Log.e(TAG, "$className getBlockedUser() onFailure: $t")
            }
        })
    }
}