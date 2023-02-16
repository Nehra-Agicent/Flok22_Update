package com.flok22.android.agicent.utils

import com.amazonaws.regions.Regions

object Constants {

    var Is_CHECKED_IN: String? = "Is_CHECKED_IN"
    const val BASE_URL = "http://api.appdevelopmentservices.in:4000/flok22/app/v1/"
    const val NEW_BASE_URL = "http://13.126.226.53:4000/api/"
    const val TAG = "Flok 22"
    const val PLACE_IMAGE =
        "https://maps.googleapis.com/maps/api/place/photo?maxwidth=200&photo_reference=<place_key>&key=AIzaSyBpix5fIryUOOxewaG6lmnyseg5r4ZUvuo"
    const val LOCATION_PERMISSION_CODE = 1
    const val DISCONNECT_OR_BLOCK_RESULT_CODE = 111

    var ACCESS_KEY: String? = "ACCESS_KEY"
    var SECRET_KEY: String? = "SECRET_KEY"
    var BUCKET_NAME: String? = "BUCKET_NAME"
    val MY_REGION = Regions.AP_SOUTH_1

    val IS_LOGGED_IN: String? = "IS_LOGGED_IN"
    val IS_FROM_OTP_SCREEN: String? = "IS_FROM_OTP_SCREEN"
    val AUTH_KEY: String? = "AUTH_KEY"
    val DEVICE_TYPE: String? = "DEVICE_TYPE"
    val PROFILE_PIC: String? = "PROFILE_PIC"
    val NAME: String? = "NAME"
    val DOB: String? = "DOB"
    val GENDER: String? = "GENDER"
    val BIO: String? = "BIO"
    val PLACE_ID: String? = "PLACE_ID"
    val USER_ID: String? = "USER_ID"
    val PRIVATE: String? = "PRIVATE"
    val PLACE_LAT: String? = "PLACE_LAT"
    val PLACE_NAME: String? = "PLACE_NAME"
    val PLACE_LNG: String? = "PLACE_LNG"
    var FIREBASE_DEVICE_TOKEN: String? = "FIREBASE_DEVICE_TOKEN"

    val PHONE_NUM: String? = "PHONE_NUM"
    var TIME_OFFSET: String? = "TIME_OFFSET"
    private const val SOCKET_BASE_URL = "http://3.111.240.219"
    const val SOCKET_URL: String = "$SOCKET_BASE_URL:4010"

}