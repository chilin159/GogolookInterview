package com.example.gogolookinterview.paging

import com.example.gogolookinterview.model.ImageHit

/**To support more view type in recyclerview if need*/
sealed class SearchPagingModel {
    data class SearchPhotoUi(val imageHit: ImageHit): SearchPagingModel()
}