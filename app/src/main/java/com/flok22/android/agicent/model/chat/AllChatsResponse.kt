package com.flok22.android.agicent.model.chat

data class AllChatsResponse(
    val `data`: List<Data>,
    val message: String,
    val success: Int
)