package com.flok22.android.agicent.model.chat

data class Data(
    val chat_id: Int,
    val other_user_id: Int,
    val profile_pic: String,
    val second_user_msg_unread_count: Int,
    val user_name: String,
    val start_datetime: String,
    val created_datetime: String,
    val chat_type: Int,
    val is_permanent_chat_accepted_by_me: Int,
    val is_permanent_chat_accepted_by_other: Int,
    val message_count: Int
)