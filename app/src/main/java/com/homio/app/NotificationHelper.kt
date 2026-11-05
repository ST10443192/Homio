package com.homio.app

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {

    const val CHANNEL_REQUESTS = "maintenance_requests"
    const val CHANNEL_MESSAGES = "messages"
    const val CHANNEL_PAYMENTS = "payments"
    const val ID_MESSAGES = 1001 // fixed id: a new message replaces the previous one

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val ctx = LocaleHelper.applyLocale(context)
        val manager = ctx.getSystemService(NotificationManager::class.java)

        val requests = NotificationChannel(
            CHANNEL_REQUESTS,
            ctx.getString(R.string.notif_channel_requests_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = ctx.getString(R.string.notif_channel_requests_desc) }

        val messages = NotificationChannel(
            CHANNEL_MESSAGES,
            ctx.getString(R.string.notif_channel_messages_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = ctx.getString(R.string.notif_channel_messages_desc) }

        val payments = NotificationChannel(
            CHANNEL_PAYMENTS,
            ctx.getString(R.string.notif_channel_payments_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = ctx.getString(R.string.notif_channel_payments_desc) }

        manager.createNotificationChannel(requests)
        manager.createNotificationChannel(messages)
        manager.createNotificationChannel(payments)
    }

    @SuppressLint("MissingPermission")
    fun show(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String,
        target: Class<*>
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return // user denied permission

        val intent = Intent(context, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.homio_purple))
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(notificationId, notification)
    }
}