package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.VideoItemCardDVM
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IHelpVideosDataRepository {
    fun loadData()
    fun getData(): LiveData<List<Any>>
}

class HelpVideosDataRepository @Inject constructor() : IHelpVideosDataRepository{
    private var data: MutableLiveData<List<Any>> = MutableLiveData()
    val collectionName = "Help_Videos"
    init {
        loadData()
    }

    fun getFirebaseReference() {
        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .orderBy("index")
            .addSnapshotListener { value, error ->
                value?.documents?.let {
                    val _data = ArrayList<VideoItemCardDVM>()
                    for (item in it) {
                        val videoYoutubeId = item?.get("videoYoutubeId") as? String ?: "-"
                        val lengthSeconds = item?.get("length",Int::class.java)?:0
                        val lengthStr = item?.getString("lengthStr")?:""
                        val title = item?.get("title") as? String ?: ""
                        val clockRequired = item?.get("clockRequired") as? Boolean?: false
                        val videoType = item?.get("videoType") as? String
                        _data.add(
                            VideoItemCardDVM(
                                videoYoutubeId = videoYoutubeId,
                                title = title,
                                timeSeconds = lengthSeconds,
                                timeStr = lengthStr,
                                clockRequired = clockRequired,
                                videoType = videoType
                            )
                        )
                    }
                    data.value = _data
                }
            }
    }

    override fun loadData() {
        getFirebaseReference()
    }

    override fun getData(): LiveData<List<Any>> {
        return data
    }

}