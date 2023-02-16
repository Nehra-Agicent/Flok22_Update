package com.flok22.android.agicent.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.FragmentChatBinding
import com.google.android.material.tabs.TabLayoutMediator

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatSlidePagerAdapter: ChatSlidePagerAdapter

    companion object {
        var isFromNotification = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatSlidePagerAdapter = ChatSlidePagerAdapter(childFragmentManager, lifecycle)

        /*chatSlidePagerAdapter.addFragment(MessageFragment(), "Message")
        chatSlidePagerAdapter.addFragment(NotificationFragment(), "Missed Notification")
        chatSlidePagerAdapter.addFragment(HistoryFragment(), "Historical Data")*/

        binding.viewPager.adapter = chatSlidePagerAdapter

        binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tab, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.message)
                1 -> tab.text = getString(R.string.missed_connection)
                2 -> tab.text = getString(R.string.historical_data)
            }
        }.attach()

        /*for (i in 0 until binding.tab.tabCount) {
            val tv = requireActivity().layoutInflater.inflate(R.layout.custom_tab, null) as TextView
            binding.tab.getTabAt(i)!!.customView = tv
            tv.text = tabTitle[i]
        }*/

        if (isFromNotification) {
            binding.tab.selectTab(binding.tab.getTabAt(1))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFromNotification = false
    }
}