package com.flok22.android.agicent.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPreferenceManager(internal var context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("UserFile", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    var isLoggedIn: Boolean
        get() = preferences.getBoolean(Constants.IS_LOGGED_IN, false)
        set(value) {
            editor.putBoolean(Constants.IS_LOGGED_IN, value)
            editor.apply()
        }

    var authKey: String
        get() = preferences.getString(Constants.AUTH_KEY, "").toString()
        set(value) {
            editor.putString(Constants.AUTH_KEY, value)
            editor.commit()
        }

    var isFromOtpScreen: Boolean
        get() = preferences.getBoolean(Constants.IS_FROM_OTP_SCREEN, false)
        set(value) {
            editor.putBoolean(Constants.IS_FROM_OTP_SCREEN, value)
            editor.apply()
        }

    var isCheckedIn: Boolean
        get() = preferences.getBoolean(Constants.Is_CHECKED_IN, false)
        set(value) {
            editor.putBoolean(Constants.Is_CHECKED_IN, value)
            editor.apply()
        }

    var deviceType: String
        get() = preferences.getString(Constants.DEVICE_TYPE, "").toString()
        set(value) {
            editor.putString(Constants.DEVICE_TYPE, value)
            editor.commit()
        }

    var profilePic: String
        get() = preferences.getString(Constants.PROFILE_PIC, "").toString()
        set(value) {
            editor.putString(Constants.PROFILE_PIC, value)
            editor.commit()
        }

    var name: String
        get() = preferences.getString(Constants.NAME, "").toString()
        set(value) {
            editor.putString(Constants.NAME, value)
            editor.commit()
        }

    var dob: String
        get() = preferences.getString(Constants.DOB, "").toString()
        set(value) {
            editor.putString(Constants.DOB, value)
            editor.commit()
        }

    var gender: String
        get() = preferences.getString(Constants.GENDER, "").toString()
        set(value) {
            editor.putString(Constants.GENDER, value)
            editor.commit()
        }

    var bio: String
        get() = preferences.getString(Constants.BIO, "").toString()
        set(value) {
            editor.putString(Constants.BIO, value)
            editor.commit()
        }

    var isPrivate: String
        get() = preferences.getString(Constants.PRIVATE, "").toString()
        set(value) {
            editor.putString(Constants.PRIVATE, value)
            editor.commit()
        }

    var placeId: Int
        get() = preferences.getInt(Constants.PLACE_ID, -1)
        set(value) {
            editor.putInt(Constants.PLACE_ID, value)
            editor.commit()
        }

    var placeName: String
        get() = preferences.getString(Constants.PLACE_NAME, "").toString()
        set(value) {
            editor.putString(Constants.PLACE_NAME, value)
            editor.commit()
        }

    var placeLat: String
        get() = preferences.getString(Constants.PLACE_LAT, "").toString()
        set(value) {
            editor.putString(Constants.PLACE_LAT, value)
            editor.commit()
        }

    var placeLng: String
        get() = preferences.getString(Constants.PLACE_LNG, "").toString()
        set(value) {
            editor.putString(Constants.PLACE_LNG, value)
            editor.commit()
        }

    var userId: String
        get() = preferences.getString(Constants.USER_ID, "").toString()
        set(value) {
            editor.putString(Constants.USER_ID, value)
            editor.commit()
        }

    var phoneNum: String
        get() = preferences.getString(Constants.PHONE_NUM, "").toString()
        set(value) {
            editor.putString(Constants.PHONE_NUM, value)
            editor.commit()
        }

    var timeOffset:String
    get() = preferences.getString(Constants.TIME_OFFSET, "").toString()
    set(value){
        editor.putString(Constants.TIME_OFFSET, value)
        editor.commit()
    }

    fun clearData() {
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }
}

class DeviceTokenPref(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("DeviceToken", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    var deviceToken: String
        get() = preferences.getString(Constants.FIREBASE_DEVICE_TOKEN, "").toString()
        set(value) {
            editor.putString(Constants.FIREBASE_DEVICE_TOKEN, value)
            editor.commit()
        }

    var accessKey: String
        get() = preferences.getString(Constants.ACCESS_KEY, "").toString()
        set(value) {
            editor.putString(Constants.ACCESS_KEY, value)
            editor.commit()
        }

    var secretKey: String
        get() = preferences.getString(Constants.SECRET_KEY, "").toString()
        set(value) {
            editor.putString(Constants.SECRET_KEY, value)
            editor.commit()
        }

    var bucketName: String
        get() = preferences.getString(Constants.BUCKET_NAME, "").toString()
        set(value) {
            editor.putString(Constants.BUCKET_NAME, value)
            editor.commit()
        }
}