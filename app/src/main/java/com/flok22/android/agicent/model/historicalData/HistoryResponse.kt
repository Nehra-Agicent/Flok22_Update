package com.flok22.android.agicent.model.historicalData

data class HistoryResponse(
    val `data`: List<Data>,
    val message: String,
    val success: Int
)