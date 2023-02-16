package com.flok22.android.agicent.model.chatInfo

data class Data(
    val chat_type: Int,
    val created_datetime: String,
    val expiry_datetime: String,
    val first_user_id: Int,
    val first_user_msg_unread_count: Int,
    val is_first_user_accept_permanant_chat: Int,
    val is_second_user_accept_permanant_chat: Int,
    val second_user_id: Int,
    val second_user_msg_unread_count: Int,
    val first_user_message_count: Int,
    val second_user_message_count: Int,
    val start_datetime: String
)