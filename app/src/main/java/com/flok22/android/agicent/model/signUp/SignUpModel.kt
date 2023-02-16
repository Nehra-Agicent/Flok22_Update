package com.flok22.android.agicent.model.signUp

import com.google.gson.annotations.SerializedName

data class SignUpModel(
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("phone_num")
    val phoneNum: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("email_id")
    val email: String,
    @SerializedName("dob")
    val dateOfBirth: String,
    @SerializedName("profile_pic")
    val profilePic: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("bio")
    val bio: String,
    @SerializedName("device_type")
    val deviceType: String,
    @SerializedName("device_token")
    val deviceToken: String
)