package com.gigforce.common_ui.viewmodels.userpreferences


import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.R
import com.gigforce.common_ui.configrepository.ConfigDataModel
import com.gigforce.common_ui.configrepository.ConfigRepository
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.repository.prefrepo.PreferencesRepository
import com.gigforce.core.datamodels.profile.AddressFirestoreModel
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.datamodels.user_preferences.EarningDataModel
import com.gigforce.core.datamodels.user_preferences.PreferencesDataModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SharedPreferenceViewModel : ViewModel {
    companion object {
        var preferencesDataModelObj: PreferencesDataModel =
            PreferencesDataModel()
        var profileDataModelObj: ProfileData = ProfileData()
    }

    var profileFirebaseRepository =
        ProfileFirebaseRepository()
    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData<ProfileData>()
    var preferencesRepository: PreferencesRepository =
        PreferencesRepository()

    var preferenceDataModel: MutableLiveData<PreferencesDataModel> =
        MutableLiveData<PreferencesDataModel>()
    var configRepository = ConfigRepository()

    constructor()
    constructor(configDataModel1: ConfigDataModel?) {
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

//    init {
//        getAllData()
//    }

    fun getAllData() {
        preferencesRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    return@EventListener
                }
                if (value?.data == null) {
                    if (configDataModel != null) {
                        var defaultData =
                            PreferencesDataModel()
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
                        value.toObject(PreferencesDataModel::class.java)
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
                        value.toObject(ProfileData::class.java)
                    )
                }
            })


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
        if (configDataModel != null)
            for (slot in configDataModel?.time_slots!!) {
                arr.add(slot.time_slot_id.toString())
            }
        return arr
    }

    fun getAllSlotsToShow(): ArrayList<String> {
        var arrTimeSlots = ArrayList<String>()
        configDataModel?.let {
            arrTimeSlots.add("All")
            for (congTimeSlot in it.time_slots) {
                arrTimeSlots.add(getTime(congTimeSlot.start_time_slot) + "-" + getTime(congTimeSlot.end_time_slot))
            }
        }
        return arrTimeSlots
    }

    fun getSelectedSlotsToShow(selectedSlots: ArrayList<String>): ArrayList<String> {
        var arrTimeSlots = ArrayList<String>()

        configDataModel?.let {
            if (selectedSlots.size == it.time_slots.size) {
                arrTimeSlots.add("All")
            }
            for (congTimeSlot in it.time_slots) {
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
        return arrTimeSlots
    }

    fun getSelectedSlotsIds(
        indices: ArrayList<Int>
    ): ArrayList<String> {
        var arrListSlots = ArrayList<String>()
        configDataModel?.let {
            for (index in indices) {
                if (index != 0)
                    arrListSlots.add(it.time_slots.get(index - 1).time_slot_id.toString())
            }
        }

        return arrListSlots
    }

    private fun getTime(date: Date?): String {
        date?.let {
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

    fun getLocation(context: Context): String {
        var str = ""
        if (preferencesDataModelObj.isWorkFromHome) {
            str += context.getString(R.string.work_from_home)
        }
        if (!profileDataModelObj.address.current.city.isNullOrBlank()) {
            str += profileDataModelObj.address.current.city
        }
        if (str.isNullOrBlank()) {
            return context.getString(R.string.none)
        }
        return str

    }

    fun getEarning(): String {
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

    fun getDateTimeSubtitle(context : Context): String {
        var subTitle = ""
        var weekDaysCount =
            if (preferencesDataModelObj.isweekdaysenabled) (if (preferencesDataModelObj.selecteddays.size == 6) (preferencesDataModelObj.selecteddays.size - 1) else preferencesDataModelObj.selecteddays.size) else 0
        var weekendCount =
            if (preferencesDataModelObj.isweekendenabled) (if (preferencesDataModelObj.selectedweekends.size == 3) preferencesDataModelObj.selectedweekends.size - 1 else preferencesDataModelObj.selectedweekends.size) else 0
        var countNumberOfSelectedDays = weekDaysCount + weekendCount
        if (countNumberOfSelectedDays == 0) {
            subTitle = context.getString(R.string.none)
        } else if (countNumberOfSelectedDays > 1) {
            subTitle = countNumberOfSelectedDays.toString() + " " + context.getString(R.string.days_with_space)
        } else if (preferencesDataModelObj.selecteddays.size == 1) {
            subTitle = countNumberOfSelectedDays.toString() + " " + context.getString(R.string.day_with_space)
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
        profileFirebaseRepository.updateCurrentAddress(address)
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


    fun setWorkFromHome(boolean: Boolean) {
        preferencesRepository.setData("workFromHome", boolean)
    }


    fun saveLanguageToFirebase(langStr: String, langCode: String) {
        preferencesRepository.setDataAsKeyValue("languageName", langStr)
        preferencesRepository.setDataAsKeyValue("languageCode", langCode)
    }

    fun saveEarningData(earningData: EarningDataModel) {
        preferencesRepository.setDataAsKeyValue(earningData)
    }

    var configLiveDataModel: MutableLiveData<ConfigDataModel> = MutableLiveData()
    var configDataModel: ConfigDataModel? = null
    fun getConfiguration() {
        configRepository.getCustomDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    return@EventListener
                }
                if (value?.data != null) {
                    configLiveDataModel.postValue(
                        value.toObject(ConfigDataModel::class.java)
                    )
                }
            })
    }

    fun setConfiguration(configDataModel: ConfigDataModel) {
        this.configDataModel = configDataModel
    }

}
