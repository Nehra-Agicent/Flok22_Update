package com.flok22.android.agicent.model

import com.google.gson.annotations.SerializedName

data class LatLngModel(
    @SerializedName("latitude")
    val latitude: String,
    @SerializedName("longitude")
    val longitude: String
)
