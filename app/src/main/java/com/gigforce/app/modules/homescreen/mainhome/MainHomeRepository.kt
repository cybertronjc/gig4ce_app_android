package com.gigforce.app.modules.homescreen.mainhome

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class MainHomeRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String {
        return "alloted_gigs_vol2"
    }

}

