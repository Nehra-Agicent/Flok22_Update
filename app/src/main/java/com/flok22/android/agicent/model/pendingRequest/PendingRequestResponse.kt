package com.flok22.android.agicent.model.pendingRequest

data class PendingRequestResponse(
    val `data`: List<Data>,
    val message: String,
    val success: Int
)