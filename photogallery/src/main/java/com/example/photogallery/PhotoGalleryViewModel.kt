package com.example.photogallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import retrofit2.http.Query

class PhotoGalleryViewModel(private val app:Application) : AndroidViewModel(app) {
    val galleryItemLiveData: LiveData<PagingData<GalleryItem>>
    private val mutableSearchTerm = MutableLiveData<String>()
    private val flickerFetchr: FlickerFetchr
    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""
    init {
        flickerFetchr = FlickerFetchr()
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)
        galleryItemLiveData = mutableSearchTerm.switchMap { searchTerm ->
            if(searchTerm.isBlank()){
                flickerFetchr.fetchPhotos().cachedIn(this)
            }else{
                flickerFetchr.searchPhotos(searchTerm).cachedIn(this)
            }
        }
    }

    fun fetchPhotos(query: String = ""){
        QueryPreferences.setStoredQuery(app,query)
        mutableSearchTerm.value = query
    }


}