package com.example.gogolookinterview.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.gogolookinterview.datasource.SearchDataSource
import com.example.gogolookinterview.paging.SearchPagingSource

class SearchRepository {
    private val dataSource = SearchDataSource()

    private fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(pageSize = 10)
    }

    fun getSearchListPager(pagingConfig: PagingConfig = getDefaultPageConfig()) = Pager(
        config = pagingConfig
    ) {
        SearchPagingSource(dataSource)
    }

    fun setQuery(query: String? = null) {
        dataSource.searchQuery = query
    }
}