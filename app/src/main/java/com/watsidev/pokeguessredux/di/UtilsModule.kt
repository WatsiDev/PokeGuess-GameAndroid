package com.watsidev.pokeguessredux.di

import android.content.Context
import com.watsidev.pokeguessredux.ui.utils.AudioHelper
import com.watsidev.pokeguessredux.ui.utils.VibrationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideAudioHelper(@ApplicationContext context: Context): AudioHelper {
        return AudioHelper(context)
    }

    @Provides
    @Singleton
    fun provideVibrationHelper(@ApplicationContext context: Context): VibrationHelper {
        return VibrationHelper(context)
    }
}
