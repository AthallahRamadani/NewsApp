package com.athallah.newsapp.data.model

data class News(
    val articles: List<ArticlesItem>,
    val status: String,
    val totalResults: Int
)
data class ArticlesItem(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
){
    data class Source(
        val id: String?,
        val name: String
    )
}




