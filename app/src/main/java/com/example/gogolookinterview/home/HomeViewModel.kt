package com.example.gogolookinterview.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.map
import com.example.gogolookinterview.model.ImageHit
import com.example.gogolookinterview.paging.SearchPagingModel
import com.example.gogolookinterview.repository.SearchRepository
import com.example.gogolookinterview.room.SearchHistory
import com.example.gogolookinterview.room.SearchHistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(
    private val searchRepository: SearchRepository,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    val searchListFlow = searchRepository.getSearchListPager().flow.map { pagingData ->
        pagingData.map { imageHit ->
            transFormToSearchPagingModel(imageHit)
        }
    }.cachedIn(viewModelScope)

    private var currentQuery: String? = null
    private val searchSuggestions = mutableListOf<String>()
    private val displayLayout = mutableStateOf<DisplayLayout>(DisplayLayout.List)

    var uiState by mutableStateOf(UiState(
        displayLayout = displayLayout,
        searchSuggestions = searchSuggestions
    ))
        private set

    sealed class DisplayLayout(val spanCount: Int) {
        object List : DisplayLayout(1)
        object Grid : DisplayLayout(2)
    }

    init {
        initSearchSuggestionsFromDao()
    }

    private fun setQuery(query: String? = null) {
        searchRepository.setQuery(query)
    }

    private fun transFormToSearchPagingModel(imageHit: ImageHit): SearchPagingModel =
        SearchPagingModel.SearchPhotoUi(imageHit)

    private fun initSearchSuggestionsFromDao() {
        viewModelScope.launch(Dispatchers.IO) {
            searchSuggestions.addAll(searchHistoryDao.getAll().map { it.searchHistory })
        }
    }

    fun submitQuery(query: String? = null, searchListItems: LazyPagingItems<SearchPagingModel>) {
        if (currentQuery == query) return
        currentQuery = query
        setQuery(query)
        searchListItems.refresh()
        query.takeUnless { it.isNullOrBlank() }?.let {
            appendSearchSuggestionIfNeed(it)
        }
    }

    private fun appendSearchSuggestionIfNeed(query: String) {
        if (searchSuggestions.contains(query)) return
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryDao.insertAll(SearchHistory(null, query))
        }
        searchSuggestions.add(query)
    }

    fun changeDisplayLayout(layout: DisplayLayout) {
        displayLayout.value = layout
    }
}