package com.example.photogallery

import com.example.photogallery.api.PhotoResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        val jsonObject = json?.asJsonObject
        val photos = jsonObject?.get("photos")?.asJsonObject
        val photo = photos?.get("photo")?.asJsonArray
        val typeToken = object : TypeToken<List<GalleryItem>>() {}.type
        val pages = photos?.get("pages")?.asInt ?: 0
        val galleryItems = context?.deserialize<List<GalleryItem>>(photo,typeToken) ?: emptyList()
        val photoResponse = PhotoResponse(galleryItems,pages)
        return photoResponse
    }
}