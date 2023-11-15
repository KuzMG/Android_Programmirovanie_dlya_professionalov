package com.example.photogallery

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T>(
    memory: Int,
    private val responseHandler: Handler,
    private val onThumbnailDownloader: (T, Bitmap) -> Unit
) : HandlerThread(TAG), DefaultLifecycleObserver {
    private val cache: LruCache<String, Bitmap>

    init {
        cache = LruCache(memory)
    }
    val fragmentLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            start()
            requestHandler = Handler(looper) { msg ->
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
                return@Handler true
            }
            Log.i(TAG, "Starting background thread")
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            quit()
            Log.i(TAG, "Destroying background thread")
        }
    }

    private var hasQuit = false
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickerFetchr = FlickerFetchr()
    private lateinit var requestHandler: Handler

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a Url: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return
        var bitmap:Bitmap
        if (cache[url]==null) {
            bitmap = flickerFetchr.fetchPhoto(url) ?: return
            cache.put(url, bitmap)
        }
        else{
            bitmap = cache[url]
        }
        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloader(target, bitmap)

        })
    }

    fun clearing() {
        Log.i(TAG, "Clearing all requests from queue")
        requestHandler.removeMessages(MESSAGE_DOWNLOAD)
        requestMap.clear()
    }
}