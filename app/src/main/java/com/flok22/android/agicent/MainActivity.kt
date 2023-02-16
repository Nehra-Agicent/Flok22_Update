package com.flok22.android.agicent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.flok22.android.agicent.databinding.ActivityMainBinding
import com.flok22.android.agicent.fragment.ChatFragment
import com.flok22.android.agicent.fragment.ConnectionRequestDialog
import com.flok22.android.agicent.model.userInfo.UserInfoModel
import com.flok22.android.agicent.model.userInfo.UserInfoResponse
import com.flok22.android.agicent.network.RetrofitBuilder
import com.flok22.android.agicent.ui.MessageScreen
import com.flok22.android.agicent.utils.*
import com.flok22.android.agicent.utils.Constants.TAG
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.RoundedCornerTreatment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), TapListener {

    private val className = MainActivity::class.java.simpleName
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment

    private var isMapShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSystemBarTheme(false)
        SharedPreferenceManager(this).isLoggedIn = true

        /*supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, MapFragment())
            commit()
        }*/

        setUiForDiffApi()
        setFragments()
        handleNotificationClick()
    }

    private fun setUiForDiffApi() {
        if (Build.VERSION.SDK_INT >= 29) {

            Log.d(TAG, "$className setUiForDiffApi: sdk 29 and above")

            //For rounded corners for bottom app bar
            val bottomBarBackground = binding.bottomAppBar.background as MaterialShapeDrawable
            bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
                .toBuilder()
                .setAllCorners(RoundedCornerTreatment())
                .setTopLeftCornerSize(RelativeCornerSize(0.5f))
                .setTopRightCornerSize(RelativeCornerSize(0.5f))
                .build()

            /**
             * For background of bottom app bar
             */
            binding.bottomNavView.background = null
            binding.bottomNavView.itemIconTintList = null

            /**
             * For visibility issue in profile page
             */
            binding.mainLayout.viewTreeObserver.addOnGlobalLayoutListener {
                val heightDiff = binding.mainLayout.rootView.height - binding.mainLayout.height
                if (heightDiff > 400) {
                    binding.bottomAppBar.hide()
                    binding.fab.hide()
                } else {
                    binding.bottomAppBar.show()
                    binding.fab.show()
                }
            }
            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            navController = navHostFragment.navController
            setupWithNavController(binding.bottomNavView, navController)

        } else {
            Log.d(TAG, "setUiForDiffApi: sdk 28 and below")
            binding.containerForApi28.show()
            binding.bottomAppBar.invisible()
            binding.fab.hide()

            binding.navViewBelow29.itemIconTintList = null

            binding.mainLayout.viewTreeObserver.addOnGlobalLayoutListener {
                val a = binding.mainLayout.rootView.height
                val b = binding.mainLayout.height
                val heightDiff = a - b
                if (heightDiff > 350) {
                    binding.navViewBelow29.invisible()
                    binding.locationFab.hide()
                } else {
                    binding.navViewBelow29.show()
                    binding.locationFab.show()
                }
            }

            navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
            navController = navHostFragment.navController
            setupWithNavController(binding.navViewBelow29, navController)
        }
    }

    private fun setFragments() {
        if (Build.VERSION.SDK_INT >= 29) {
            binding.bottomNavView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.chat -> {
                        isMapShowing = false
                        Log.d(TAG, "$className inside menu chat: isMapShowing:$isMapShowing")
                        NavigationUI.onNavDestinationSelected(it, navController)
                    }
                    R.id.location -> {
                        isMapShowing = true
                        Log.d(TAG, "$className inside menu location: isMapShowing:$isMapShowing")
//                        navController.navigate(R.id.action_global_location)
                        NavigationUI.onNavDestinationSelected(it, navController)
                        true

//                        changed because of distance condition
                    }
                    R.id.userProfile -> {
                        isMapShowing = false
                        Log.d(TAG, "$className inside menu userProfile: isMapShowing:$isMapShowing")
                        NavigationUI.onNavDestinationSelected(it, navController)

//                          Without nav component
                        //setCurrentFragment(UserProfileFragment())
                    }
                    else -> false
                }
            }
            binding.bottomNavView.selectedItemId = R.id.location
            binding.fab.setOnClickListener {
                if (!isMapShowing) {
                    isMapShowing = true
                    navController.navigate(R.id.action_global_location)
                }
            }
        } else {
            binding.navViewBelow29.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.chat -> {
                        isMapShowing = false
                        Log.d(TAG, "$className inside menu item: isMapShowing:$isMapShowing")
                        NavigationUI.onNavDestinationSelected(it, navController)
                    }
                    R.id.location -> {
                        isMapShowing = true
                        Log.d(TAG, "inside menu item: isMapShowing:$isMapShowing")
//                        navController.navigate(R.id.action_global_location)
                        NavigationUI.onNavDestinationSelected(it, navController)
                        true

                        /**
                         * changed because of distance condition
                         */
                    }
                    R.id.userProfile -> {
                        isMapShowing = false
                        Log.d(TAG, "$className inside menu item: isMapShowing:$isMapShowing")
                        NavigationUI.onNavDestinationSelected(it, navController)
                    }
                    else -> false
                }
            }
            binding.navViewBelow29.selectedItemId = R.id.location

            binding.locationFab.setOnClickListener {
                Log.d(TAG, "onLocationFabClick: isMapShowing:$isMapShowing")
                if (!isMapShowing) {
                    isMapShowing = true
                    navController.navigate(R.id.action_global_location)
                }
            }
        }
    }

    /**
     * Used when nav component were not implemented
     */
    /*private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.container, fragment)
            commit()
        }
    }*/

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(requestReceiver, IntentFilter("connectionRequest"))
    }

    private val requestReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val senderId = intent?.getStringExtra("senderId")
            val call =
                RetrofitBuilder.apiService.getCheckedInUserInfo(
                    SharedPreferenceManager(this@MainActivity).authKey, UserInfoModel(senderId!!)
                )
            call?.enqueue(object : Callback<UserInfoResponse?> {
                override fun onResponse(
                    call: Call<UserInfoResponse?>,
                    response: Response<UserInfoResponse?>
                ) {
                    when (response.code()) {
                        200 -> {
                            val userInfo = response.body()?.data?.get(0)
                            val bundle = Bundle()
                            bundle.putString("fullName", userInfo?.full_name)
                            bundle.putString("dob", userInfo?.dob)
                            bundle.putString("pic", userInfo?.profile_pic)
                            userInfo?.place_id?.let { bundle.putInt("placeId", it) }
                            userInfo?.user_id?.let { bundle.putInt("userId", it) }
                            val dialog = ConnectionRequestDialog(null, this@MainActivity)
                            dialog.arguments = bundle
                            dialog.show(supportFragmentManager, "ConnectionRequestDialog")
                        }
                        500 -> {
                            response.body()?.message?.let { showToast(it) }
                        }
                    }
                }

                override fun onFailure(call: Call<UserInfoResponse?>, t: Throwable) {
                    Log.e(TAG, "$className getCheckedInUserInfo onFailure: $t")
                }
            })
        }

    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestReceiver)
    }

    private fun handleNotificationClick() {
        when (intent.action) {
            "connectionRequest" -> {
                intent?.apply {
                    ChatFragment.isFromNotification = true
                    navController.navigate(R.id.action_global_chat)
                    isMapShowing = false
                }
            }
            "chat" -> {
                intent?.apply {
                    val receiverId: Int = getIntExtra("otherUserId", 0)
                    val name = getStringExtra("senderName")
                    val profileImg = getStringExtra("profilePic")
                    val notificationDateTime = getStringExtra("notificationDateTime")
                    val createdDatetime = getStringExtra("startDatetime")
                    val chatId: Int = getIntExtra("chatId", 0)
                    val chatType: Int = getIntExtra("chatType", 0)
                    /*val lastMsgId: String = intent.getStringExtra("lastMsgId").toString()
                    val onlineStatus: Int = intent.getIntExtra("onlineStatus", 0)*/

                    val i = Intent(this@MainActivity, MessageScreen::class.java)
                    i.putExtra("otherUserId", receiverId)
                    i.putExtra("senderName", name)
                    i.putExtra("profilePic", profileImg)
                    i.putExtra("notificationDateTime", notificationDateTime)
                    i.putExtra("createdDatetime", createdDatetime)
                    i.putExtra("chatId", chatId)
                    i.putExtra("chatType", chatType)
                    startActivity(i)
                }
            }
            "accept" -> {
                intent?.apply {
                    val receiverId: Int = getIntExtra("otherUserId", 0)
                    val name = getStringExtra("senderName")
                    val profileImg = getStringExtra("profilePic")
                    val createdDatetime = getStringExtra("startDatetime")
                    val chatId: Int = getIntExtra("chatId", 0)
                    val chatType: Int = getIntExtra("chatType", 0)

                    val i = Intent(this@MainActivity, MessageScreen::class.java)
                    i.putExtra("otherUserId", receiverId)
                    i.putExtra("senderName", name)
                    i.putExtra("profilePic", profileImg)
                    i.putExtra("createdDatetime", createdDatetime)
                    i.putExtra("chatId", chatId)
                    i.putExtra("chatType", chatType)
                    startActivity(i)
                }
            }
        }
    }

    override fun onTapped() {}

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestReceiver)
    }
}