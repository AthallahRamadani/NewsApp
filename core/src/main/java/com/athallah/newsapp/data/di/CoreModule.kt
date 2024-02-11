package com.athallah.newsapp.data.di

import com.athallah.newsapp.data.datasource.remote.api.service.ApiService
import com.athallah.newsapp.data.repository.NewsRepository
import com.athallah.newsapp.data.repository.NewsRepositoryImpl
import com.athallah.newsapp.data.utils.Constant
import com.athallah.newsapp.data.utils.Constant.Companion.BASE_URL
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.dsl.module

val repositoryModule = module {
    single { NewsRepositoryImpl(get()) } bind NewsRepository::class
}

val apiModule = module {
    single<Retrofit> {
        return@single Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
    }

    single {
        OkHttpClient.Builder().apply {
            addInterceptor(get<ChuckerInterceptor>())
        }.build()
    }

    single {
        ChuckerInterceptor.Builder(androidApplication())
            .collector(ChuckerCollector(androidApplication()))
            .maxContentLength(250000L)
            .redactHeaders(emptySet())
            .alwaysReadResponseBody(false)
            .build()
    }

    single<ApiService> {
        get<Retrofit>().create(ApiService::class.java)
    }
}