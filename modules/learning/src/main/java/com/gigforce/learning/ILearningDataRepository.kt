package com.gigforce.learning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface ILearningDataRepository {
    fun loadData()
    fun getData(): LiveData<List<FeatureItemCardDVM>>
}

class LearningDataRepository @Inject constructor() : ILearningDataRepository {
    private var data: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()
    override fun loadData() {
        FirebaseFirestore.getInstance().collection("Course_blocks").whereEqualTo("type", "course").addSnapshotListener { value, error ->

            val doc = value?.documents
            doc?.let {
                val _data = ArrayList<FeatureItemCardDVM>()
                for (item in it) {
                    val mapItem = item as? Map<String, Any>
                    val name = mapItem?.get("Name") as? String ?: "-"
                    val level = mapItem?.get("Level") as? String ?: "-"
                    val title = name + level
                    val priority = (mapItem?.get("priority") as? Int) ?: 500
                    val coverPic = mapItem?.get("cover_pic") as? String
                    val nav_path = mapItem?.get("nav_path") as? String
                    _data.add(FeatureItemCardDVM(title = title, image = coverPic, navPath = nav_path))
                }
                data.value = _data
            }

        }
    }

    override fun getData(): LiveData<List<FeatureItemCardDVM>> {
        return data
    }
}
