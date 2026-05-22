package com.leekleak.trafficlight.integrations

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.leekleak.play_integration.AppReviewManager
import com.leekleak.trafficlight.BuildConfig
import com.leekleak.trafficlight.database.AppPreferenceRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PlayServicesProviderImpl(
    private val context: Context,
    private val appReviewManager: AppReviewManager,
    private val appPreferenceRepo: AppPreferenceRepo
): PlayServicesProvider {

    init {
        CoroutineScope(Dispatchers.IO).launch {
            appPreferenceRepo.ads.filter { it }.first()
            MobileAds.initialize(context, InitializationConfig.Builder(BuildConfig.ADMOB_APP_ID).build())
        }
    }

    override suspend fun onAppLaunch(activity: Activity) {
        appReviewManager.onAppLaunch(activity)
    }
}
