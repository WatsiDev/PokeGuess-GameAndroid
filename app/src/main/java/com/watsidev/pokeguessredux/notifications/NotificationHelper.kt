package com.watsidev.pokeguessredux.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.watsidev.pokeguessredux.MainActivity
import com.watsidev.pokeguessredux.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_DAILY_ID = "daily_pokemon_channel"
        const val CHANNEL_STREAK_ID = "streak_reminder_channel"
        const val NOTIFICATION_DAILY_ID = 1001
        const val NOTIFICATION_STREAK_ID = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_ID,
                context.getString(R.string.notification_channel_daily),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_daily_desc)
            }

            val streakChannel = NotificationChannel(
                CHANNEL_STREAK_ID,
                context.getString(R.string.notification_channel_streak),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_streak_desc)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(streakChannel)
        }
    }

    fun showDailyNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_DAILY_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_daily_title))
            .setContentText(context.getString(R.string.notification_daily_body))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_daily_body))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_DAILY_ID, notification)
        } catch (e: SecurityException) {
            // Permission missing or denied
        }
    }

    fun showStreakReminderNotification(currentStreak: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_STREAK_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bodyText = if (currentStreak > 0) {
            context.getString(R.string.notification_streak_body, currentStreak)
        } else {
            context.getString(R.string.notification_streak_body_no_streak)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_streak_title))
            .setContentText(bodyText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bodyText)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_STREAK_ID, notification)
        } catch (e: SecurityException) {
            // Permission missing or denied
        }
    }
}
