package com.gigforce.app.modules.preferences


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.R
import com.gigforce.app.modules.preferences.earnings.EarningDataModel
import com.gigforce.app.modules.preferences.location.CitiesRepository
import com.gigforce.app.modules.preferences.location.models.LocationPreferenceModel
import com.gigforce.app.modules.profile.models.AddressModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.profile.ProfileFirebaseRepository

import com.gigforce.app.modules.profile.models.AddressFirestoreModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.configrepository.ConfigDataModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SharedPreferenceViewModel : ViewModel {
    companion object {
        var preferencesDataModelObj: PreferencesDataModel = PreferencesDataModel()
        var profileDataModelObj: ProfileData = ProfileData()
        var addressModelObj: AddressModel = AddressModel()
        var configDataModel: ConfigDataModel? = null
    }

    var profileFirebaseRepository = ProfileFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    var preferencesRepository: PreferencesRepository = PreferencesRepository()
    //    var profileRepository:ProfileFirebaseRepository = ProfileFirebaseRepository()
    var citiesRepository: CitiesRepository = CitiesRepository()
    var preferenceDataModel: MutableLiveData<PreferencesDataModel> =
        MutableLiveData<PreferencesDataModel>()
//    var profileDataModel: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    constructor(){}
    constructor(configDataModel1: ConfigDataModel?){
    configDataModel = configDataModel1
    }
    fun getPreferenceDataModel(): PreferencesDataModel {
        return preferencesDataModelObj
    }

    fun setPreferenceDataModel(preferencesDataModel: PreferencesDataModel) {
        preferencesDataModelObj = preferencesDataModel
    }

    fun getProfileDataModel(): ProfileData {
        return profileDataModelObj
    }

    fun setProfileDataModel(profileDataModel: ProfileData) {
        profileDataModelObj = profileDataModel
    }


    init {
        getAllData()
    }

    fun getAllData() {
        preferencesRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    return@EventListener
                }
                if (value?.data == null) {
                    if(configDataModel!=null) {
                        var defaultData = PreferencesDataModel()
                        var slots = getAllSlots()
                        defaultData.isweekdaysenabled = true
                        defaultData.selecteddays.addAll(getAllDays())
                        defaultData.selectedslots.addAll(slots)
                        defaultData.isweekendenabled = true
                        defaultData.selectedweekends.addAll(getAllWeekendsDays())
                        defaultData.selectedweekendslots.addAll(slots)
                        preferencesRepository.setDefaultData(defaultData)
                    }

                } else {
                    preferenceDataModel.postValue(
                        value!!.toObject(PreferencesDataModel::class.java)
                    )
                }
            })

        profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    return@EventListener
                }
                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    userProfileData.postValue(
                        value!!.toObject(ProfileData::class.java)
                    )
                }
            })

//        profileRepository.getDBCollection().addSnapshotListener(EventListener<DocumentSnapshot> {
//                value, e ->
//            if (e != null) {
//                return@EventListener
//            }
//            profileDataModel.postValue(
//                value!!.toObject(ProfileData::class.java)
//            )
//        })


    }

    private fun getAllWeekendsDays(): ArrayList<String> {
        var weekendDays = ArrayList<String>()
        weekendDays.add("All")
        weekendDays.add("Saturday")
        weekendDays.add("Sunday")
        return weekendDays
    }

    private fun getAllDays(): ArrayList<String> {
        var arrDays = ArrayList<String>()
        arrDays.add("All")
        arrDays.add("Monday")
        arrDays.add("Tuesday")
        arrDays.add("Wednesday")
        arrDays.add("Thursday")
        arrDays.add("Friday")
        return arrDays
    }

    private fun getAllSlots(): ArrayList<String> {
        var arr = ArrayList<String>()
        if(configDataModel!=null)
        for(slot in configDataModel?.time_slots!!){
            arr.add(slot.time_slot_id.toString())
        }
        return arr
    }

    public fun getAllSlotsToShow(configDataModel: ConfigDataModel?): ArrayList<String> {
        var arrTimeSlots = ArrayList<String>()
        if (configDataModel != null) {
            arrTimeSlots.add("All")
            for (congTimeSlot in configDataModel.time_slots) {
                arrTimeSlots.add(getTime(congTimeSlot.start_time_slot) + "-" + getTime(congTimeSlot.end_time_slot))
            }
        }
        return arrTimeSlots;
    }
    public fun getSelectedSlotsToShow(configDataModel: ConfigDataModel?,selectedSlots:ArrayList<String>): ArrayList<String> {
        var arrTimeSlots = ArrayList<String>()
        if (configDataModel != null && selectedSlots!=null) {
            if(selectedSlots.size==configDataModel.time_slots.size)
            arrTimeSlots.add("All")
            for (congTimeSlot in configDataModel.time_slots) {
                for (seletedSlot in selectedSlots) {
                    if (congTimeSlot.time_slot_id.toString().equals(seletedSlot))
                        arrTimeSlots.add(
                            getTime(congTimeSlot.start_time_slot) + "-" + getTime(
                                congTimeSlot.end_time_slot
                            )
                        )
                }
            }
        }
        return arrTimeSlots;
    }
    fun getSelectedSlotsIds(indices: ArrayList<Int>,configDataModel: ConfigDataModel?): ArrayList<String> {
        var arrListSlots = ArrayList<String>()
        if(configDataModel!=null)
        for (index in indices) {
            if(index!=0)
                arrListSlots.add(configDataModel.time_slots.get(index-1).time_slot_id.toString())
        }
    return arrListSlots
    }
    private fun getTime(date: Date?): String {
        if (date != null) {
            var dateFormat = SimpleDateFormat("hh:mm aa")
            var convertedDate = dateFormat.format(date)
            return convertedDate
        }
        return ""
    }

    fun setIsWeekdays(checked: Boolean) {
        preferencesRepository.setData(preferencesRepository.WEEKDAYS, checked)
    }


    fun setWorkingDays(arrDays: ArrayList<String>) {
        preferencesRepository.setDataAndDeleteOldData(preferencesRepository.WORKINGDAYS, arrDays)
    }

    fun setWorkingSlots(arrDays: ArrayList<String>) {
        preferencesRepository.setDataAndDeleteOldData(preferencesRepository.WORKINGSLOTS, arrDays)
    }

    fun setIsWeekend(checked: Boolean) {
        preferencesRepository.setData(preferencesRepository.WEEKEND, checked)
    }

    fun setWorkendDays(arrDays: ArrayList<String>) {
        preferencesRepository.setDataAndDeleteOldData(preferencesRepository.WEEKENDDAYS, arrDays)
    }

    fun setWorkendSlots(arrDays: ArrayList<String>) {
        preferencesRepository.setDataAndDeleteOldData(preferencesRepository.WEEKENDSLOTS, arrDays)
    }

    fun getPrefrencesData(): ArrayList<PreferencesScreenItem> {
        val prefrencesItems = ArrayList<PreferencesScreenItem>()
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_link_black,"Category",""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_group_black,"Roles","At atm"))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_clock_black,"Day and Time",getDateTimeSubtitle()))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_location_pin_black,"Location",getLocation()))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_credit_card_black,"Earning",getEarning()))
        prefrencesItems.add(PreferencesScreenItem(0,"OTHERS",""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_language_black,"App Language",getLanguage()))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_notifications_on_black,"Notification",""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_power_button_black,"Sign out",""))
        return prefrencesItems
    }

    private fun getLocation(): String {
        var str : String = ""
        if(preferencesDataModelObj.isWorkFromHome){
            str+="Work from home,"
        }
        if(!profileDataModelObj.address.current.city.isNullOrBlank()){
            str+=profileDataModelObj.address.current.city
        }
        if(str.isNullOrBlank()){
            return "none"
        }
        return str

    }

    private fun getEarning(): String {

        return preferencesDataModelObj.earning.perMonthGoal.toString() + " Rs"
    }

    fun getPreferredLocationList(): ArrayList<String> {
        var preferredList = ArrayList<String>()
        preferredList.add("HSR")
        preferredList.add("Kormangala Block 1")
        preferredList.add("Kormangala Block 2")
        preferredList.add("BTM Layout 1")
        preferredList.add("BTM Layout 2")
        preferredList.add("HSR Sector 2")
        preferredList.add("Bellandur")
        return preferredList
    }

    fun getLanguage(): String {
        if (preferencesDataModelObj != null) return preferencesDataModelObj.languageName else return "English"
    }

    fun getDateTimeSubtitle(): String {
        var subTitle = ""
        var daysStr = "day"
        if (preferencesDataModelObj.selecteddays.size == 0) {
            subTitle = "None"
        } else if (preferencesDataModelObj.selecteddays.size > 1) {
            var totalDays = preferencesDataModelObj.selecteddays.size
            if (totalDays == 6)
                totalDays -= 1
            subTitle = totalDays.toString() + " days"
        } else if (preferencesDataModelObj.selecteddays.size == 1) {
            subTitle = preferencesDataModelObj.selecteddays.size.toString() + " day"

        }
        return subTitle
    }

    fun getCurrentAddress(): AddressModel? {
        return profileDataModelObj.address.current
    }

    fun getPermanentAddress(): AddressModel? {
        return profileDataModelObj.address.home
    }


    fun setCurrentAddress(address: AddressModel) {
        var addressMap: AddressFirestoreModel = profileDataModelObj.address
        addressMap.current = address
        profileFirebaseRepository.setAddress(addressMap)
    }

    fun setCurrentAddressPrferredDistanceData(
        preferredDistance: Int,
        preferredDistanceActive: Boolean
    ) {
        var addressMap: AddressFirestoreModel = profileDataModelObj.address
        addressMap.current.preferred_distance = preferredDistance
        addressMap.current.preferredDistanceActive = preferredDistanceActive
        profileFirebaseRepository.setAddress(addressMap)
    }

    fun setCurrentAddressPreferredDistanceActive(preferredDistanceActive: Boolean) {
        var addressMap: AddressFirestoreModel = profileDataModelObj.address
        addressMap.current.preferredDistanceActive = preferredDistanceActive
        profileFirebaseRepository.setAddress(addressMap)
    }

    fun setPermanentAddress(address: AddressModel) {
        var addressMap: AddressFirestoreModel = profileDataModelObj.address
        addressMap.home = address
        profileFirebaseRepository.setAddress(addressMap)
    }

    fun getLocations(): ArrayList<DocumentReference> {
        return preferencesDataModelObj.locations
    }

    fun setLocations(locations: LocationPreferenceModel) {
        preferencesRepository.setData(locations)
    }

    fun setWorkFromHome(boolean: Boolean) {
        preferencesRepository.setData("workFromHome", boolean)
    }

    fun getCities(): ArrayList<String> {
        return citiesRepository.getCities()
    }

    fun saveLanguageToFirebase(langStr: String, langCode: String) {
        preferencesRepository.setDataAsKeyValue("languageName", langStr)
        preferencesRepository.setDataAsKeyValue("languageCode", langCode)
    }

    fun saveEarningData(earningData: EarningDataModel) {
        preferencesRepository.setDataAsKeyValue(earningData)
    }


}

/*
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_products, "Category", ""))
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_product_services_pressed,
                "Roles",
                "At atm"
            )
        )
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_referal,
                "Day and Time",
                getDateTimeSubtitle()
            )
        )
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_settings,
                "Location",
                "Work from home,Bangalore"
            )
        )
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_settings, "Earning", getEarning()))
        prefrencesItems.add(PreferencesScreenItem(0, "OTHERS", ""))
        prefrencesItems.add(
            PreferencesScreenItem(
                R.drawable.ic_link_broken,
                "App Language",
                getLanguage()
            )
        )
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_broadcast, "Notification", ""))
        prefrencesItems.add(PreferencesScreenItem(R.drawable.ic_products, "Sign out", ""))
        return prefrencesItems;
 */