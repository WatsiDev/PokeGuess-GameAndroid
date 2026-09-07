package com.watsidev.pokeguessredux.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.watsidev.pokeguessredux.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StreakReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferencesRepository(applicationContext)
        val streakEnabled = userPreferences.streakNotificationsEnabled.first()

        if (streakEnabled) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastGuessDate = userPreferences.lastGuessDate.first()

            // If user has NOT completed today's guess yet
            if (lastGuessDate != today) {
                val currentStreak = userPreferences.currentStreak.first()
                val notificationHelper = NotificationHelper(applicationContext)
                notificationHelper.showStreakReminderNotification(currentStreak)
            }
        }

        return Result.success()
    }
}
