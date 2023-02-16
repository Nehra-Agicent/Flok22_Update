package com.flok22.android.agicent.ui.intro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.flok22.android.agicent.adapter.SlidePagerAdapter
import com.flok22.android.agicent.databinding.ActivityIntroBinding
import com.flok22.android.agicent.ui.LoginActivity

import com.google.android.material.tabs.TabLayoutMediator

class IntroActivity : AppCompatActivity() {

    lateinit var binding: ActivityIntroBinding

    private lateinit var pagerAdapter: SlidePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pagerAdapter = SlidePagerAdapter(this)
        binding.mainViewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.mainViewPager) { _, _ ->
        }.attach()

        window.statusBarColor = Color.TRANSPARENT
        binding.skip.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        // window.statusBarColor = Color.TRANSPARENT
    }

    fun Activity.setSystemBarTheme(pIsDark: Boolean) {
        val lFlags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = if (pIsDark) {
            lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        } else {
            lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}