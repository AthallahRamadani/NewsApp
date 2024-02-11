package com.athallah.newsapp.utils

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(publishedAt: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = inputFormat.parse(publishedAt)
    return outputFormat.format(date!!)
}