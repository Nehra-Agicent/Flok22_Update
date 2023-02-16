package com.flok22.android.agicent.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.flok22.android.agicent.adapter.NotificationAdapter
import com.flok22.android.agicent.databinding.FragmentNotificationBinding
import com.flok22.android.agicent.model.pendingRequest.Data
import com.flok22.android.agicent.model.pendingRequest.PendingRequestResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : Fragment(), TapListener {

    private val classname = NotificationFragment::class.java.simpleName
    private lateinit var binding: FragmentNotificationBinding
    private lateinit var prefHelper: SharedPreferenceManager
    private var pendingRequest: List<Data>? = null
    private var position = -1
    private var adapter: NotificationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefHelper = SharedPreferenceManager(requireContext())
        pendingRequest = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPendingRequest(prefHelper.authKey)
        binding.refreshMessages.setOnRefreshListener {
            binding.refreshMessages.isRefreshing = false

            if (requireContext().isNetworkAvailable()) {
                getPendingRequest(prefHelper.authKey)
            } else {
                binding.noConnectionView.noConnectionLayout.show()
            }
        }
        adapter = NotificationAdapter { pos, history, src ->
            onItemClick(pos, history, src)
        }
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.adapter = adapter
    }

    private fun getPendingRequest(authKey: String) {
        if (requireContext().isNetworkAvailable()) {
            binding.noConnectionView.noConnectionLayout.hide()
            binding.spinKit.visibility = View.VISIBLE
            val call =
                RetrofitBuilder.apiService.getPendingRequest(authKey)
            call?.enqueue(object : Callback<PendingRequestResponse?> {
                override fun onResponse(
                    call: Call<PendingRequestResponse?>,
                    response: Response<PendingRequestResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            binding.spinKit.visibility = View.GONE
                            val pendingResponse = response.body()?.data
                            if (pendingResponse!!.isEmpty()) {
                                binding.missedNotification.show()
                                binding.notificationRecyclerView.hide()
                            } else {
                                binding.missedNotification.hide()
                                binding.notificationRecyclerView.show()
                                (pendingRequest as ArrayList<Data>).clear()
                                (pendingRequest as ArrayList<Data>).addAll(pendingResponse)
                                adapter?.differ?.submitList(pendingResponse)
                            }
                        }
                        500 -> {
                            response.body()?.message?.let { requireContext().showToast(it) }
                        }
                        else -> {
                            context?.showToast("No data received")
                        }
                    }
                }

                override fun onFailure(call: Call<PendingRequestResponse?>, t: Throwable) {
                    binding.spinKit.visibility = View.GONE
                    Log.e(TAG, "$classname onViewCreated() onFailure: $t")
                }
            })
        } else binding.noConnectionView.noConnectionLayout.show()
    }

    //to open connection request dialog
    private fun onItemClick(pos: Int, history: Data, src: String) {
        when (src) {
            "root" -> {
                position = pos
                ConnectionRequestDialog(history, this).show(childFragmentManager, "")
            }
        }
    }

    //to remove from the list if request is accepted/rejected
    override fun onTapped() {
        if ((pendingRequest as ArrayList<Data>).isNotEmpty()) {
            (pendingRequest as ArrayList<Data>).removeAt(position)
            adapter?.notifyItemRemoved(position)
            adapter?.differ?.submitList(pendingRequest)
            if (pendingRequest!!.isEmpty()) {
                binding.missedNotification.show()
                binding.notificationRecyclerView.hide()
            }
        }
    }
}