package com.flok22.android.agicent.model.userInfo

data class UserInfoResponse(
    val `data`: List<Data>,
    val message: String,
    val success: Int
)