package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.inject.Inject

interface IMainNavDataRepository {
    fun reload()
    fun getData():LiveData<List<FeatureItemCard2DVM>>
}

class MainNavDataRepository @Inject constructor() :
    IMainNavDataRepository {

    private var data:MutableLiveData<List<FeatureItemCard2DVM>> = MutableLiveData()
    private val firebaseAuthStateListener: FirebaseAuthStateListener by lazy {
        FirebaseAuthStateListener.getInstance()
    }
    init {
        reload()
    }

    override fun reload() {
        FirebaseFirestore.getInstance().collection("AppConfigs").document("main_nav").addSnapshotListener { value, error ->
            val doc = value?.data
            doc ?. let {
                val list = doc.get("data") as? List<Map<String, Any>>
                list ?. let {
                    val _data = ArrayList<FeatureItemCard2DVM>()
                    for(item in list){
                        val title = item.get("title") as? String ?: "-"
                        val index = (item.get("index") as? Long) ?: 500
                        val icon_type = item.get("icon") as? String
                        val navPath = item.get("navPath") as? String
                        _data.add(FeatureItemCard2DVM(title = title, image_type = icon_type, navPath = navPath,index = index.toInt()))
                    }
                    _data.sortBy { it.index }
                    data.value = _data
                }
            }
        }
    }

//    private suspend fun prepareMenus() {
//
//        try {
//            val currentUser =
//                firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow()
//            val phoneNumber = currentUser.phoneNumber!!
//            val phoneNumber2 = phoneNumber.substring(1)
//            val phoneNumber3 = "0" + phoneNumber.substring(3)
//            val phoneNumber4 = phoneNumber.substring(3)
//
//            val getBussinessContactQuery = bsCalendarScreenRepository
//                .db
//                .collection("Business_Contacts")
//                .whereIn(
//                    "primary_no",
//                    arrayListOf(phoneNumber, phoneNumber2, phoneNumber3, phoneNumber4)
//                )
//                .getOrThrow()
//
//            _isUserTlCheck.postValue(getBussinessContactQuery.size() > 0)
//        } catch (e: Exception) {
//            _isUserTlCheck.postValue(false)
//        }
//    }

    override fun getData(): LiveData<List<FeatureItemCard2DVM>> {
        return data
    }

}