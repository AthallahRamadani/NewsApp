package com.athallah.newsapp.data.repository

import androidx.paging.PagingData
import com.athallah.newsapp.data.ResultState
import com.athallah.newsapp.data.model.ArticlesItem
import com.athallah.newsapp.data.model.ArticlesItemEverything
import com.athallah.newsapp.data.model.News
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getHeadline(country: String, category: String?): Flow<ResultState<List<ArticlesItem>>>
    fun getEverythingPaging(pageNumber: Int): Flow<PagingData<ArticlesItemEverything>>
}