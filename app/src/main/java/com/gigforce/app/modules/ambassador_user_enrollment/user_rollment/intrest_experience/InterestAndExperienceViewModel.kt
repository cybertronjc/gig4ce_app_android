package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.intrest_experience

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.UserEnrollmentRepository
import com.gigforce.app.modules.preferences.AppConfigurationRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.profile.models.Skill2
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

data class InterestData(
        val interest : List<Skill2>,
        val profileData : ProfileData?
)

class InterestAndExperienceViewModel constructor(
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val userEnrollmentRepository: UserEnrollmentRepository = UserEnrollmentRepository(),
    private val appConfigurationRepository: AppConfigurationRepository = AppConfigurationRepository()
) : ViewModel() {

    private val _submitInterestState = MutableLiveData<Lse?>()
    val submitInterestState: LiveData<Lse?> = _submitInterestState

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

    private val _saveExpAndReturnNextOne = MutableLiveData<Lce<String?>?>()
    val saveExpAndReturnNextOne: LiveData<Lce<String?>?> = _saveExpAndReturnNextOne

    fun saveExpAndReturnNewOne(userId: String, experience: Experience) = viewModelScope.launch {

        try {
            _saveExpAndReturnNextOne.value = Lce.loading()

            profileFirebaseRepository.submitExperience(userId, experience)
            checkForPendingInterestExperience(userId)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _saveExpAndReturnNextOne.value = Lce.error("Unable to save Exp : ${e.message}")
            _saveExpAndReturnNextOne.value = null
        }
    }

    fun updateExpAndReturnNewOne(userId: String, experience: Experience) = viewModelScope.launch {
        _saveExpAndReturnNextOne.value = Lce.loading()

        try {
            profileFirebaseRepository.updateExistingExperienceElseAdd(userId, experience)

            val profileData = profileFirebaseRepository.getProfileData(userId)
            val experienceList = profileData.experiences ?: return@launch
            val skills = profileData.skills ?: return@launch

            if(experienceList.size == profileData.skills!!.size) {

                for (i in experienceList.indices) {
                    if (experienceList[i].title == experience.title) {

                        if (i == experienceList.size - 1) {
                            _saveExpAndReturnNextOne.value = Lce.content(null)
                            _saveExpAndReturnNextOne.value = null
                        } else {
                            _saveExpAndReturnNextOne.value = Lce.content(experienceList[i + 1].title)
                            _saveExpAndReturnNextOne.value = null

                        }
                        return@launch
                    }
                }
            } else {
                for (i in skills.indices) {
                    if (skills[i].id == experience.title) {

                        if (i == skills.size - 1) {
                            _saveExpAndReturnNextOne.value = Lce.content(null)
                            _saveExpAndReturnNextOne.value = null
                        } else {
                            _saveExpAndReturnNextOne.value = Lce.content(skills[i + 1].id)
                            _saveExpAndReturnNextOne.value = null
                        }
                        return@launch
                    }
                }
            }
            _saveExpAndReturnNextOne.value = Lce.content(null)
            _saveExpAndReturnNextOne.value = null

        } catch (e: Exception) {

            FirebaseCrashlytics.getInstance().recordException(e)
            _saveExpAndReturnNextOne.value = Lce.error(e.message!!)
            _saveExpAndReturnNextOne.value = null

        }
    }

    fun skipCurrentExperienceAndFetchNextOne(userId: String, expName: String) = viewModelScope.launch {
        _saveExpAndReturnNextOne.value = Lce.loading()

        try {
            val profileData = profileFirebaseRepository.getProfileData(userId)
            val experienceList = profileData.experiences ?: return@launch
            val skills = profileData.skills ?: return@launch


            if(experienceList.size == profileData.skills!!.size) {

                for (i in experienceList.indices) {
                    if (experienceList[i].title == expName) {

                        if (i == experienceList.size - 1) {
                            _saveExpAndReturnNextOne.value = Lce.content(null)
                            _saveExpAndReturnNextOne.value = null

                        } else {

                            _saveExpAndReturnNextOne.value = Lce.content(experienceList[i + 1].title)
                            _saveExpAndReturnNextOne.value = null
                        }
                        return@launch
                    }
                }
            } else {
                for (i in skills.indices) {
                    if (skills[i].id == expName) {

                        if (i == skills.size - 1) {
                            _saveExpAndReturnNextOne.value = Lce.content(null)
                            _saveExpAndReturnNextOne.value = null

                        } else {
                            _saveExpAndReturnNextOne.value = Lce.content(skills[i + 1].id)
                            _saveExpAndReturnNextOne.value = null

                        }
                        return@launch
                    }
                }
            }

            _saveExpAndReturnNextOne.value = Lce.content(null)
            _saveExpAndReturnNextOne.value = null

        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)

            _saveExpAndReturnNextOne.value = Lce.error(e.message!!)
            _saveExpAndReturnNextOne.value = null

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
                _saveExpAndReturnNextOne.value = null
            } else {
                _saveExpAndReturnNextOne.value = Lce.content(pendingInts.first().id)
                _saveExpAndReturnNextOne.value = null
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _saveExpAndReturnNextOne.value = Lce.error("Unable to save Exp : ${e.message}")
            _saveExpAndReturnNextOne.value = null

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
                        experience = null,
                        roles = getRolesForInterest(pendingInts.first().id)
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
                            experience = null,
                            roles = getRolesForInterest(interest.id)
                        )
                    )
                } else {
                    _experience.value = Lce.content(
                        InterestAndExperienceData(
                            interestName = interest.id,
                            experience = expMatch,
                            roles = getRolesForInterest(interest.id)
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
                            experience = null,
                            roles = getRolesForInterest(interestName)
                        )
                    )
                } else {
                    _experience.value = Lce.content(
                        InterestAndExperienceData(
                            interestName = interestName!!,
                            experience = expMatch,
                            roles = getRolesForInterest(interestName)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _experience.value = Lce.error("Unable to get Exp : ${e.message}")
        }
    }

    private val _fetchUserInterestDataState = MutableLiveData<Lce<InterestData>?>()
    val fetchUserInterestDataState: LiveData<Lce<InterestData>?> = _fetchUserInterestDataState

    fun getInterestForUser(
            userId: String?,
            shouldFetchProfileDataToo : Boolean
    ) = viewModelScope.launch {
        try {
            _fetchUserInterestDataState.value = Lce.loading()

            var profileData : ProfileData? = null

            if(shouldFetchProfileDataToo && userId != null) {
                profileData = profileFirebaseRepository.getProfileData(
                        userId = userId
                )
            }

            _fetchUserInterestDataState.value = Lce.content(
                    InterestData(
                            interest = appConfigurationRepository.getAllSkills(),
                            profileData = profileData
                    )
            )
        } catch (e: Exception) {
            _fetchUserInterestDataState.value = Lce.error(e.message!!)
        }
    }

    private suspend fun getRolesForInterest(
            interestName :String
    ):List<String> =  appConfigurationRepository.getRolesForSkill(interestName)

}



data class InterestAndExperienceData(
    val interestName: String,
    val experience: Experience?,
    val roles : List<String>
)