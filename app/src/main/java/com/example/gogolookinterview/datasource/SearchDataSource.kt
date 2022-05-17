package com.example.gogolookinterview.datasource

import com.example.gogolookinterview.api.ApiService
import com.example.gogolookinterview.api.getResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchDataSource {

    private val api get() = ApiService.create()

    suspend fun getSearchImages(page: Int, perPage: Int) = withContext(Dispatchers.IO) {
        api.getSearchImages(page = page, perPage = perPage).getResult { it.imageHits }
    }

}