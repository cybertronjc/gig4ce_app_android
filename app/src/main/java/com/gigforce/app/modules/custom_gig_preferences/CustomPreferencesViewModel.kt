package com.gigforce.app.modules.custom_gig_preferences

import androidx.lifecycle.*

class CustomPreferencesViewModel(var owner: LifecycleOwner) : ViewModel() {
    var customPreferencesRepository = CustomPreferencesRepository()
    public lateinit var customPreferencesDataModel: CustomPreferencesDataModel
    var customPreferencesLiveDataModel: MutableLiveData<CustomPreferencesDataModel> =
        MutableLiveData<CustomPreferencesDataModel>()

    init {
        getAllData()
    }

    public fun getAllData() {
        customPreferencesRepository.getDBCollection()
            .addSnapshotListener { value, e ->
                if (e != null) {
                    return@addSnapshotListener

                }
                if (value?.data != null) {
                    customPreferencesLiveDataModel.postValue(
                        value!!.toObject(CustomPreferencesDataModel::class.java)
                    )
                } else {
                    var customPreferencesDataModel1 = CustomPreferencesDataModel()
                    customPreferencesDataModel1.unavailable = ArrayList<UnavailableDataModel>()
                    customPreferencesRepository.setDefaultData(customPreferencesDataModel1)
                }
            }
        customPreferencesLiveDataModel.observe(owner, Observer { data ->
            customPreferencesDataModel = data
        })
    }

    fun getCustomPreferenceData(): CustomPreferencesDataModel {
        return customPreferencesDataModel
    }

    fun updateCustomPreference(unavailableDataModel: UnavailableDataModel) {

        if (customPreferencesDataModel != null && unavailableDataModel != null) {
            deleteCustomPreference(unavailableDataModel)
            customPreferencesRepository.setData(unavailableDataModel)
        }
    }

    fun deleteCustomPreference(unavailableDataModel: UnavailableDataModel) {
        var oldUnavailableDataModel = unavailableDataModel.findDateDataModel(
            customPreferencesDataModel.unavailable
        )
        if (oldUnavailableDataModel != null) {
            customPreferencesRepository.removeData(
                oldUnavailableDataModel
            )
        }
    }

    fun markUnavaialbleTimeSlots(unavailableDataModel: UnavailableDataModel){
        if (customPreferencesDataModel != null && unavailableDataModel != null) {
            var dataModelToDelete = findDataModelForSlots(unavailableDataModel)
            if(dataModelToDelete!=null) {
                var dataModelToInsert =
                    findDataModelForUnavailableSlots(dataModelToDelete, unavailableDataModel)
                deleteCustomPreference(dataModelToDelete)
            customPreferencesRepository.setData(dataModelToInsert)
            }
            else{
                customPreferencesRepository.setData(unavailableDataModel)
            }
        }
    }

    private fun findDataModelForUnavailableSlots(
        dataModelToDelete: UnavailableDataModel,
        unavailableDataModel: UnavailableDataModel
    ): UnavailableDataModel {
        var copiedModel = dataModelToDelete.copyObject()
        copiedModel.setUnavailaleSlots(unavailableDataModel)
        return copiedModel
    }

    fun markAvailableTimeSlots(unavailableDataModel: UnavailableDataModel) {
        if (customPreferencesDataModel != null && unavailableDataModel != null) {
            var dataModelToDelete = findDataModelForSlots(unavailableDataModel)
            var dataModelToInsert =
                findDataModelForAvailableSlots(dataModelToDelete, unavailableDataModel)
            deleteCustomPreference(dataModelToDelete)
            customPreferencesRepository.setData(dataModelToInsert)
        }
    }

    private fun findDataModelForAvailableSlots(
        dataModelToDelete: UnavailableDataModel,
        unavailableDataModel: UnavailableDataModel
    ): UnavailableDataModel {
        var copiedModel = dataModelToDelete.copyObject()
        copiedModel.setAvailaleSlots(unavailableDataModel)
        return copiedModel
    }

    private fun findDataModelForSlots(unavailableDataModel: UnavailableDataModel): UnavailableDataModel {
        return unavailableDataModel.findDataModelForSlot(
            customPreferencesDataModel.unavailable
        )!!
    }
}