package com.example.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.photogallery.api.FlickrApi
import com.example.photogallery.api.PhotoInterceptor
import com.example.photogallery.api.PhotoResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.InputStream

private const val TAG = "FlickrFetchr"

class FlickerFetchr {
    private val flickrApi: FlickrApi

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(PhotoInterceptor())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(buildGsonConverterFactory())
            .client(client)
            .build()
        flickrApi = retrofit.create()
    }

    private fun buildGsonConverterFactory(): GsonConverterFactory {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
        return GsonConverterFactory.create(gsonBuilder.create())
    }

    fun fetchPhotos(): LiveData<PagingData<GalleryItem>> {

        return Pager(
            config = PagingConfig(
                pageSize = 1
            )
        ) {
            GalleryDataSource(flickrApi)
        }.liveData
    }
    suspend fun fetchPhotoRequest(page: Int): Response<PhotoResponse>{
        return flickrApi.fetchPhotos(page)
    }
    suspend fun searchPhotosRequest(page: Int,query: String): Response<PhotoResponse>{
        return flickrApi.searchPhotos(query,page)
    }

    fun searchPhotos(query: String): LiveData<PagingData<GalleryItem>>{
        return Pager(
            config = PagingConfig(
                pageSize = 2
            )
        ) {
            SearchDataSource(flickrApi,query)
        }.liveData
    }
    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}