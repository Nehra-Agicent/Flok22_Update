package com.flok22.android.agicent.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.flok22.android.agicent.adapter.HistoryAdapter
import com.flok22.android.agicent.databinding.FragmentHistoryBinding
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailModel
import com.flok22.android.agicent.model.checkedInUserDetail.CheckedInUserDetailResponse
import com.flok22.android.agicent.model.historicalData.Data
import com.flok22.android.agicent.model.historicalData.HistoryResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.ui.ConnectionRequestActivity
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryFragment : Fragment() {
    private val className = HistoryFragment::class.java.simpleName

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var historyArrayList: ArrayList<Data>? = null
    private var adapter: HistoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefHelper = SharedPreferenceManager(requireContext())
        historyArrayList = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHistoricalData()

        binding.refreshMessages.setOnRefreshListener {
            binding.refreshMessages.isRefreshing = false

            if (requireContext().isNetworkAvailable()) {
                getHistoricalData()
            } else {
                binding.noConnectionView.noConnectionLayout.show()
            }
        }
        adapter = HistoryAdapter(historyArrayList, requireContext()) { _, data ->
            onClick(data)
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = adapter

    }

    private fun onClick(data: Data) {
        val checkedInUserDetailModel =
            CheckedInUserDetailModel(data.user_id.toString(), data.place_id)
        val call = RetrofitBuilder.apiService.getCheckedInUserDetail(
            prefHelper.authKey, checkedInUserDetailModel
        )
        call?.enqueue(object : Callback<CheckedInUserDetailResponse?> {
            override fun onResponse(
                call: Call<CheckedInUserDetailResponse?>,
                response: Response<CheckedInUserDetailResponse?>
            ) {
                val detailResponse = response.body()?.data
                when (response.code()) {
                    200 -> {
                        val intent =
                            Intent(requireActivity(), ConnectionRequestActivity::class.java)
                        intent.putExtra("placeId", data.place_id)
                        intent.putExtra("userId", detailResponse?.user_id)
                        intent.putExtra("userName", detailResponse?.full_name)
                        intent.putExtra("profilePic", detailResponse?.profile_pic)
                        intent.putExtra("bio", detailResponse?.bio)
                        intent.putExtra("dob", detailResponse?.dob)
                        intent.putExtra("isPrivate", detailResponse?.is_private)
                        intent.putExtra("isBusy", detailResponse?.is_busy)
                        intent.putExtra("isBusyWithMe", detailResponse?.is_busy_with_me)
                        intent.putExtra("ChatId", detailResponse?.chat_id)
                        intent.putExtra("chatType", detailResponse?.chat_type)
                        intent.putExtra("startDateTime", detailResponse?.start_datetime)
                        intent.putExtra("isRequestSent", detailResponse?.is_request_sent)
                        intent.putExtra("isRequestReceived", detailResponse?.is_request_received)
                        startActivity(intent)
                    }
                    500 -> {
                        response.body()?.message?.let { context?.showToast(it) }
                    }
                }
            }

            override fun onFailure(call: Call<CheckedInUserDetailResponse?>, t: Throwable) {
                Log.e(TAG, "$className onClick getCheckedInUserDetail onFailure: $t")
            }
        })
    }

    private fun getHistoricalData() {
        if (requireContext().isNetworkAvailable()) {
            binding.noConnectionView.noConnectionLayout.hide()

            val call = RetrofitBuilder.apiService.getHistoricalData(prefHelper.authKey)
            call?.enqueue(object : Callback<HistoryResponse?> {
                override fun onResponse(
                    call: Call<HistoryResponse?>,
                    response: Response<HistoryResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            val data = response.body()?.data
                            if (data!!.isEmpty()) {
                                binding.historyRecyclerView.hide()
                                binding.noHistory.show()
                            } else {
                                binding.historyRecyclerView.show()
                                binding.noHistory.hide()
                                historyArrayList?.clear()
                                response.body()?.data?.let { historyArrayList?.addAll(it) }
                                adapter?.notifyDataSetChanged()
                            }
                        }
                        204 -> {
                            binding.historyRecyclerView.hide()
                            binding.noHistory.show()
                        }
                        500 -> {
                            response.body()?.message?.let { context?.showToast(it) }
                        }
                        else -> {
                            context?.showToast("No response")
                        }
                    }
                }

                override fun onFailure(call: Call<HistoryResponse?>, t: Throwable) {
                    Log.e(TAG, "$className getHistoricalData() onFailure: $t")
                }
            })
        } else binding.noConnectionView.noConnectionLayout.show()
    }
}
