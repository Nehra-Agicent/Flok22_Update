package com.flok22.android.agicent.model.login

import com.google.gson.annotations.SerializedName

data class LogInModel(
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("phone_num")
    val phoneNumber: String
)
