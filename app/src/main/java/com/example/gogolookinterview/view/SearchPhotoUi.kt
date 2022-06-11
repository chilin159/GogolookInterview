package com.example.gogolookinterview.view

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gogolookinterview.R
import com.example.gogolookinterview.model.ImageHit
import com.example.gogolookinterview.paging.SearchPagingModel
import com.example.gogolookinterview.theme.AppTheme
import com.example.gogolookinterview.theme.darkColors
import com.example.gogolookinterview.theme.lightColors

@Composable
fun SearchPhotoUi(searchPhotoUi: SearchPagingModel.SearchPhotoUi) {
    val imageHit = searchPhotoUi.imageHit
    AppTheme {
        Column {
            Text(
                text = imageHit.user,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.h1,
                color = AppTheme.colors.textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageHit.imageURL)
                    .build(),
                contentDescription = imageHit.tags,
                contentScale = ContentScale.FillBounds,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = imageHit.imageWidth / imageHit.imageHeight.toFloat())
                    .clip(RoundedCornerShape(4.dp))
                    .animateContentSize()
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
private fun PreviewSearchPhotoUi() {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val colors = if (darkTheme) darkColors() else lightColors()
    AppTheme(colors) {
        SearchPhotoUi(
            SearchPagingModel.SearchPhotoUi(
                ImageHit(
                    id = 0,
                    type = "type",
                    tags = "tags",
                    imageURL = "",
                    imageWidth = 400,
                    imageHeight = 300,
                    previewURL = "",
                    previewHeight = 400,
                    previewWidth = 300,
                    user = "user name",
                    userImageURL = ""
                )
            )
        )
    }
}