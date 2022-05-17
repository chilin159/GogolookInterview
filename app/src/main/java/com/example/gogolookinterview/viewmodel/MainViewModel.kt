package com.example.gogolookinterview.viewmodel

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.gogolookinterview.model.ImageHit
import com.example.gogolookinterview.paging.SearchPagingModel
import com.example.gogolookinterview.repository.SearchRepository
import kotlinx.coroutines.flow.map

class MainViewModel(app: Application, private val searchRepository: SearchRepository): AndroidViewModel(app) {

    fun getSearchListFlow() = searchRepository.getSearchListPager().flow.map { pagingData ->
        pagingData.map { imageHit ->
            transFormToSearchPagingModel(imageHit)
        }
    }.cachedIn(viewModelScope)

    @VisibleForTesting
    fun transFormToSearchPagingModel(imageHit: ImageHit): SearchPagingModel = SearchPagingModel.SearchPhotoUi(imageHit)

}