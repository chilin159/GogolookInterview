package com.example.gogolookinterview.view

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.gogolookinterview.paging.SearchPagingModel

class SearchPagingAdapter: PagingDataAdapter<SearchPagingModel, RecyclerView.ViewHolder>(GagPagingDiffCallback())  {

    private val viewTypeNone = -1
    private val viewTypePhoto = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchPhotoViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val gagPagingModel = getItem(position) ?: return
        when(holder) {
            is SearchPhotoViewHolder -> holder.bindData((gagPagingModel as SearchPagingModel.SearchPhotoUi).imageHit)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is SearchPagingModel.SearchPhotoUi -> viewTypePhoto
            else -> viewTypeNone
        }
    }

    fun onSpanCountChange() {
        notifyItemRangeChanged(0, itemCount)
    }
}

class GagPagingDiffCallback: DiffUtil.ItemCallback<SearchPagingModel>() {
    override fun areItemsTheSame(oldItem: SearchPagingModel, newItem: SearchPagingModel): Boolean {
        return if (oldItem is SearchPagingModel.SearchPhotoUi && newItem is SearchPagingModel.SearchPhotoUi) {
            oldItem.imageHit.id == newItem.imageHit.id
        } else {
            false
        }
    }

    override fun areContentsTheSame(oldItem: SearchPagingModel, newItem: SearchPagingModel): Boolean {
        return oldItem == newItem
    }
}

