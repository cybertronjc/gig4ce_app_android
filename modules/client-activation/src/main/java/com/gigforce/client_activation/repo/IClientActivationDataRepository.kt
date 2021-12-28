package com.gigforce.client_activation.repo

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.core.StringConstants
import com.gigforce.core.analytics.ClientActivationEvents
import com.gigforce.core.extensions.getOrThrow
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IClientActivationDataRepository {
    suspend fun  getData(priorityVal : Long): List<FeatureItemCardDVM>
}

class ClientActivationDataRepository @Inject constructor():
    IClientActivationDataRepository {

    override suspend fun getData(priorityVal : Long): List<FeatureItemCardDVM> {
        if(priorityVal == 0.toLong())return emptyList()
        val jobprofiles = FirebaseFirestore.getInstance().collection("Job_Profiles").whereLessThanOrEqualTo("priority",priorityVal).getOrThrow()
        val doc = jobprofiles.documents

        doc.let {
            val _data = ArrayList<FeatureItemCardDVM>()
            for (item in it) {
                val title = item?.get("cardTitle") as? String ?: "-"
                val onlyTitle = item?.get("title") as? String ?: "-"
                val cardImage = item?.get("cardImage") as? String
                val priority = (item?.get("priority") as? Long) ?: 10000
                val isActive = item?.get("isActive") as? Boolean

                _data.add(FeatureItemCardDVM(isActive = isActive,id=item.id,title = title, image = cardImage, navPath = "client_activation",args = bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to item?.id),priority = priority.toInt(), eventName = onlyTitle +"_"+ ClientActivationEvents.EVENT_USER_CLICKED, props = mapOf(
                    "id" to item.id,
                    "title" to onlyTitle,
                    "screen_source" to "Client Activation"
                )))
            }
            _data.sortBy { it.priority }
            return _data.filter { it.isActive?:false }
        }

    }

}

