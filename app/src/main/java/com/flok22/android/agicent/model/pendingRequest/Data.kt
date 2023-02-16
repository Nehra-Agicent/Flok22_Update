package com.flok22.android.agicent.model.pendingRequest

data class Data(
    val bio: String,
    val full_name: String,
    val gender: String,
    val place_id: Int,
    val profile_pic: String,
    val request_by: Int,
    val receiver_id: Int,
    val dob: String,
    val place_name: String,
    val created_datetime: String
)