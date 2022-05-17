package com.example.gogolookinterview.utils

import android.content.res.Resources

fun dp(dp: Int) = (dp * Resources.getSystem().displayMetrics.density).toInt()