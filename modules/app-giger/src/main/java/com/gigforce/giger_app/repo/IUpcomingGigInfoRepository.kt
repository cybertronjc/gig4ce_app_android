package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.GigInfoCardDVM
import com.gigforce.common_ui.viewdatamodels.VideoItemCardDVM
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

interface IUpcomingGigInfoRepository {
    fun loadData()
    fun getData(): LiveData<List<Any>>
}

class UpcomingGigInfoRepository @Inject constructor() : IUpcomingGigInfoRepository,
    BaseFirestoreDBRepository() {
    private var data: MutableLiveData<List<Any>> = MutableLiveData()

    companion object {
        val COLLECTION_NAME = "Gigs"
    }

    init {
        loadData()
    }

    fun getFirebaseReference() {
        var calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        getCollectionReference().whereEqualTo("gigerId", getUID()).whereGreaterThan(
            "startDateTime",
            calendar.time
        ).orderBy("startDateTime")
            .addSnapshotListener { value, error ->
                value?.documents?.let { it ->

                    val _data = ArrayList<GigInfoCardDVM>()
                    it.forEach { doc ->
                        doc.toObject(GigInfoCardDVM::class.java)?.let { data ->
                            data.gigId = doc.id
                            _data.add(data)
                        }
                    }
                    data.value = _data

                } ?: run {
                    print("working")
                }
            }
    }

    override fun loadData() {
        getFirebaseReference()
    }

    override fun getData(): LiveData<List<Any>> {
        return data
    }

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

}