package com.gigforce.profile.onboarding.fragments.assetsowned

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.profile.onboarding.fragments.interest.InterestDM
import com.gigforce.profile.repository.OnboardingProfileFirebaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class AssetOwnedViewModel : ViewModel() {
    val onboardingProfileFirebaseRepository =  OnboardingProfileFirebaseRepository()
    private val _assetsData = MutableLiveData<ArrayList<AssestDM>>()
    val assets: LiveData<ArrayList<AssestDM>> = _assetsData

    fun getAssetList(): LiveData<ArrayList<AssestDM>>{
        try {
            onboardingProfileFirebaseRepository.getAssets().addSnapshotListener { value, error ->
                if (error != null) {
                    _assetsData.value = ArrayList<AssestDM>()
                }
                var assetsData = ArrayList<AssestDM>()
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
        return _assetsData
    }
}