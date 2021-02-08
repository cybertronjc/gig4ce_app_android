package com.gigforce.learning.repo

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.core.StringConstants
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface ILearningDataRepository {
    fun loadData()
    fun getData(): LiveData<List<FeatureItemCardDVM>>
}

class LearningDataRepository @Inject constructor() :
    ILearningDataRepository {
    private var data: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()

    init {
        loadData()
    }

    override fun loadData() {
        FirebaseFirestore.getInstance().collection("Course_blocks").orderBy("priority")
            .whereEqualTo("type", "course").whereEqualTo("isopened", true)
            .addSnapshotListener { value, error ->

                val doc = value?.documents
                doc?.let {
                    val _data = ArrayList<FeatureItemCardDVM>()
                    for (item in it) {
                        val title = item?.get("Name") as? String ?: "-"
                        val subtitle = item?.get("Level") as? String ?: "-"
//                    val title = name + level
//                        val priority = (item?.get("priority") as? Int) ?: 500
                        val coverPic = item?.get("cover_pic") as? String
                        val nav_path = "learning/main"//item?.get("nav_path") as? String

                        _data.add(
                            FeatureItemCardDVM(
                                id = item.id,
                                title = title,
                                subtitle = subtitle,
                                image = coverPic,
                                navPath = nav_path,
                                args = bundleOf(
                                    StringConstants.COURSE_ID.value to item?.id)
                            )
                        )
                    }
                    data.value = _data
                }

            }
    }

    override fun getData(): LiveData<List<FeatureItemCardDVM>> {
        return data
    }
}
