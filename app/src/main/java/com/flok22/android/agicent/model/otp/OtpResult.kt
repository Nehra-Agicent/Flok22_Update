package com.flok22.android.agicent.model.otp

data class OtpResult(
    val message: String,
    val data: OtpData,
    val success: Int
)