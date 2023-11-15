package com.example.photogallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity


class PhotoGalleryActivity : AppCompatActivity(R.layout.activity_photo_gallery) {
    companion object {
        fun newIntent(context: Context) = Intent(context, PhotoGalleryActivity::class.java)
    }
}