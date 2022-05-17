package com.example.gogolookinterview.api

import com.example.gogolookinterview.BuildConfig
import com.example.gogolookinterview.model.SearchImagesData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("api/")
    suspend fun getSearchImages(
        @Query("key") key: String = BuildConfig.API_KEY,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): Response<SearchImagesData>

    companion object {
        private fun getBaseUrl() = "https://pixabay.com/"
        private val client
            get() = OkHttpClient().newBuilder().apply {
                val interceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                connectTimeout(60, TimeUnit.SECONDS)
                readTimeout(60, TimeUnit.SECONDS)
                writeTimeout(120, TimeUnit.SECONDS)
                addInterceptor(interceptor)
            }.build()

        fun create(): ApiService {
            return Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
        }
    }
}