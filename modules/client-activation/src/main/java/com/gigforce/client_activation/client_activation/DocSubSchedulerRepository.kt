package com.gigforce.client_activation.client_activation

import com.gigforce.core.fb.BaseFirestoreDBRepository

class DocSubSchedulerRepository : BaseFirestoreDBRepository(){
    override fun getCollectionName(): String {
        return "JP_Applications"
    }


}