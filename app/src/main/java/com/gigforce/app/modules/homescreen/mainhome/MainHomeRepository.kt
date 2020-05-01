package com.gigforce.app.modules.homescreen.mainhome

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository

class MainHomeRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String {
        getDBCollection()
        return "alloted_gigs_vol2"

    }
}

