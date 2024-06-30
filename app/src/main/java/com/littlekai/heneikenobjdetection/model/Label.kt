package com.littlekai.heneikenobjdetection.model

import android.graphics.RectF

data class Label(
    var name: String,
    val boundingBox: RectF,
    val score: Float
)