package com.gigforce.profile.onboarding.fragments.interest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class InterestViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var firebaseDB = FirebaseFirestore.getInstance()
    var COLLECTION_NAME = "Mst_skills"

    private val _skillsData = MutableLiveData<ArrayList<InterestDM?>>()
    val skills: LiveData<ArrayList<InterestDM?>> = _skillsData


    fun getCollectionName(): String {
        return COLLECTION_NAME
    }



    suspend fun getSkills()  {
        val skillsData =  ArrayList<InterestDM?>()
        try {

            firebaseDB.collection(getCollectionName())
                .whereEqualTo("isActive", true).orderBy("index")
                .addSnapshotListener { value, error ->
                    error?.printStackTrace()

                    value.let {

                        it?.documents?.forEach { skill ->
                            skill.toObject(InterestDM::class.java).let {
                                it?.id = skill.id
                                skillsData.add(it)
                            }
                        }

                        _skillsData.value = skillsData
                    }
                }


        }
        catch (e: Exception){

        }

    }

}