package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.client_activation.client_activation.dataviewmodel.ClientActivationLayoutDVM
import com.gigforce.common_ui.viewdatamodels.BannerCardDVM
import com.gigforce.common_ui.viewdatamodels.OtherFeatureComponentDVM
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.giger_app.dataviewmodel.GigForceTipsDVM
import com.gigforce.giger_app.dataviewmodel.HelpVideosSectionDVM
import com.gigforce.giger_app.dataviewmodel.MainSectionDVM
import com.gigforce.giger_app.dataviewmodel.UpcomingGigSectionDVM
import com.gigforce.learning.dataviewmodels.LearningLayoutDVM
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

interface ICommonConfigCardsFBRepo{
    fun getData(): LiveData<List<Any>>
    fun loadData(collectionName: String)
}

open class CommonConfigCardsFBRepo : ICommonConfigCardsFBRepo {
    private var data: MutableLiveData<List<Any>> = MutableLiveData()

    override fun getData(): LiveData<List<Any>> {
        return data
    }

    override fun loadData(collectionName : String) {
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
            "sec_tips" -> {
                return snapshot.toObject(GigForceTipsDVM::class.java)
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
            "sec_other_features" ->{
                return snapshot.toObject(OtherFeatureComponentDVM::class.java)
            }
            "sec_banner_feature" ->{
                return snapshot.toObject(BannerCardDVM::class.java)
            }
            else -> return null
        }
    }
}