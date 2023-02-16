package com.flok22.android.agicent.model.chatMessages

data class ChatMsg(
    var sender_id: Int,
    var receiver_id: Int,
    var message: String,
    var chat_id: Int
)
