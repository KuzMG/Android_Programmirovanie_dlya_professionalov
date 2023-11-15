package com.example.photogallery

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

private const val TAG = "NotificationReceiver"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context, p1: Intent) {
        Log.i(TAG, "received bresult: ${resultCode}")
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        val requestCode = p1.getIntExtra(PollWorker.REQUEST_CODE, 0)
        val notification: Notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                p1.getParcelableExtra(PollWorker.NOTIFICATION, Notification::class.java)!!
            } else {
                @Suppress("DEPRECATED")
                p1.getParcelableExtra(PollWorker.NOTIFICATION)!!
            }
        val notificationManager = NotificationManagerCompat.from(p0)

        if (ActivityCompat.checkSelfPermission(
                p0,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(requestCode, notification)
        }

    }
}