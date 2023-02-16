package com.flok22.android.agicent.model.userInfo

data class Data(
    val bio: String,
    val dob: String,
    val full_name: String,
    val gender: String,
    val is_private: Int,
    val place_address: String,
    val place_id: Int,
    val place_latitude: Double,
    val place_longtitude: Double,
    val place_name: String,
    val profile_pic: String,
    val user_id: Int
)