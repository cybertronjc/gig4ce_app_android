package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IMainNavDataRepository {
    fun reload()
    fun getData():LiveData<List<FeatureItemCard2DVM>>
}

class MainNavDataRepository @Inject constructor() :
    IMainNavDataRepository {

    private var data:MutableLiveData<List<FeatureItemCard2DVM>> = MutableLiveData()

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

    override fun getData(): LiveData<List<FeatureItemCard2DVM>> {
        return data
    }

}