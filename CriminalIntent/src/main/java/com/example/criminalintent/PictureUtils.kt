package com.example.criminalintent

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build

fun getScaledBitmap(path: String, destWidth: Int, destHeight: Int): Bitmap {
    var options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)

    val srcWidth = options.outWidth.toFloat()
    val srcHeight = options.outHeight.toFloat()

    var inSampleSize = 1
    if (srcHeight > destHeight || srcWidth > destHeight) {
        val heightScale = srcHeight / destHeight
        val widthScale = srcWidth / destWidth
        val sampleScale = if (heightScale > widthScale) heightScale else widthScale

        inSampleSize = Math.round(sampleScale)
    }
    options.inSampleSize = inSampleSize
    options.inJustDecodeBounds = false

    return BitmapFactory.decodeFile(path, options)
}

fun getScaledBitmap(path: String, activity: Activity): Bitmap {
    var width: Int
    var height: Int
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        width = activity.windowManager.currentWindowMetrics.bounds.width()
        height = activity.windowManager.currentWindowMetrics.bounds.height()
    } else {
        val size = Point()
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getSize(size)
        width = size.x
        height = size.y
    }

    return getScaledBitmap(path, width, height)
}