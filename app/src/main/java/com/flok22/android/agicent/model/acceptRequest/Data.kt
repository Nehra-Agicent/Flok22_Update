package com.flok22.android.agicent.model.acceptRequest

data class Data(
    val chat_id: Int,
    val chat_type: Int,
    val full_name: String,
    val profile_pic: String,
    val start_datetime: String
)