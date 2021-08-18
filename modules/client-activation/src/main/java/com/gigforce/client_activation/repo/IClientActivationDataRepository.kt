package com.gigforce.client_activation.repo

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.core.StringConstants
import com.gigforce.core.analytics.ClientActivationEvents
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IClientActivationDataRepository {
    fun loadData()
    fun getData(): LiveData<List<Any>>
}

class ClientActivationDataRepository @Inject constructor():
    IClientActivationDataRepository {

    private var data: MutableLiveData<List<Any>> = MutableLiveData()

    init {
        loadData()
    }

    override fun loadData() {
        FirebaseFirestore.getInstance().collection("Job_Profiles").whereEqualTo("isActive", true).addSnapshotListener { value, error ->
            val doc = value?.documents
            doc?.let {
                val _data = ArrayList<FeatureItemCardDVM>()
                for (item in it) {
                    val title = item?.get("cardTitle") as? String ?: "-"
                    val onlyTitle = item?.get("title") as? String ?: "-"
                    val cardImage = item?.get("cardImage") as? String
                    val priority = (item?.get("priority") as? Long) ?: 10000
                    _data.add(FeatureItemCardDVM(id=item.id,title = title, image = cardImage, navPath = "client_activation",args = bundleOf(
                        StringConstants.JOB_PROFILE_ID.value to item?.id),priority = priority.toInt(), eventName = onlyTitle +"_"+ ClientActivationEvents.EVENT_USER_CLICKED, props = mapOf(
                        "id" to item.id,
                        "title" to onlyTitle,
                        "screen_source" to "Client Activation"
                    )))
                }
                _data.sortBy { it.priority }
                data.value = _data
            }?:run{
                data.value = ArrayList<FeatureItemCardDVM>()
            }

        }
    }

    override fun getData(): LiveData<List<Any>> {
        return data
    }





}

