package com.flok22.android.agicent.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.flok22.android.agicent.R
import com.flok22.android.agicent.databinding.LayoutConnectionEstablishedBinding
import com.flok22.android.agicent.utils.SharedPreferenceManager
import com.flok22.android.agicent.utils.aws.S3Utils

class Connected : AppCompatActivity() {

    private lateinit var binding: LayoutConnectionEstablishedBinding
    private var senderName: String? = null
    private var profilePic: String? = null
    private var createdDatetime: String? = null
    private var chatId: Int? = null
    private var chatType: Int? = null
    private lateinit var prefHelper: SharedPreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutConnectionEstablishedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefHelper = SharedPreferenceManager(this)

        val extras = intent.extras
        if (extras != null) {
            senderName = extras.getString("userName")
            profilePic = extras.getString("profilePic")
            createdDatetime = extras.getString("createdDatetime")
            chatId = extras.getInt("chatId")
            chatType = extras.getInt("chatType")
        }
        //set data on ui
        binding.connectedWith.text = getString(R.string.connected_with, senderName)
        //user icon
        Glide.with(this).load(
            S3Utils.generateS3ShareUrl(applicationContext, prefHelper.profilePic)
        ).into(binding.selfIcon)

        // friend icon
        Glide.with(this).load(
            S3Utils.generateS3ShareUrl(applicationContext, profilePic)
        ).into(binding.friendIcon)

        binding.coordinateButton.setOnClickListener {
            val intent = Intent(this@Connected, MessageScreen::class.java)
            intent.putExtra("senderName", senderName)
            intent.putExtra("profilePic", profilePic)
            intent.putExtra("chatId", chatId)
            intent.putExtra("chatType", chatType)
            intent.putExtra("createdDatetime", createdDatetime)
            startActivity(intent)
            finish()
        }
    }
}