package com.athallah.newsapp.di

import com.athallah.newsapp.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val vmModule = module {
    viewModel { MainViewModel(get()) }
}