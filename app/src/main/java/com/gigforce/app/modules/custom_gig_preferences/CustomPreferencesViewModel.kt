package com.gigforce.app.modules.custom_gig_preferences

import androidx.lifecycle.*

class CustomPreferencesViewModel(var owner: LifecycleOwner) : ViewModel() {
    var customPreferencesRepository = CustomPreferencesRepository()
    lateinit var customPreferencesDataModel : CustomPreferencesDataModel
    var customPreferencesLiveDataModel: MutableLiveData<CustomPreferencesDataModel> =
        MutableLiveData<CustomPreferencesDataModel>()
    init {
        getAllData()
    }

    private fun getAllData() {
        customPreferencesRepository.getDBCollection()
            .addSnapshotListener{ value, e ->
                if (e != null) {
                    return@addSnapshotListener

                }
                if (value?.data != null) {
                    customPreferencesLiveDataModel.postValue(
                        value!!.toObject(CustomPreferencesDataModel::class.java)
                    )
                }
                else{
                    var customPreferencesDataModel1  = CustomPreferencesDataModel()
                    customPreferencesDataModel1.unavailable = ArrayList<UnavailableDataModel>()
                    customPreferencesRepository.setDefaultData(customPreferencesDataModel1)
                }
            }
        customPreferencesLiveDataModel.observe( owner, Observer { data ->
            customPreferencesDataModel = data
        })
    }
    fun getCustomPreferenceData():CustomPreferencesDataModel{
        return customPreferencesDataModel
    }
    fun updateCustomPreference(unavailableDataModel : UnavailableDataModel){

        if(customPreferencesDataModel!=null && unavailableDataModel!=null) {
            deleteCustomPreference(unavailableDataModel)
            customPreferencesRepository.setData(unavailableDataModel)
        }
    }

    fun deleteCustomPreference(unavailableDataModel : UnavailableDataModel){
        var oldUnavailableDataModel = unavailableDataModel.findDateDataModel(
            customPreferencesDataModel.unavailable
        )
        if(oldUnavailableDataModel!=null){
            customPreferencesRepository.removeData(
                oldUnavailableDataModel
            )}
    }
}