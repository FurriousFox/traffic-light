package com.leekleak.trafficlight.integrations

import android.telephony.SubscriptionInfo
import com.leekleak.shizukuintegration.ShizukuHelper
import com.leekleak.trafficlight.BuildConfig
import com.leekleak.trafficlight.database.DataPlanDao
import com.leekleak.trafficlight.database.DataPlanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ShizukuServicesProviderImpl(
    private val dataPlanDao: DataPlanDao,
    private val dataPlanRepository: DataPlanRepository,
    private val scope: CoroutineScope,
): ShizukuServicesProvider {

    val shizukuHelper = ShizukuHelper(
        BuildConfig.APPLICATION_ID,
        BuildConfig.DEBUG,
        BuildConfig.VERSION_CODE
    ) {
        updateSimData()
    }

    private fun getSubscriptionInfos(): List<SubscriptionInfo> = shizukuHelper.getSubscriptionInfos()

    private fun getSubscriberID(subscriptionId: Int): String? = shizukuHelper.getSubscriberID(subscriptionId)

    override fun updateSimData() {
        scope.launch {
            val infos = getSubscriptionInfos().sortedBy { it.simSlotIndex }
            val activeSubscriberIDs = infos.map { getSubscriberID(it.subscriptionId) }
            val plans = dataPlanDao.getAll().map { plan ->
                plan.copy(simIndex = activeSubscriberIDs.indexOf(plan.decryptedID))
            }
            activeSubscriberIDs.forEachIndexed { index, activeID ->
                if (activeID !in plans.map { it.decryptedID } && activeID != null) {
                    dataPlanRepository.savePlan(
                        activeID,
                        infos[index].simSlotIndex,
                        infos[index].carrierName?.toString() ?: ""
                    )
                }
            }
            dataPlanDao.addAll(plans)
        }
    }

    override fun shizukuRunning(): Boolean = shizukuHelper.shizukuRunning()
    override fun shizukuPermission(): Int = shizukuHelper.shizukuPermission()
    override fun shizukuRequestPermission(): Unit = shizukuHelper.shizukuRequestPermission()

    override fun enable() {
        shizukuHelper.bind()
    }

    override fun disable() {
        shizukuHelper.unbind()
    }
}