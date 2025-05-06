package com.omer.stegochat

import android.graphics.Bitmap

data class EncodeResult(
    val bitmap: Bitmap,
    val chaosIndices: List<Int>
) 