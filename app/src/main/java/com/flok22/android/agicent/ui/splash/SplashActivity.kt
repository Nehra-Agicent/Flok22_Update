package com.flok22.android.agicent.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.flok22.android.agicent.MainActivity
import com.flok22.android.agicent.databinding.ActivitySplashBinding
import com.flok22.android.agicent.ui.LoginActivity
import com.flok22.android.agicent.ui.SignUpActivity
import com.flok22.android.agicent.ui.intro.IntroActivity
import com.flok22.android.agicent.utils.SharedPreferenceManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    lateinit var prefHelper: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper = SharedPreferenceManager(this)
        window?.decorView?.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        //   window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,WindowManager.LayoutParams.FLAG_FULLSCREEN)

        window.statusBarColor = Color.TRANSPARENT

        Handler().postDelayed({
            checkAuth()
            finish()
        }, 2000)
        //  setSystemBarTheme(false)
    }

    private fun checkAuth() {
        if (prefHelper.isLoggedIn) {
            prefHelper.isFromOtpScreen = false
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        } else {
            startActivity(Intent(this, IntroActivity::class.java))
        }
    }
}