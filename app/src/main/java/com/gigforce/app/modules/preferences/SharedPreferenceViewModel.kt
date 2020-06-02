package com.gigforce.app.modules.preferences


import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.R
import com.gigforce.app.modules.preferences.location.CitiesRepository
import com.gigforce.app.modules.preferences.location.models.LocationPreferenceModel
import com.gigforce.app.modules.profile.models.AddressModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.profile.ProfileFirebaseRepository

import com.gigforce.app.modules.profile.models.AddressFirestoreModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class SharedPreferenceViewModel : ViewModel() {
    companion object {
        var preferencesDataModelObj: PreferencesDataModel = PreferencesDataModel()
        var profileDataModelObj: ProfileData = ProfileData()
        var addressModelObj: AddressModel = AddressModel()
    }
    var profileFirebaseRepository = ProfileFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    var preferencesRepository:PreferencesRepository = PreferencesRepository()
    var profileRepository:ProfileFirebaseRepository = ProfileFirebaseRepository()
    var citiesRepository:CitiesRepository = CitiesRepository()
    var preferenceDataModel: MutableLiveData<PreferencesDataModel> = MutableLiveData<PreferencesDataModel>()
    var profileDataModel: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()

    fun getPreferenceDataModel():PreferencesDataModel{
        return preferencesDataModelObj
    }
    fun setPreferenceDataModel(preferencesDataModel: PreferencesDataModel){
        preferencesDataModelObj = preferencesDataModel
    }

    fun getProfileDataModel(): ProfileData{
        return profileDataModelObj
    }

    fun setProfileDataModel(profileDataModel: ProfileData){
        profileDataModelObj = profileDataModel
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
            if(value?.data==null){
                var defaultData = PreferencesDataModel()
                defaultData.isweekdaysenabled = true
                defaultData.selecteddays.addAll(getAllDays())
                defaultData.selectedslots.addAll(getAllSlots())
             preferencesRepository.setDefaultData(defaultData)
            }else {
                preferenceDataModel.postValue(
                    value!!.toObject(PreferencesDataModel::class.java)
                )
            }
        })

        profileFirebaseRepository.getProfile().addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->
            if (e != null) {
                return@EventListener
            }
            if (value!!.data == null) {
                profileFirebaseRepository.createEmptyProfile()
            }
            else {
                userProfileData.postValue(
                    value!!.toObject(ProfileData::class.java)
                )
            }
        })

        profileRepository.getDBCollection().addSnapshotListener(EventListener<DocumentSnapshot> {
                value, e ->
            if (e != null) {
                return@EventListener
            }
            profileDataModel.postValue(
                value!!.toObject(ProfileData::class.java)
            )
        })


    }

    private fun getAllDays(): ArrayList<String> {
        var arrDays = ArrayList<String>()
        arrDays.add("All")
        arrDays.add("Monday")
        arrDays.add("Tuesday")
        arrDays.add("Wednesday")
        arrDays.add("Thrusday")
        arrDays.add("Friday")
        return arrDays
    }
    private fun getAllSlots(): ArrayList<String> {
        var arrSlots = ArrayList<String>()
        arrSlots.add("All")
        arrSlots.add("06:00 am - 10:00 am")
        arrSlots.add("10:00 am - 12:00 pm")
        arrSlots.add("12:00 pm - 06:00 pm")
        arrSlots.add("06:00 pm - 09:00 pm")
        arrSlots.add("09:00 pm - 12:00 pm")
        arrSlots.add("12:00 am - 03:00 am")
        return arrSlots
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
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_link_broken,"App Language",getLanguage()))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_broadcast,"Notification",""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_products,"Sign out",""))
        return prefrencesItems;
    }
    fun getLanguage():String{
        if(preferencesDataModelObj!=null) return preferencesDataModelObj.languageName else return "English"
    }
    fun getDateTimeSubtitle():String{
        var subTitle = ""
        var daysStr  = "day"
        if(preferencesDataModelObj.selecteddays.size==0){
            subTitle = "None"
        }else if(preferencesDataModelObj.selecteddays.size>1){
            var totalDays = preferencesDataModelObj.selecteddays.size
            if(totalDays==6)
                totalDays-=1
            subTitle = totalDays.toString()+" days"
        }
        else if(preferencesDataModelObj.selecteddays.size==1){
            subTitle = preferencesDataModelObj.selecteddays.size.toString()+" day"

        }
        return subTitle
    }

    fun getCurrentAddress(): AddressModel? {
        return profileDataModelObj.address.current
    }

    fun getPermanentAddress(): AddressModel? {
        return profileDataModelObj.address.home
    }


    fun setCurrentAddress(address: AddressModel){
        var addressMap:AddressFirestoreModel= profileDataModelObj.address
        addressMap.current=address
        profileRepository.setAddress(addressMap)
    }

    fun setPermanentAddress(address: AddressModel){
        var addressMap:AddressFirestoreModel= profileDataModelObj.address
        addressMap.home=address
        profileRepository.setAddress(addressMap)
    }

    fun getLocations(): ArrayList<DocumentReference> {
        return preferencesDataModelObj.locations
    }

    fun setLocations(locations: LocationPreferenceModel){
        preferencesRepository.setData(locations)
    }

    fun setWorkFromHome(boolean: Boolean){
        preferencesRepository.setData("workFromHome",boolean)
    }

    fun getCities(): ArrayList<String>{
        return citiesRepository.getCities()
    }

    fun saveLanguageToFirebase(langStr: String, langCode: String) {
        preferencesRepository.setDataAsKeyValue("languageName",langStr)
        preferencesRepository.setDataAsKeyValue("languageCode",langCode)

    }
}