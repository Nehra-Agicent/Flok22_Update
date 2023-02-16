package com.flok22.android.agicent.model.allChatMsg

data class AllChatMessageResponse(
    val `data`: ArrayList<GetChatMessage>,
    val message: String,
    val success: Int
)