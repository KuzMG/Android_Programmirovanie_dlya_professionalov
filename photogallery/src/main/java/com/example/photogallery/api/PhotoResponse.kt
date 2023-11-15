package com.example.photogallery.api

import com.example.photogallery.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse(val galleryItems: List<GalleryItem>,val pages : Int)