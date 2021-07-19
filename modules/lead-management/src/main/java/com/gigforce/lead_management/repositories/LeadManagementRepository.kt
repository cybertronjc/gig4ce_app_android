package com.gigforce.lead_management.repositories

import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.GigForGigerActivation
import com.gigforce.common_ui.viewdatamodels.leadManagement.Joining
import com.gigforce.lead_management.ui.reference_check.ReferenceCheckViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LeadManagementRepository {

    private val mutex = Mutex()

    suspend fun fetchJoinings() : List<Joining> {
        mutex.withLock {



            return emptyList()
        }
    }

    suspend fun getGigsForReferral() : List<GigForGigerActivation>{

        return emptyList()
    }

    suspend fun saveReference(
        userUid : String,
        name: String,
        relation: String,
        contactNo: String
    ) {


    }
}