package com.flok22.android.agicent.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.flok22.android.agicent.MainActivity
import com.flok22.android.agicent.R
import com.flok22.android.agicent.bottomsheet.CheckInUserTopFragment
import com.flok22.android.agicent.ui.CheckedInUserStatusActivity
import com.flok22.android.agicent.utils.Constants
import com.flok22.android.agicent.utils.DeviceTokenPref
import com.flok22.android.agicent.utils.showToast
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.lang.System.currentTimeMillis
import kotlin.random.Random

class SendPushNotification : FirebaseMessagingService() {

    private val className = SendPushNotification::class.java.simpleName
    private val channelId = ""
    lateinit var intent: Intent

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        sendNotification(remoteMessage)
//        if (remoteMessage.data["type"]?.toInt() == 1)
    }

    override fun onNewToken(token: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val deviceToken = task.result.toString()
                    DeviceTokenPref(this).deviceToken = deviceToken
                    Log.d(
                        Constants.TAG,
                        "$className onCompleted: $deviceToken"
                    )
                } else {
                    showToast("Token not generated")
                }
            }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(message: RemoteMessage) {
        when (message.data["type"]?.toInt()) {
            1 -> {
                intent = Intent(this, MainActivity::class.java)
                intent.action = "connectionRequest"
            }

            2 -> {
                intent = Intent(this, MainActivity::class.java)

                intent.action = "accept"
                intent.putExtra("otherUserId", message.data["send_by"]?.toInt())
                intent.putExtra("senderName", message.data["user_name"])
                intent.putExtra("profilePic", message.data["user_profile_pic"])
                intent.putExtra("startDatetime", message.data["start_datetime"])
                intent.putExtra("chatId", message.data["chat_id"]?.toInt())
                intent.putExtra("chatType", message.data["chat_type"]?.toInt())
            }
            3 -> {
                intent = Intent(this, MainActivity::class.java)

                intent.action = "chat"
                intent.putExtra("otherUserId", message.data["send_by "]?.toInt())
                intent.putExtra("senderName", message.data["user_name"])
                intent.putExtra("profilePic", message.data["user_profile_pic"])
                intent.putExtra("notificationDateTime", message.data["notification_datetime"])
                intent.putExtra("chatId", message.data["chat_id"]?.toInt())
                intent.putExtra("startDatetime", message.data["start_datetime"])
                intent.putExtra("chatType", message.data["chat_type"]?.toInt())

            }

            else -> {
                intent = Intent(this, MainActivity::class.java)
            }

        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        createNotificationChannel(notificationManager)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            currentTimeMillis().toInt(), intent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.app_icon)
            .setAutoCancel(true)
            .setVibrate(LongArray(2) { 100 })
            .setContentIntent(pendingIntent)
            .build()



        when (message.data["type"]?.toInt()) {
            1 -> {
                sendBroadcast(message.data["send_by"])
                notificationManager.notify(notificationID, notification)
            }
            6 -> {
                if (CheckedInUserStatusActivity.isActivityVisible || CheckInUserTopFragment.isDialogVisible) {
                    sendReloadBroadcast(message.data["place_id"])
                } else {
                    notificationManager.notify(notificationID, notification)
                }
            }
            else -> {
                notificationManager.notify(notificationID, notification)
            }
        }
    }

    private fun sendReloadBroadcast(placeId: String?) {
        val requestIntent = Intent("newCheckedInUser")
        requestIntent.putExtra("placeId", placeId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(requestIntent)
    }

    private fun sendBroadcast(senderId: String?) {
        val requestIntent = Intent("connectionRequest")
        requestIntent.putExtra("senderId", senderId)
        LocalBroadcastManager.getInstance(this).sendBroadcast(requestIntent)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }
}