package com.notifications.pushy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import me.pushy.sdk.services.PushySocketService
import me.pushy.sdk.util.PushyLogger
import me.pushy.sdk.util.PushyServiceManager


/**
 * Created by grandolf49 on 05-01-2022
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        startForegroundService(context)
        PushyLogger.d("Device boot complete")
        PushyServiceManager.start(context)
    }

    private fun startForegroundService(context: Context) {
        Log.i("Pushy", "Starting foreground service to listen for notifications")
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        // Get app name as string
        val appName = context.packageManager.getApplicationLabel(context.applicationInfo).toString()
        val description = "Listening for notifications"

        // Android O and newer requires notification channels
        // to be created prior to dispatching a notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("pushy_ongoing", appName, NotificationManager.IMPORTANCE_MIN)
            channel.description = description

            // Register the channel with the system
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create foreground notification using pushy_ongoing notification channel (customize as necessary)
        val notification: Notification = NotificationCompat.Builder(context, "pushy_ongoing")
            .setContentTitle(appName)
            .setContentText(description)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .build()

        // Configure Pushy SDK to start a foreground service with this notification
        // Must be called before Pushy.listen();
        PushySocketService.setForegroundNotification(notification)
    }
}
