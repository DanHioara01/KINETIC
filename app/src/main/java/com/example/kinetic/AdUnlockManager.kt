package com.example.kinetic

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Wrapper around AdMob Rewarded Ads. Free users watch a rewarded ad to temporarily
 * unlock a premium feature (see [BillingProducts.AD_UNLOCK_DURATION_MS]).
 *
 * NOTE: [AD_UNIT_ID] is the Google test rewarded ad unit. Replace with your real unit for release.
 */
class AdUnlockManager(private val appContext: Context) {

    private var rewardedAd: RewardedAd? = null
    @Volatile private var isLoading = false
    @Volatile private var initialized = false

    fun initialize() {
        if (initialized) return
        initialized = true
        MobileAds.initialize(appContext) {}
        loadAd()
    }

    fun loadAd(onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedAd != null) {
            onLoaded?.invoke()
            return
        }
        isLoading = true
        RewardedAd.load(
            appContext,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

        fun isAdReady(): Boolean = rewardedAd != null

        /**
         * Shows the rewarded ad. [onRewarded] fires only when the user completes the ad
         * (earns the reward). Preloads the next ad on dismiss.
         */
        fun showAd(activity: Activity, onRewarded: () -> Unit, onUnavailable: () -> Unit = {}) {
            val ad = rewardedAd
            if (ad == null) {
                onUnavailable()
                loadAd()
                return
            }
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    rewardedAd = null
                    loadAd()
                    onUnavailable()
                }
            }
            ad.show(activity, OnUserEarnedRewardListener { onRewarded() })
        }

        companion object {
        private val AD_UNIT_ID: String get() = com.example.kinetic.BuildConfig.ADMOB_REWARDED_AD_UNIT_ID
    }
}
