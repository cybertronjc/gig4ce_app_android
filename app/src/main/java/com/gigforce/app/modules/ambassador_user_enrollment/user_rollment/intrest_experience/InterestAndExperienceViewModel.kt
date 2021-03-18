package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.intrest_experience

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.core.datamodels.profile.Experience
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.repo.UserEnrollmentRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterestAndExperienceViewModel @Inject constructor(
    private val buildConfig: IBuildConfigVM
) : ViewModel() {
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val userEnrollmentRepository: UserEnrollmentRepository =
        UserEnrollmentRepository(buildConfig = buildConfig)

    private val _submitInterestState = MutableLiveData<Lse>()
    val submitInterestState: LiveData<Lse> = _submitInterestState

    fun submitInterests(
        uid: String,
        interests: List<String>
    ) = viewModelScope.launch {

        _submitInterestState.postValue(Lse.loading())
        try {
            profileFirebaseRepository.submitSkills(
                uid = uid,
                interest = interests
            )
            userEnrollmentRepository.setInterestAsUploaded(uid)

            _submitInterestState.value = Lse.success()
            _submitInterestState.value = null
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            _submitInterestState.value = Lse.error(e.message ?: "Unable to submit interest")
            _submitInterestState.value = null
        }
    }

    private val _saveExpAndReturnNextOne = MutableLiveData<Lce<String?>>()
    val saveExpAndReturnNextOne: LiveData<Lce<String?>> = _saveExpAndReturnNextOne

    fun saveExpAndReturnNewOne(userId: String, experience: Experience) = viewModelScope.launch {

        try {
            _saveExpAndReturnNextOne.value = Lce.loading()

            profileFirebaseRepository.submitExperience(userId, experience)
            checkForPendingInterestExperience(userId)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _saveExpAndReturnNextOne.value = Lce.error("Unable to save Exp : ${e.message}")
        }
    }

    fun updateExpAndReturnNewOne(userId: String, experience: Experience) = viewModelScope.launch {
        _saveExpAndReturnNextOne.value = Lce.loading()

        try {
            profileFirebaseRepository.updateExistingExperienceElseAdd(userId, experience)

            val profileData = profileFirebaseRepository.getProfileData(userId)
            val experienceList = profileData.experiences ?: return@launch
            val skills = profileData.skills ?: return@launch

            if (experienceList.size == profileData.skills!!.size) {

                for (i in experienceList.indices) {
                    if (experienceList[i].title == experience.title) {

                        if (i == experienceList.size - 1) {
                            _saveExpAndReturnNextOne.value = Lce.content(null)
                        } else {
                            _saveExpAndReturnNextOne.value =
                                Lce.content(experienceList[i + 1].title)
                        }
                        return@launch
                    }
                }
            } else {
                for (i in skills.indices) {
                    if (skills[i].id == experience.title) {

                        if (i == skills.size - 1) {
                            _saveExpAndReturnNextOne.value = Lce.content(null)
                        } else {
                            _saveExpAndReturnNextOne.value = Lce.content(skills[i + 1].id)
                        }
                        return@launch
                    }
                }
            }
            _saveExpAndReturnNextOne.value = Lce.content(null)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _saveExpAndReturnNextOne.value = Lce.error(e.message!!)
        }
    }

    fun skipCurrentExperienceAndFetchNextOne(userId: String, expName: String) =
        viewModelScope.launch {
            _saveExpAndReturnNextOne.value = Lce.loading()

            try {
                val profileData = profileFirebaseRepository.getProfileData(userId)
                val experienceList = profileData.experiences ?: return@launch
                val skills = profileData.skills ?: return@launch


                if (experienceList.size == profileData.skills!!.size) {

                    for (i in experienceList.indices) {
                        if (experienceList[i].title == expName) {

                            if (i == experienceList.size - 1) {
                                _saveExpAndReturnNextOne.value = Lce.content(null)
                            } else {
                                _saveExpAndReturnNextOne.value =
                                    Lce.content(experienceList[i + 1].title)
                            }
                            return@launch
                        }
                    }
                } else {
                    for (i in skills.indices) {
                        if (skills[i].id == expName) {

                            if (i == skills.size - 1) {
                                _saveExpAndReturnNextOne.value = Lce.content(null)
                            } else {
                                _saveExpAndReturnNextOne.value = Lce.content(skills[i + 1].id)
                            }
                            return@launch
                        }
                    }
                }

                _saveExpAndReturnNextOne.value = Lce.content(null)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                _saveExpAndReturnNextOne.value = Lce.error(e.message!!)
            }
        }


    private suspend fun checkForPendingInterestExperience(userId: String) {
        try {
            val profileData = profileFirebaseRepository.getProfileData(userId)

            val filledExps = profileData.experiences!!.map { it.title }
            val pendingInts = profileData.skills?.filter {
                !filledExps.contains(it.id)
            } ?: emptyList()

            if (pendingInts.isEmpty()) {
                userEnrollmentRepository.setExperienceAsUploaded(userId)
                _saveExpAndReturnNextOne.value = Lce.content(null)
            } else {
                _saveExpAndReturnNextOne.value = Lce.content(pendingInts.first().id)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _saveExpAndReturnNextOne.value = Lce.error("Unable to save Exp : ${e.message}")
        }
    }

    private val _experience = MutableLiveData<Lce<InterestAndExperienceData?>>()
    val experience: LiveData<Lce<InterestAndExperienceData?>> = _experience

    fun getPendingInterestExperience(userId: String) = viewModelScope.launch {
        try {
            val profileData = profileFirebaseRepository.getProfileData(userId)

            val filledExps = profileData.experiences!!.map { it.title }
            val pendingInts = profileData.skills?.filter {
                !filledExps.contains(it.id)
            } ?: emptyList()

            if (pendingInts.isEmpty()) {
                _experience.value = Lce.content(null)
            } else {
                _experience.value = Lce.content(
                    InterestAndExperienceData(
                        interestName = pendingInts.first().id,
                        experience = null
                    )
                )
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _experience.value = Lce.error("Unable to get Exp : ${e.message}")
        }
    }

    fun getInterestDetailsOrFetchFirstOneIfInterestNameIsNull(
        userId: String,
        interestName: String?
    ) = viewModelScope.launch {
        try {
            val profileData = profileFirebaseRepository.getProfileData(userId)

            if (interestName == null && !profileData.skills.isNullOrEmpty()) {
                //Show First one
                val interest = profileData.skills!!.first()
                val expMatch = profileData.experiences!!.find { exp -> exp.title == interest.id }

                if (expMatch == null) {
                    //Did not filled exp for this interest
                    _experience.value = Lce.content(
                        InterestAndExperienceData(
                            interestName = interest.id,
                            experience = null
                        )
                    )
                } else {
                    _experience.value = Lce.content(
                        InterestAndExperienceData(
                            interestName = interest.id,
                            experience = expMatch
                        )
                    )
                }
            } else {
                val expMatch = profileData.experiences!!.find { exp -> exp.title == interestName }

                if (expMatch == null) {
                    //Did not filled exp for this interest
                    _experience.value = Lce.content(
                        InterestAndExperienceData(
                            interestName = interestName!!,
                            experience = null
                        )
                    )
                } else {
                    _experience.value = Lce.content(
                        InterestAndExperienceData(
                            interestName = interestName!!,
                            experience = expMatch
                        )
                    )
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _experience.value = Lce.error("Unable to get Exp : ${e.message}")
        }
    }

}

data class InterestAndExperienceData(
    val interestName: String,
    val experience: Experience?
)