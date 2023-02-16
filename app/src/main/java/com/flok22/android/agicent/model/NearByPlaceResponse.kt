package com.flok22.android.agicent.model

data class NearByPlaceResponse(
    val `data`: List<PlaceData>,
    val message: String,
    val success: Int
)