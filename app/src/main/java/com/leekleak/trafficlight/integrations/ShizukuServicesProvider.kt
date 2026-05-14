package com.leekleak.trafficlight.integrations

import com.leekleak.trafficlight.database.DataPlanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface ShizukuServicesProvider {
    fun updateSimData()
    fun shizukuRunning(): Boolean
    fun shizukuPermission(): Int
    fun shizukuRequestPermission()
    fun enable()
    fun disable()
}

fun updateSimDataBasic(scope: CoroutineScope, dataPlanRepository: DataPlanRepository) {
    scope.launch {
        val dataPlanDao = dataPlanRepository.dao
        val plans = dataPlanDao.getAll()
        val newPlans = plans.map { it.copy(simIndex = if (it.decryptedID == null) 0 else -1) }
        if (plans.isEmpty()) {
            dataPlanRepository.savePlan(
                null,
                0,
                ""
            )
        }
        dataPlanDao.addAll(newPlans)
    }
}
