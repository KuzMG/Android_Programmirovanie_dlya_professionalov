package com.example.photogallery.api

import okhttp3.Interceptor
import okhttp3.Response

private const val API_KEY = "c6f5511d7d9ccc5c34fd756b9effcdaf"

class PhotoInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val newURl = originRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format","json")
            .addQueryParameter("nojsoncallback","1")
            .addQueryParameter("extras","url_s")
            .addQueryParameter("savesearch","1")
            .build()
        val newRequest =originRequest.newBuilder()
            .url(newURl)
            .build()
        return chain.proceed(newRequest)
    }
}