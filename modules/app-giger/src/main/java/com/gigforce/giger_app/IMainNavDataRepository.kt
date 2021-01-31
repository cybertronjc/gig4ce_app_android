package com.gigforce.giger_app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.SimpleDVM
import com.gigforce.giger_app.AppModuleLevelViewTypes.Companion.VIEW_MAIN_NAV_CTA
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IMainNavDataRepository {
    fun reload()
    fun getData():LiveData<List<FeatureItemCard2DVM>>
}

data class MainNavItemDVM(
    val text:String,
    val imageIconType: String?,
    val imageUrl: String?,
    val navPath: String?
) : SimpleDVM(VIEW_MAIN_NAV_CTA, navPath){

}

/*class FakeMainNavDataRepository @Inject constructor() :IMainNavDataRepository{


    override fun reload() {

    }

    override fun getData(): List<FeatureItemCard2DVM> {
        val featureItems = ArrayList<FeatureItemCard2DVM>()
        featureItems.add(FeatureItemCard2DVM("Chat", "chat" ))
        featureItems.add(FeatureItemCard2DVM("Setting", "setting", navPath = "setting"))
        // featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "My Gig"))
//        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Wallet"))
//        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Learning"))
//        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Chat"))
//        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Explore"))
//        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Profile", navPath = "profile"))
//        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Verification"))
//        featureItems.add(FeatureItemCard2DVM(R.drawable.ic_tip, "Settings", "setting"))
        return featureItems
    }
}*/

class MainNavDataRepository @Inject constructor() : IMainNavDataRepository{

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
                        val index = (item.get("index") as? Int) ?: 500
                        val icon_type = item.get("icon") as? String
                        val nav_path = item.get("nav_path") as? String
                        _data.add(FeatureItemCard2DVM(title = title, image_type = icon_type, navPath = nav_path))
                    }
                    data.value = _data
                }
            }
        }
    }

    override fun getData(): LiveData<List<FeatureItemCard2DVM>> {
        return data
    }

}