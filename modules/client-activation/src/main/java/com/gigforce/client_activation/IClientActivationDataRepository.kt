package com.gigforce.client_activation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

interface IClientActivationDataRepository {
    fun loadData()
    fun getData(): LiveData<List<FeatureItemCardDVM>>
}

class ClientActivationDataRepository @Inject constructor(): IClientActivationDataRepository{

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
                    val nav_path = item?.get("nav_path") as? String
                    _data.add(FeatureItemCardDVM(id=item.id,title = title, image = cardImage, navPath = nav_path))
                }
                data.value = _data
            }

        }
    }

    override fun getData(): LiveData<List<FeatureItemCardDVM>> {
        return data
    }

}

