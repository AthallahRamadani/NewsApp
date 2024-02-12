package com.athallah.newsapp.data.datasource.remote.api.request

data class EverythingQuery(
    val search: String? = null,
    val page: Int? = null,
    val limit: Int? = null
)
