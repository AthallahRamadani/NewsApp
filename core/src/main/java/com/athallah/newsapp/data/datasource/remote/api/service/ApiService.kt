package com.athallah.newsapp.data.datasource.remote.api.service

import com.athallah.newsapp.data.datasource.remote.api.response.EverythingResponse
import com.athallah.newsapp.data.datasource.remote.api.response.HeadlineResponse
import com.athallah.newsapp.data.utils.Constant.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String,
        @Query("category") category: String? = null,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<HeadlineResponse>

    @GET("v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("page") pageNumber: Int,
        @Query("apiKey") apiKey: String = API_KEY
    ): Response<EverythingResponse>
}