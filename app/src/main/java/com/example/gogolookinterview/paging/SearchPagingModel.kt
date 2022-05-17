package com.example.gogolookinterview.paging

import com.example.gogolookinterview.model.ImageHit

sealed class SearchPagingModel {
    data class SearchPhotoUi(val imageHit: ImageHit): SearchPagingModel()
}