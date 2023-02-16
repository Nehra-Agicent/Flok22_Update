package com.flok22.android.agicent.googleNearPlaces

import com.flok22.android.agicent.model.GooglePlacesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface GoogleApiService {
    @GET
    fun getNearByPlaces(@Url url:String):Call<GooglePlacesResponse>
}