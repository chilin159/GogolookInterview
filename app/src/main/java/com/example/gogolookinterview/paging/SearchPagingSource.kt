package com.example.gogolookinterview.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.gogolookinterview.api.Result
import com.example.gogolookinterview.datasource.SearchDataSource
import com.example.gogolookinterview.model.ImageHit

class SearchPagingSource(private val dataSource: SearchDataSource) : PagingSource<Int, ImageHit>() {

    override suspend fun load(
        params: LoadParams<Int>
    ): LoadResult<Int, ImageHit> {
        try {
            val pagingData = mutableListOf<ImageHit>()
            val pageNumber = params.key ?: 1
            var nextPageNumber: Int? = null
            when(val result = dataSource.getSearchImages(page = pageNumber, perPage = params.loadSize)) {
                is Result.Success -> result.data.takeUnless { it.isNullOrEmpty() }?.let {
                    pagingData.addAll(it)
                    nextPageNumber = pageNumber + 1
                }
                is Result.Error -> {
                    return LoadResult.Error(result.exception)
                }
            }

            return LoadResult.Page(
                data = pagingData,
                prevKey = (pageNumber - 1).takeUnless { it <= 0 },
                nextKey = nextPageNumber
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ImageHit>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}