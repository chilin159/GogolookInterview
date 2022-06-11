package com.example.gogolookinterview.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class AppTypography(
    val h1: TextStyle = TextStyle(
        fontSize = 24.sp
    ),
    val subtitle: TextStyle = TextStyle(
        fontSize = 16.sp
    ),
    val body: TextStyle = TextStyle(
        fontSize = 16.sp
    ),
    val button: TextStyle = TextStyle(
        fontSize = 16.sp
    ),
    val caption: TextStyle = TextStyle(
        fontSize = 12.sp
    )
)

internal val LocalTypography = staticCompositionLocalOf { AppTypography() }