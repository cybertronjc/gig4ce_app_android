package com.gigforce.client_activation.repo

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.core.StringConstants
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IClientActivationDataRepository {
    fun loadData()
    fun getData(): LiveData<List<FeatureItemCardDVM>>
}

class ClientActivationDataRepository @Inject constructor():
    IClientActivationDataRepository {

    private var data: MutableLiveData<List<FeatureItemCardDVM>> = MutableLiveData()

    init {
        loadData()
    }

    override fun loadData() {
        FirebaseFirestore.getInstance().collection("Job_Profiles").orderBy("isActive").addSnapshotListener { value, error ->
            val doc = value?.documents
            doc?.let {
                val _data = ArrayList<FeatureItemCardDVM>()
                for (item in it) {
                    val title = item?.get("cardTitle") as? String ?: "-"
                    val cardImage = item?.get("cardImage") as? String
                    val priority = item?.get("priority") as? Int ?: 0
                    _data.add(FeatureItemCardDVM(id=item.id,title = title, image = cardImage, navPath = "client_activation",args = bundleOf(
                        StringConstants.JOB_PROFILE_ID.value to item?.id),priority = priority))
                }
                _data.sortBy { it.priority }
                data.value = _data
            }?:run{
                data.value = ArrayList<FeatureItemCardDVM>()
            }

        }
    }

    override fun getData(): LiveData<List<FeatureItemCardDVM>> {
        return data
    }

}

