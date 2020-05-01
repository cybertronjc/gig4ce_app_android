package com.gigforce.app.modules.preferences

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.R
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class SharedPreferenceViewModel : ViewModel() {
    companion object {
        var preferencesDataModelObj: PreferencesDataModel = PreferencesDataModel()
    }
    var preferencesRepository:PreferencesRepository = PreferencesRepository()
    var preferenceDataModel: MutableLiveData<PreferencesDataModel> = MutableLiveData<PreferencesDataModel>()

    fun getPreferenceDataModel():PreferencesDataModel{
        return preferencesDataModelObj
    }
    fun setPreferenceDataModel(preferencesDataModel: PreferencesDataModel){
        preferencesDataModelObj = preferencesDataModel
    }

    init {
        getAllData()
    }

    fun getAllData(){
        preferencesRepository.getDBCollection().addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->
            if (e != null) {
                return@EventListener
            }
            preferenceDataModel.postValue(
                value!!.toObject(PreferencesDataModel::class.java)
            )
        })
    }
    fun setIsWeekdays(checked: Boolean) {
        preferencesRepository.setData(preferencesRepository.WEEKDAYS,checked)
    }
    fun setIsWeekend(checked: Boolean){
        preferencesRepository.setData(preferencesRepository.WEEKEND,checked)
    }

    fun setWorkingDays(arrDays:ArrayList<String>){
        preferencesRepository.setDataAndDeleteOldData(preferencesRepository.WORKINGDAYS,arrDays)
    }

    fun setWorkingSlots(arrDays:ArrayList<String>){
        preferencesRepository.setDataAndDeleteOldData(preferencesRepository.WORKINGSLOTS,arrDays)
    }

    fun getPrefrencesData(): ArrayList<PreferencesScreenItem> {
        val prefrencesItems = ArrayList<PreferencesScreenItem>()
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_products,"Category",""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_product_services_pressed,"Roles","At atm"))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_referal,"Day and Time",getDateTimeSubtitle()))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_settings,"Location","Work from home,Bangalore"))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_settings,"Earning","2000-2200rs"))
        prefrencesItems.add(PreferencesScreenItem(0,"OTHERS",""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_link_broken,"App Language","English"))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_broadcast,"Notification",""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_products,"Sign out",""))
        return prefrencesItems;
    }
    fun getDateTimeSubtitle():String{
        var subTitle = ""
        var daysStr  = "day"
        if(preferencesDataModelObj.selecteddays.size==0){
            subTitle = "None"
        }else if(preferencesDataModelObj.selecteddays.size>1){
            subTitle = preferencesDataModelObj.selecteddays.size.toString()+" days"
        }
        else if(preferencesDataModelObj.selecteddays.size==1){
            subTitle = preferencesDataModelObj.selecteddays.size.toString()+" day"

        }
        return subTitle
    }
}