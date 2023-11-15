package com.example.photogallery

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope

private const val TAG = "PollWorker"

class PollWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        val query = QueryPreferences.getStoredQuery(applicationContext)
        val lastResultId = QueryPreferences.getLastResultId(applicationContext)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickerFetchr().fetchPhotoRequest(1).body()?.galleryItems
        } else {
            FlickerFetchr().searchPhotosRequest(1, query).body()?.galleryItems
        } ?: emptyList()
        if (items.isEmpty())
            return@coroutineScope Result.success()

        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got an new result: $resultId")
            QueryPreferences.setLastResultId(applicationContext, resultId)

            val intent = PhotoGalleryActivity.newIntent(applicationContext)
            val pendingIntent = PendingIntent.getActivity(
                applicationContext, 0, intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val resource = applicationContext.resources
            val notification =
                NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
//                    .setTicker(resource.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resource.getString(R.string.new_pictures_title))
                    .setContentText(resource.getString(R.string.new_pictures_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
            showBackgroundNotification(0, notification)
        }
        Result.success()
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }
        applicationContext.sendOrderedBroadcast(intent, PERM_PRIVATE)
    }

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.example.photogallery.SHOW_NOTIFICATION"
        const val PERM_PRIVATE = "com.example.photogallery.PRIVATE"
        const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
    }
}