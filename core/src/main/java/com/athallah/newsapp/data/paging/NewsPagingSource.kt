package com.athallah.newsapp.data.paging

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.athallah.newsapp.data.datasource.remote.api.response.ArticlesItemEverything
import com.athallah.newsapp.data.datasource.remote.api.service.ApiService
import com.athallah.newsapp.data.utils.toEverything
import java.io.IOException

class NewsPagingSource(
    private val apiService: ApiService,
    private val query: String
) : PagingSource<Int, ArticlesItemEverything>() {
    override fun getRefreshKey(state: PagingState<Int, ArticlesItemEverything>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticlesItemEverything> {
        val pageNumber = params.key ?: 1

        return try {
            val response = apiService.getEverything(query, pageNumber)
            val data = response.body()?.articles ?: emptyList()

            LoadResult.Page(
                data = data,
                prevKey = if (pageNumber == 1) null else pageNumber - 1,
                nextKey = if (data.isEmpty()) null else pageNumber + 1
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }
}