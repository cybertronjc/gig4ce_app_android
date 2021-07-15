package com.gigforce.lead_management.repositories

import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LeadManagementRepository {

    private val mutex = Mutex()

    suspend fun fetchJoinings() : List<Joining> {
        mutex.withLock {



            return emptyList()
        }
    }

}