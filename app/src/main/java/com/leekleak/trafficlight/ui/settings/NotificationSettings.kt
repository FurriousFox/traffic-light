package com.leekleak.trafficlight.ui.settings

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.leekleak.trafficlight.R
import com.leekleak.trafficlight.database.AppPreferenceRepo
import com.leekleak.trafficlight.database.TrafficSnapshot
import com.leekleak.trafficlight.services.notifications.SpeedNotification.Companion.NOTIFICATION_CHANNEL_ID_DISCONNECTED
import com.leekleak.trafficlight.util.DataSize
import com.leekleak.trafficlight.util.PageTitle
import com.leekleak.trafficlight.util.categoryTitleSmall
import com.leekleak.trafficlight.util.openLink
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.math.roundToInt

private const val KB_IN_BYTES = 1024L
private const val SPEED_CHANNEL_MIN_KB = 32
private const val SPEED_CHANNEL_MAX_KB = 2048
private const val SPEED_CHANNEL_STEP_KB = 32
private const val AUTO_LIVE_MIN_KB = 128
private const val AUTO_LIVE_MAX_KB = 20480
private const val AUTO_LIVE_STEP_KB = 128

private fun sliderSteps(min: Int, max: Int, step: Int): Int = (max - min) / step - 1

private fun snapToStep(value: Float, min: Int, max: Int, step: Int): Int {
    val clamped = value.coerceIn(min.toFloat(), max.toFloat())
    val steps = ((clamped - min) / step).roundToInt()
    return (min + steps * step).coerceIn(min, max)
}

@Composable
fun NotificationSettings(paddingValues: PaddingValues) {
    val appPreferenceRepo: AppPreferenceRepo = koinInject()
    val viewModel: SettingsVM = koinViewModel()
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    val haptic = LocalHapticFeedback.current
    val speedBits by appPreferenceRepo.speedBits.collectAsState(false)
    val separateUpDown by appPreferenceRepo.separateUpDown.collectAsState(false)
    val liveNotification by viewModel.liveNotification.collectAsState()
    val speedChannelSwitch by appPreferenceRepo.speedChannelSwitch.collectAsState(false)
    val speedChannelThreshold by appPreferenceRepo.speedChannelThreshold
        .collectAsState(AppPreferenceRepo.DEFAULT_SPEED_CHANNEL_THRESHOLD_BYTES)
    val autoLiveNotification by appPreferenceRepo.autoLiveNotification.collectAsState(false)
    val autoLiveThreshold by appPreferenceRepo.autoLiveThreshold
        .collectAsState(AppPreferenceRepo.DEFAULT_AUTO_LIVE_THRESHOLD_BYTES)

    val speedChannelThresholdKb = (speedChannelThreshold / KB_IN_BYTES).toInt()
    val speedChannelValueKb = speedChannelThresholdKb
        .coerceIn(SPEED_CHANNEL_MIN_KB, SPEED_CHANNEL_MAX_KB)
    val autoLiveThresholdKb = (autoLiveThreshold / KB_IN_BYTES).toInt()
    val autoLiveValueKb = autoLiveThresholdKb
        .coerceIn(AUTO_LIVE_MIN_KB, AUTO_LIVE_MAX_KB)

    LazyColumn(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize()
            .hazeSource(hazeState),
        contentPadding = paddingValues
    ) {
        categoryTitleSmall { stringResource(R.string.appearance) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            item {
                Row (
                    modifier = Modifier.height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SwitchPreference(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.live_notification),
                        icon = painterResource(R.drawable.app_badging),
                        value = liveNotification,
                        enabled = !separateUpDown,
                        onValueChanged = { scope.launch { appPreferenceRepo.setLiveNotification(it) } }
                    )
                    IconPreference(
                        title = stringResource(R.string.help),
                        painter = painterResource(R.drawable.help),
                        onClick = { openLink(activity, "https://github.com/leekleak/traffic-light/wiki/Troubleshooting#notifications") },
                    )
                }
            }
        }
        item {
            SwitchPreference(
                title = stringResource(R.string.separate_upload_and_download),
                summary = null,
                icon = painterResource(R.drawable.speed_separate),
                value = separateUpDown,
                enabled = !liveNotification,
                onValueChanged = { scope.launch { appPreferenceRepo.setSeparateUpDown(it) } }
            )
        }
        item {
            SwitchPreference(
                title = stringResource(R.string.speed_in_bits),
                summary = null,
                icon = painterResource(R.drawable.speed),
                value = speedBits,
                onValueChanged = { scope.launch { appPreferenceRepo.setSpeedBits(it) } }
            )
        }

        categoryTitleSmall { stringResource(R.string.speed_thresholds) }
        item {
            SwitchPreference(
                title = stringResource(R.string.speed_channel_switch),
                summary = stringResource(R.string.speed_channel_switch_description),
                icon = painterResource(R.drawable.notification),
                value = speedChannelSwitch,
                onValueChanged = { scope.launch { appPreferenceRepo.setSpeedChannelSwitch(it) } }
            )
        }
        item {
            val speedLabel = DataSize(speedChannelValueKb.toLong() * KB_IN_BYTES)
                .toString(speed = true, inBits = speedBits)
            SliderPreference(
                title = stringResource(R.string.speed_channel_threshold),
                summary = stringResource(R.string.speed_channel_threshold_description),
                icon = painterResource(R.drawable.speed),
                value = speedChannelValueKb.toFloat(),
                valueLabel = speedLabel,
                valueRange = SPEED_CHANNEL_MIN_KB.toFloat()..SPEED_CHANNEL_MAX_KB.toFloat(),
                steps = sliderSteps(SPEED_CHANNEL_MIN_KB, SPEED_CHANNEL_MAX_KB, SPEED_CHANNEL_STEP_KB),
                enabled = speedChannelSwitch,
                onValueChanged = { newValue ->
                    val snapped = snapToStep(newValue, SPEED_CHANNEL_MIN_KB, SPEED_CHANNEL_MAX_KB, SPEED_CHANNEL_STEP_KB)
                    if (snapped != speedChannelValueKb) {
                        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                        scope.launch { appPreferenceRepo.setSpeedChannelThreshold(snapped.toLong() * KB_IN_BYTES) }
                    }
                }
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            item {
                SwitchPreference(
                    title = stringResource(R.string.auto_live_notification),
                    summary = stringResource(R.string.auto_live_notification_description),
                    icon = painterResource(R.drawable.app_badging),
                    value = autoLiveNotification,
                    enabled = !separateUpDown,
                    onValueChanged = { scope.launch { appPreferenceRepo.setAutoLiveNotification(it) } }
                )
            }
            item {
                val liveLabel = DataSize(autoLiveValueKb.toLong() * KB_IN_BYTES)
                    .toString(speed = true, inBits = speedBits)
                SliderPreference(
                    title = stringResource(R.string.auto_live_threshold),
                    summary = stringResource(R.string.auto_live_threshold_description),
                    icon = painterResource(R.drawable.speed),
                    value = autoLiveValueKb.toFloat(),
                    valueLabel = liveLabel,
                    valueRange = AUTO_LIVE_MIN_KB.toFloat()..AUTO_LIVE_MAX_KB.toFloat(),
                    steps = sliderSteps(AUTO_LIVE_MIN_KB, AUTO_LIVE_MAX_KB, AUTO_LIVE_STEP_KB),
                    enabled = autoLiveNotification && !separateUpDown,
                    onValueChanged = { newValue ->
                        val snapped = snapToStep(newValue, AUTO_LIVE_MIN_KB, AUTO_LIVE_MAX_KB, AUTO_LIVE_STEP_KB)
                        if (snapped != autoLiveValueKb) {
                            haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                            scope.launch { appPreferenceRepo.setAutoLiveThreshold(snapped.toLong() * KB_IN_BYTES) }
                        }
                    }
                )
            }
        }

        categoryTitleSmall { stringResource(R.string.behavior) }
        item {
            val modeAOD by appPreferenceRepo.modeAOD.collectAsState(false)
            SwitchPreference(
                title = stringResource(R.string.screen_off_update),
                summary = stringResource(R.string.screen_off_update_description),
                icon = painterResource(R.drawable.aod),
                value = modeAOD,
                onValueChanged = { scope.launch { appPreferenceRepo.setModeAOD(it) } }
            )
        }
        item {
            val altVpn by appPreferenceRepo.altVpn.collectAsState(false)
            SwitchPreference(
                title = stringResource(R.string.alt_vpn_workaround),
                summary = stringResource(R.string.alt_vpn_workaround_description),
                icon = painterResource(R.drawable.vpn),
                value = altVpn,
                onValueChanged = { scope.launch { appPreferenceRepo.setAltVpn(it) } }
            )
        }
        item {
            val forceFallback by appPreferenceRepo.forceFallback.collectAsState(false)
            val doesFallbackWork = remember { TrafficSnapshot.doesFallbackWork() }
            SwitchPreference(
                title = stringResource(R.string.force_fallback),
                summary = if (doesFallbackWork) stringResource(R.string.force_fallback_description)
                          else stringResource(R.string.fallback_unsupported),
                icon = painterResource(R.drawable.fallback),
                value = forceFallback,
                enabled = doesFallbackWork,
                onValueChanged = { scope.launch { appPreferenceRepo.setForceFallback(it) } }
            )
        }

        categoryTitleSmall { stringResource(R.string.notification_channels) }
        item {
            Row (
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavigatePreference(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.disconnected_from_network),
                    icon = painterResource(R.drawable.signal_disconnected),
                    onClick = { viewModel.openNotificationChannelSettings(activity, NOTIFICATION_CHANNEL_ID_DISCONNECTED) },
                )
                IconPreference(
                    title = stringResource(R.string.help),
                    painter = painterResource(R.drawable.help),
                    onClick = { openLink(activity, "https://github.com/leekleak/traffic-light/wiki/Hide-status-bar-icon-when-disconnected") },
                )
            }
        }
    }
    PageTitle (true, hazeState, stringResource(R.string.notifications))
}