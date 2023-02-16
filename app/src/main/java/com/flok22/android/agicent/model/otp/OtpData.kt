package com.flok22.android.agicent.model.otp

data class OtpData(
    val auth_Key: String,
    val bio: String,
    val country_code: String,
    val dob: String,
    val email_id: String,
    val full_name: String,
    val gender: String,
    val is_active: String,
    val is_block: String,
    val is_email_verified: String,
    val phone_num: String,
    val profile_pic: String,
    val user_id: String,
    val is_private: String
)