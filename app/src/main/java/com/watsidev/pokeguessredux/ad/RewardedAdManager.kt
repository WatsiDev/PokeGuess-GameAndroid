package com.watsidev.pokeguessredux.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private val adUnitId = "ca-app-pub-9489490067134108/5667420297" // Test: ca-app-pub-3940256099942544/5224354917

    private val _isAdAvailable = MutableStateFlow(false)
    val isAdAvailable: StateFlow<Boolean> = _isAdAvailable.asStateFlow()

    init {
        loadAd()
    }

    fun loadAd() {
        if (isLoading || rewardedAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "Ad failed to load: ${adError.message}")
                rewardedAd = null
                isLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                isLoading = false
                _isAdAvailable.value = true
            }
        })
    }

    fun showAd(activity: Activity, onRewardEarned: () -> Unit) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed.")
                rewardedAd = null
                _isAdAvailable.value = false
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "Ad failed to show: ${adError.message}")
                rewardedAd = null
                _isAdAvailable.value = false
                loadAd()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed.")
            }
        }

        rewardedAd?.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned()
        } ?: run {
            Log.d(TAG, "The rewarded ad wasn't ready yet.")
            loadAd()
        }
    }

    fun isAdAvailable(): Boolean = rewardedAd != null

    companion object {
        private const val TAG = "RewardedAdManager"
    }
}
