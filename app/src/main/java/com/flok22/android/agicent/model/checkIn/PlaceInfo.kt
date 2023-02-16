package com.flok22.android.agicent.model.checkIn

data class PlaceInfo(
    val checked_in_count: Int,
    val google_place_key: String,
    val place_address: String,
    val place_id: Int,
    val place_latitude: Double,
    val place_longtitude: Double,
    val place_name: String,
    val place_photo_key: String,
    val is_checked_in: Int
)