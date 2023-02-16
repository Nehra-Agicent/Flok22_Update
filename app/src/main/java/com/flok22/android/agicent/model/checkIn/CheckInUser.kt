package com.flok22.android.agicent.model.checkIn

data class CheckInUser(
    val bio: String,
    val full_name: String,
    val gender: String,
    val profile_pic: String,
    val user_id: Int,
    val dob: String,
    val is_private: Int,
    val is_busy: Int,
    val is_busy_with_me: Int,
    val chat_id: Int,
    val chat_type: Int
)