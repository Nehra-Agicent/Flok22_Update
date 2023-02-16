package com.flok22.android.agicent.model.otp

import com.google.gson.annotations.SerializedName

data class OtpModel(
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("phone_num")
    val phoneNumber: String,
    @SerializedName("otp")
    val otp: String,
    val time_offset: String
)
