package com.example.gogolookinterview.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageHit(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("tags") val tags: String,
    @SerializedName("webformatURL") val imageURL: String,
    @SerializedName("webformatWidth") val imageWidth: Int,
    @SerializedName("webformatHeight") val imageHeight: Int,
    @SerializedName("previewURL") val previewURL: String,
    @SerializedName("previewWidth") val previewWidth: Int,
    @SerializedName("previewHeight") val previewHeight: Int,
    @SerializedName("user") val user: String,
    @SerializedName("userImageURL") val userImageURL: String
) : Parcelable