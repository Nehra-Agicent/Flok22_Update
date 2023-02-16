package com.flok22.android.agicent.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

import com.flok22.android.agicent.ui.intro.ScreenSlideFragment
import com.flok22.android.agicent.ui.intro.ScreenSlideFragment2
import com.flok22.android.agicent.ui.intro.ScreenSlideFragment3

class SlidePagerAdapter(pager: FragmentActivity) : FragmentStateAdapter(pager) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                ScreenSlideFragment()
            }
            1 -> {
                ScreenSlideFragment2()
            }
            else -> {
                ScreenSlideFragment3()
            }
        }

    }
}