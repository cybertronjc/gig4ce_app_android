package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.ActionButton
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.giger_app.dataviewmodel.ClientActivationLayoutDVM
import com.gigforce.giger_app.dataviewmodel.LearningLayoutDVM
import com.gigforce.giger_app.dataviewmodel.MainSectionDVM
import com.gigforce.giger_app.dataviewmodel.UpcomingGigSectionDVM
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IBSDataRepository {
    fun loadData()
    fun getData(): LiveData<List<Any>>
}

class BSDataRepository @Inject constructor() : IBSDataRepository{
    private var data: MutableLiveData<List<Any>> = MutableLiveData()
    val collectionName = "AppConfigs_BS"

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
                val title: String = snapshot.get("title") as? String ?: "-"
                val desc: String = snapshot.get("desc") as? String ?: "-"
                val imageUrl: String? = snapshot.get("imageUrl") as? String
                val actionP: Map<String, String>? = snapshot.get("action1") as? Map<String, String>
                    ?: null

                val action1 =
                    ActionButton(title = actionP?.get("title"), navPath = actionP?.get("navPath"))
                val bgcolor = Integer.valueOf(
                    snapshot.get("bgcolor")?.toString() ?: "0"
                ) //Todo Integer value issue
                val textColor = Integer.valueOf(
                    snapshot.get("textcolor")?.toString() ?: "0"
                ) //Todo Integer value issue
                val marginRequired = snapshot.get("marginRequired") as? Boolean ?: false
                return StandardActionCardDVM(
                    null,
                    title = title,
                    desc = desc,
                    imageUrl = imageUrl,
                    action1 = action1,
//                    bgcolor = bgcolor,
                    textColor = textColor,
                    marginRequired = marginRequired
                )
            }
            "sec_learning" -> {
                return LearningLayoutDVM(
                    type = type
                )
            }
            "sec_client_activation" -> {
                return ClientActivationLayoutDVM(
                    type = "sec_client_activation"
                )
            }

            "sec_main_nav" -> {
                return MainSectionDVM(type = type)
            }
            "upcoming_gigs_info" ->{
                return UpcomingGigSectionDVM(type = type)
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