package com.flok22.android.agicent.googleNearPlaces

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitGoogleClient {
    companion object {

       private val GOOGLE_API_URL="https://maps.googleapis.com/"
        val googleApiService: GoogleApiService by lazy {
            Retrofit.Builder()
                .baseUrl(GOOGLE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okClient())
                .build()
                .create(GoogleApiService::class.java)
        }


        private fun okClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
}