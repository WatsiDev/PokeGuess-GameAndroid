package com.watsidev.pokeguessredux.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun updateWidget() {
        try {
            StreakGlanceWidget().updateAll(context)
        } catch (e: Exception) {
            // Ignore widget update errors
        }
    }
}
