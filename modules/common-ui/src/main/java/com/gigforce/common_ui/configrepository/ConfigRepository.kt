package com.gigforce.common_ui.configrepository

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository

class ConfigRepository : BaseFirestoreDBRepository() {
    var COLLECTION_NAME = "Configuration"
    var CUSTOMUID = "N9EzW0SOAhOLVI1oA9Pu"


    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    override fun getCustomUid(): String? {
        return CUSTOMUID //To change body of created functions use File | Settings | File Templates.
    }

    fun getForceUpdateCurrentVersion(latestAPPUpdateListener: LatestAPPUpdateListener) {
        getCollectionReference().whereEqualTo("app_update_config", true).get()
            .addOnSuccessListener {
                runCatching {
                    val appUpdates = it.toObjects(LatestAPPUpdateModel::class.java)
                    appUpdates.get(0)?.let {
                        latestAPPUpdateListener.getCurrentAPPVersion(it)
                    }
                }.onSuccess {

                }.onFailure {

                }

            }
    }

    interface LatestAPPUpdateListener {
        fun getCurrentAPPVersion(latestAPPUpdateModel: LatestAPPUpdateModel)
    }

    class LatestAPPUpdateModel {
        var active: Boolean = false
        lateinit var force_update_current_version: String
        var force_update_required: Boolean = false
    }

}