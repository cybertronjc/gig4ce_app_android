package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.ActionButton
import com.gigforce.common_ui.viewdatamodels.GigInfoCardDVM
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.giger_app.dataviewmodel.*
import com.gigforce.giger_app.ui.UpcomingGigsComponent
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IHomeCardsFBRepository {
    fun getData(): LiveData<List<Any>>
    fun loadData()
}

class HomeCardsFBRepository @Inject constructor() : IHomeCardsFBRepository {
    private var data: MutableLiveData<List<Any>> = MutableLiveData()
    val collectionName = "AppConfigs_Home"

    init {
        loadData()
    }

    fun getFirebaseReference() {
        FirebaseFirestore.getInstance()
            .collection(collectionName)
            .orderBy("index")
            .addSnapshotListener { value, error ->
                value?.documents?.let {
                    val allData = ArrayList<Any>()
                    for (item in it) {
                        handleDataSnapshot(item)?.let { allData.add(it) }
                    }
                    data.value = allData
                }
            }
    }

    fun handleDataSnapshot(snapshot: DocumentSnapshot): Any? {
        val type = snapshot.get("type") as? String ?: ""
        when (type) {
            "sec_action" -> {
                return snapshot.toObject(StandardActionCardDVM::class.java)
            }
            "sec_learning" -> {
                return snapshot.toObject(LearningLayoutDVM::class.java)
            }
            "sec_client_activation" -> {
                return snapshot.toObject(ClientActivationLayoutDVM::class.java)
            }

            "sec_main_nav" -> {
                return snapshot.toObject(MainSectionDVM::class.java)
            }
            "sec_help_videos" -> {
                return snapshot.toObject(HelpVideosSectionDVM::class.java)
            }
            "upcoming_gigs_info" ->{
                return snapshot.toObject(UpcomingGigSectionDVM::class.java)
            }
            else -> return null
        }
    }

    override fun getData(): LiveData<List<Any>> {
        return data
    }

    override fun loadData() {
        getFirebaseReference()
    }

}