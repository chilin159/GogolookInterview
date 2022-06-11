package com.example.gogolookinterview.datasource

import com.example.gogolookinterview.api.ApiService
import com.example.gogolookinterview.api.getResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchDataSource {

    private val api get() = ApiService.create()
    var searchQuery: String? = null

    suspend fun getSearchImages(page: Int, perPage: Int) = withContext(Dispatchers.IO) {
        api.getSearchImages(query = searchQuery, page = page, perPage = perPage).getResult { it.imageHits }
    }
}