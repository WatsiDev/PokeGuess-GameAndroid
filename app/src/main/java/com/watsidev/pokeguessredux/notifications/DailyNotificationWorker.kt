package com.watsidev.pokeguessredux.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.watsidev.pokeguessredux.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class DailyNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userPreferences = UserPreferencesRepository(applicationContext)
        val dailyEnabled = userPreferences.dailyNotificationsEnabled.first()

        if (dailyEnabled) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showDailyNotification()
        }

        return Result.success()
    }
}
