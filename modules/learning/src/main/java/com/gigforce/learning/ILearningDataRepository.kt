package com.gigforce.learning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface ILearningDataRepository {
    fun loadData()
    fun getData(): LiveData<List<FeatureItemCardDVM>>
}

//class LearningDataRepository @Inject constructor() : ILearningDataRepository{
//    private var data: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()
//    override fun loadData() {
//        FirebaseFirestore.getInstance().collection("Course_blocks").whereEqualTo("type","course").addSnapshotListener { value, error ->
//
//            val doc = value?.documents
//            doc ?. let {
//
////                val list = doc.get("data") as? List<Map<String, Any>>
////                list ?. let {
////                    val _data = ArrayList<FeatureItemCardDVM>()
////                    for(item in list){
////                        val title = item.get("title") as? String ?: "-"
////                        val index = (item.get("index") as? Int) ?: 500
////                        val icon_type = item.get("icon") as? String
////                        val nav_path = item.get("nav_path") as? String
////                        _data.add(FeatureItemCardDVM(title = title, image_type = icon_type, navPath = nav_path))
////                    }
////                    data.value = _data
//                }
//
//            }
//        }
//    }
//
//    override fun getData(): LiveData<List<FeatureItemCardDVM>> {
//        return LiveData<List<FeatureItemCardDVM>>()
//    }

//}