package com.gigforce.profile.onboarding.fragments.interest

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.profile.repository.OnboardingProfileFirebaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class InterestViewModel: ViewModel() {

    val onboardingProfileFirebaseRepository =  OnboardingProfileFirebaseRepository()
    private var _skillsData = MutableLiveData<ArrayList<InterestDM>>()
    var skills: LiveData<ArrayList<InterestDM>> = _skillsData

    fun getSkillsList(): LiveData<ArrayList<InterestDM>>{
            try {
                onboardingProfileFirebaseRepository.getSkills().addSnapshotListener { value, error ->
                    if (error != null) {
                        _skillsData.value = ArrayList<InterestDM>()
                    }
                    var skillsData = ArrayList<InterestDM>()
                    value.let {
                        it?.documents?.forEach { skill ->
                            Log.d("skill", skill.toString())
                            skill.toObject(InterestDM::class.java).let {
                                it?.id = skill.id
                                if (it != null) {
                                    skillsData.add(it)
                                }
                            }
                        }
                    }
                    _skillsData.value = skillsData
                }
            }
            catch (e: Exception){

            }

        return _skillsData
    }
}