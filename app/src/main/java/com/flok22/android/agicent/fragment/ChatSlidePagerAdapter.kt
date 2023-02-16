package com.flok22.android.agicent.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChatSlidePagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MessageFragment()
            1 -> NotificationFragment()
            2 -> HistoryFragment()
            else -> Fragment()
        }
    }

    override fun getItemCount() = 3

}