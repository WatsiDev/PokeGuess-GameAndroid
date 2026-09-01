package com.watsidev.pokeguessredux.di

import android.content.Context
import com.watsidev.pokeguessredux.ad.RewardedAdManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AdModule {

    @Provides
    @Singleton
    fun provideRewardedAdManager(@ApplicationContext context: Context): RewardedAdManager {
        return RewardedAdManager(context)
    }
}
