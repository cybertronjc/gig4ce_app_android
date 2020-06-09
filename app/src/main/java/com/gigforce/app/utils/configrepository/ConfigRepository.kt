package com.gigforce.app.utils.configrepository

import androidx.lifecycle.MutableLiveData
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class ConfigRepository : BaseFirestoreDBRepository {
    var COLLECTION_NAME = "configuration";
    var CUSTOMUID = "N9EzW0SOAhOLVI1oA9Pu"

    companion object {
        private var configrepositoryObj: ConfigRepository? = null;
        fun getInstance(): ConfigRepository? {
            if (configrepositoryObj == null) configrepositoryObj =
                ConfigRepository()
            return configrepositoryObj
        }
    }

    var configLiveDataModel: MutableLiveData<ConfigDataModel> = MutableLiveData<ConfigDataModel>()

    constructor() {
        configCollectionListener()
    }

    private fun configCollectionListener() {
        getCustomDBCollection()?.addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
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


}