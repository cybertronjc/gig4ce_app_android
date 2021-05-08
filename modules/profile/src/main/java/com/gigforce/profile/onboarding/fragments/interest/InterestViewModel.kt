package com.gigforce.profile.onboarding.fragments.interest

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class InterestViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var firebaseDB = FirebaseFirestore.getInstance()
    var COLLECTION_NAME = "Mst_Skills"

    private val _skillsData = MutableLiveData<ArrayList<InterestDM>>()
    val skills: LiveData<ArrayList<InterestDM>> = _skillsData


    fun getCollectionName(): String {
        return COLLECTION_NAME
    }

     fun getSkills()  {

        try {
            val skillsData =  ArrayList<InterestDM>()
            firebaseDB.collection(getCollectionName())
                .addSnapshotListener { value, error ->
                    error?.printStackTrace()

                    value.let {
                        it?.documents?.forEach { skill ->
                            Log.d("skill", skill.toString())
                            skill.toObject(InterestDM::class.java).let {
                                it?.id = skill.id
                                if (it != null) {
                                    skillsData.add(it)
                                }
                                Log.d("fb", it.toString())
                            }
                        }
                    }
                    _skillsData.value = skillsData
                }
        }
        catch (e: Exception){

        }
    }
}