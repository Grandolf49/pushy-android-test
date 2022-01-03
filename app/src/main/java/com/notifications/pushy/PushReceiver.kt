package com.notifications.pushy

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import me.pushy.sdk.Pushy

/**
 * Created by grandolf49 on 03-01-2022
 */
class PushReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Attempt to extract the "title" property from the data payload, or fallback to app shortcut label
        val notificationTitle =
            if (intent.getStringExtra("title") != null) intent.getStringExtra("title") else context.packageManager.getApplicationLabel(
                context.applicationInfo
            ).toString()

        // Attempt to extract the "message" property from the data payload: {"message":"Hello World!"}
        var notificationText =
            if (intent.getStringExtra("message") != null) intent.getStringExtra("message") else "Test notification"

        // Prepare a notification with vibration, sound and lights
        val builder = NotificationCompat.Builder(context)
            .setAutoCancel(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setLights(Color.RED, 1000, 1000)
            .setVibrate(longArrayOf(0, 400, 250, 400))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        // Automatically configure a Notification Channel for devices running Android O+
        Pushy.setNotificationChannel(builder, context)

        // Get an instance of the NotificationManager service
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification and display it
        notificationManager.notify(1, builder.build())

        Log.d("Pushy", "onReceive: Is pushy connected? ${Pushy.isConnected()}")
    }
}
