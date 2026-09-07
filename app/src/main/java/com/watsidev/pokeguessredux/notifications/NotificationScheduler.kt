package com.watsidev.pokeguessredux.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    const val WORK_DAILY_NAME = "daily_pokemon_notification_work"
    const val WORK_STREAK_NAME = "streak_reminder_notification_work"

    fun scheduleDailyNotifications(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Schedule Daily Notification at 09:00 AM
        val delayDaily = calculateInitialDelay(targetHour = 9, targetMinute = 0)
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyNotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayDaily, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_DAILY_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            dailyWorkRequest
        )

        // Schedule Streak Reminder at 20:00 PM (8 PM)
        val delayStreak = calculateInitialDelay(targetHour = 20, targetMinute = 0)
        val streakWorkRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayStreak, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_STREAK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            streakWorkRequest
        )
    }

    fun cancelDailyNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_DAILY_NAME)
    }

    fun cancelStreakNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_STREAK_NAME)
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        return dueDate.timeInMillis - currentDate.timeInMillis
    }
}
