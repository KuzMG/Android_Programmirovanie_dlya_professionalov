package com.example.photogallery

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.photogallery.api.FlickrApi

class GalleryDataSource(val flickrApi: FlickrApi) : PagingSource<Int, GalleryItem>() {
    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        return try {
            val position = params.key ?: 1
            val response = flickrApi.fetchPhotos(position)
            val pages = response.body()?.pages ?: 0
            if (position > pages)
                return LoadResult.Error(Exception())
            val galleryItems = response.body()?.galleryItems?.filterNot { it.url.isBlank() } ?: emptyList()
            LoadResult.Page(
                data = galleryItems,
                prevKey = if (position == 1) null else position - 1,
                nextKey = position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}