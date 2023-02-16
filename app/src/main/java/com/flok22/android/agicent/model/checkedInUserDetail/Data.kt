package com.flok22.android.agicent.model.checkedInUserDetail

data class Data(
    val bio: String,
    val chat_id: Int,
    val chat_type: Int,
    val dob: String,
    val full_name: String,
    val gender: String,
    val is_busy: Int,
    val is_private: Int,
    val is_request_received: Int,
    val is_request_sent: Int,
    val profile_pic: String,
    val start_datetime: String,
    val user_id: Int,
    val is_busy_with_me: Int
)