package com.example.gogolookinterview.view

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.gogolookinterview.R
import com.example.gogolookinterview.model.ImageHit
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.holder_search_photo.view.*

class SearchPhotoViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.holder_search_photo, parent, false)
) {

    private val imageWidth = Resources.getSystem().displayMetrics.widthPixels

    fun bindData(imageHit: ImageHit) {
        val imageHeight = (imageWidth * (imageHit.imageHeight / imageHit.imageWidth.toFloat())).toInt()
        with(itemView) {
            title.text = imageHit.user
            image.initSize(imageWidth, imageHeight)
            imageHit.imageURL.takeUnless { it.isBlank() }?.let {
                image.setImageUrl(it, imageWidth, imageHeight)
            }
        }
    }

    private fun ImageView.initSize(width: Int, height: Int) {
        val params = layoutParams
        params.width = width
        params.height = height
        layoutParams = params
    }

    private fun ImageView.setImageUrl(url: String, width: Int, height: Int) {
        Picasso.get().apply {
            isLoggingEnabled = true
        }.load(url)
            .resize(width, height)
            .placeholder(R.color.black)
            .onlyScaleDown()
            .into(this)
    }
}