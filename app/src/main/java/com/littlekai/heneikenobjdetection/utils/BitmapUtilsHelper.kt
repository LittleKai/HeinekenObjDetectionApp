package com.littlekai.heneikenobjdetection.utils

import android.graphics.Bitmap

class BitmapUtilsHelper {
    companion object {
        fun resize640Bitmap(inputBitmap: Bitmap, maxDimension: Int): Bitmap {
//            val maxDimension = 640
            val width = inputBitmap.width
            val height = inputBitmap.height

            val scaleFactor = if (width > height) {
                maxDimension.toFloat() / width
            } else {
                maxDimension.toFloat() / height
            }

            val newWidth = (width * scaleFactor).toInt()
            val newHeight = (height * scaleFactor).toInt()

            return Bitmap.createScaledBitmap(inputBitmap, newWidth, newHeight, true)
        }
    }
}