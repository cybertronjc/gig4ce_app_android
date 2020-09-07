package com.gigforce.app.utils.configrepository

import androidx.lifecycle.MutableLiveData
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.gigPage.models.Gig
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ConfigRepository : BaseFirestoreDBRepository {
    var COLLECTION_NAME = "Configuration";
    var CUSTOMUID = "N9EzW0SOAhOLVI1oA9Pu"

    companion object {
        //        private var configrepositoryObj: ConfigRepository? = null;
        private var documentReference: DocumentReference? = null
//        fun getInstance(): ConfigRepository? {
//            if (configrepositoryObj == null) configrepositoryObj =
//                ConfigRepository()
//
//            return configrepositoryObj
//        }
    }

    var configLiveDataModel: MutableLiveData<ConfigDataModel> = MutableLiveData<ConfigDataModel>()

    constructor() {
        documentReference = getCustomDBCollection()
        configCollectionListener()
    }

    fun configCollectionListener() {
        documentReference?.addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
            if (e != null) {
                return@EventListener
            }
            if (value?.data != null) {
                configLiveDataModel.postValue(
                    value!!.toObject(ConfigDataModel::class.java)
                )
            }
        })

    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    override fun getCustomUid(): String? {
        return CUSTOMUID //To change body of created functions use File | Settings | File Templates.
    }

    fun getForceUpdateCurrentVersion(latestAPPUpdateListener: LatestAPPUpdateListener){
        var data = getCollectionReference().whereEqualTo("app_update_config", true).get()
                .addOnSuccessListener {
                    runCatching {
                        val appUpdates = it.toObjects(LatestAPPUpdateModel::class.java)
                        appUpdates?.get(0)?.let {
                        latestAPPUpdateListener?.getCurrentAPPVersion(it)
                        }
                    }.onSuccess {

                    }.onFailure {

                    }

            }
    }

    interface LatestAPPUpdateListener{
        fun getCurrentAPPVersion(latestAPPUpdateModel : LatestAPPUpdateModel)
    }

    class LatestAPPUpdateModel{
        var is_active = false
        lateinit var force_update_current_version : String
        var force_update_required:Boolean = false
    }

}