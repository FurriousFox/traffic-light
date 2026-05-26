package com.leekleak.trafficlight.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.leekleak.trafficlight.BuildConfig
import com.leekleak.trafficlight.ui.theme.Theme
import com.leekleak.trafficlight.util.valueOfOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.appPreferences: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppPreferenceRepo (
    private val context: Context,
) {
    private val dataStore get() = context.appPreferences
    private val data get() = dataStore.data

    val notification: Flow<Boolean> = data.map { it[NOTIFICATION] ?: false }.distinctUntilChanged()
    suspend fun setNotification(value: Boolean) = dataStore.edit { it[NOTIFICATION] = value }

    val modeAOD: Flow<Boolean> = data.map { it[MODE_AOD] ?: false }.distinctUntilChanged()
    suspend fun setModeAOD(value: Boolean) = dataStore.edit { it[MODE_AOD] = value }

    val speedBits: Flow<Boolean> = data.map { it[SPEED_BITS] ?: false }.distinctUntilChanged()
    suspend fun setSpeedBits(value: Boolean) = dataStore.edit { it[SPEED_BITS] = value }

    val separateUpDown: Flow<Boolean> = data.map { it[SEPARATE_UP_DOWN] ?: false }.distinctUntilChanged()
    suspend fun setSeparateUpDown(value: Boolean) = dataStore.edit { it[SEPARATE_UP_DOWN] = value }

    val forceFallback: Flow<Boolean> = data.map { it[FORCE_FALLBACK] ?: false }.distinctUntilChanged()
    suspend fun setForceFallback(value: Boolean) = dataStore.edit { it[FORCE_FALLBACK] = value }

    val altVpn: Flow<Boolean> = data.map { it[ALT_VPN_WORKAROUND] ?: false }.distinctUntilChanged()
    suspend fun setAltVpn(value: Boolean) = dataStore.edit { it[ALT_VPN_WORKAROUND] = value }

    val liveNotification: Flow<Boolean> = data.map { it[LIVE_NOTIFICATION] ?: false }.distinctUntilChanged()
    suspend fun setLiveNotification(value: Boolean) = dataStore.edit { it[LIVE_NOTIFICATION] = value }

    val speedChannelSwitch: Flow<Boolean> = data.map { it[SPEED_CHANNEL_SWITCH] ?: false }.distinctUntilChanged()
    suspend fun setSpeedChannelSwitch(value: Boolean) = dataStore.edit { it[SPEED_CHANNEL_SWITCH] = value }

    val speedChannelThreshold: Flow<Long> =
        data.map { it[SPEED_CHANNEL_THRESHOLD] ?: DEFAULT_SPEED_CHANNEL_THRESHOLD_BYTES }.distinctUntilChanged()
    suspend fun setSpeedChannelThreshold(value: Long) = dataStore.edit { it[SPEED_CHANNEL_THRESHOLD] = value }

    val autoLiveNotification: Flow<Boolean> = data.map { it[AUTO_LIVE_NOTIFICATION] ?: false }.distinctUntilChanged()
    suspend fun setAutoLiveNotification(value: Boolean) = dataStore.edit { it[AUTO_LIVE_NOTIFICATION] = value }

    val autoLiveThreshold: Flow<Long> =
        data.map { it[AUTO_LIVE_THRESHOLD] ?: DEFAULT_AUTO_LIVE_THRESHOLD_BYTES }.distinctUntilChanged()
    suspend fun setAutoLiveThreshold(value: Long) = dataStore.edit { it[AUTO_LIVE_THRESHOLD] = value }
    
    val theme: Flow<Theme> = data.map { prefs -> prefs[THEME]?.let { valueOfOrNull<Theme>(it) } ?: Theme.AutoMaterial }.distinctUntilChanged()
    suspend fun setTheme(value: Theme) = dataStore.edit { it[THEME] = value.name }

    val shizukuTracking: Flow<Boolean> = data.map { it[SHIZUKU_TRACKING] ?: false }.distinctUntilChanged()
    suspend fun setShizukuTracking(value: Boolean) = dataStore.edit { it[SHIZUKU_TRACKING] = value }

    val shizukuHint: Flow<Boolean> = data.map { it[SHIZUKU_HINT] ?: BuildConfig.SHIZUKU }.distinctUntilChanged()
    suspend fun setShizukuHint(value: Boolean) = dataStore.edit { it[SHIZUKU_HINT] = value }

    val ads: Flow<Boolean> = data.map { it[ADS] ?: false }.distinctUntilChanged()
    suspend fun setAds(value: Boolean) = dataStore.edit { it[ADS] = value }

    internal companion object {
        internal const val DEFAULT_SPEED_CHANNEL_THRESHOLD_BYTES = 128L * 1024L
        internal const val DEFAULT_AUTO_LIVE_THRESHOLD_BYTES = 1024L * 1024L

        private val NOTIFICATION = booleanPreferencesKey("notification")
        private val LIVE_NOTIFICATION = booleanPreferencesKey("live_notification")
        private val SPEED_CHANNEL_SWITCH = booleanPreferencesKey("speed_channel_switch")
        private val SPEED_CHANNEL_THRESHOLD = longPreferencesKey("speed_channel_threshold")
        private val AUTO_LIVE_NOTIFICATION = booleanPreferencesKey("auto_live_notification")
        private val AUTO_LIVE_THRESHOLD = longPreferencesKey("auto_live_threshold")
        private val MODE_AOD = booleanPreferencesKey("mode_aod")
        private val SPEED_BITS = booleanPreferencesKey("speed_bits")
        private val SEPARATE_UP_DOWN = booleanPreferencesKey("separate_up_down")
        private val FORCE_FALLBACK = booleanPreferencesKey("force_fallback")
        private val ALT_VPN_WORKAROUND = booleanPreferencesKey("alt_vpn")
        private val THEME = stringPreferencesKey("theme")
        private val SHIZUKU_TRACKING = booleanPreferencesKey("shizuku_tracking")
        private val SHIZUKU_HINT = booleanPreferencesKey("shizuku_hint")
        private val ADS = booleanPreferencesKey("supporter_ads")
    }
}