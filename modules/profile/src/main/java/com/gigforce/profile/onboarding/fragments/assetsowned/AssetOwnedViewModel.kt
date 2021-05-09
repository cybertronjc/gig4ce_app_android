package com.gigforce.profile.onboarding.fragments.assetsowned

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.profile.onboarding.fragments.interest.InterestDM
import com.google.firebase.firestore.FirebaseFirestore

class AssetOwnedViewModel : ViewModel() {
    var firebaseDB = FirebaseFirestore.getInstance()
    var COLLECTION_NAME = "Mst_Assets"

    private val _assetsData = MutableLiveData<ArrayList<AssestDM>>()
    val assets: LiveData<ArrayList<AssestDM>> = _assetsData


    fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    fun getAssets()  {

        try {
            val assetsData =  ArrayList<AssestDM>()
            firebaseDB.collection(getCollectionName())
                .whereEqualTo("isActive", true)
                .addSnapshotListener { value, error ->
                    error?.printStackTrace()

                    value.let {
                        it?.documents?.forEach { asset ->
                            Log.d("asset", asset.toString())
                            asset.toObject(AssestDM::class.java).let {
                                it?.id = asset.id
                                if (it != null) {
                                    assetsData.add(it)
                                }
                            }
                        }
                    }
                    _assetsData.value = assetsData
                }
        }
        catch (e: Exception){

        }
    }
}