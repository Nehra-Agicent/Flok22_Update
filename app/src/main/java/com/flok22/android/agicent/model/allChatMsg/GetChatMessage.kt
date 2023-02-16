package com.flok22.android.agicent.model.allChatMsg

data class GetChatMessage(
    val chat_id: Int,
    val created_datetime: String,
    val message: String,
    val read_flag: Int,
    val receiver_id: Int,
    val sender_id: Int
)