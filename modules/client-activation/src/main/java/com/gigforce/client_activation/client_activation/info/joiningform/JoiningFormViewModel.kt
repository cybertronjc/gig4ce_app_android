package com.gigforce.client_activation.client_activation.info.joiningform

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.client_activation.info.HubSubmissionRepository
import com.gigforce.client_activation.client_activation.info.JPApplicationDOJ
import com.gigforce.client_activation.client_activation.info.hubform.BusinessLocationDM
import com.gigforce.client_activation.client_activation.info.hubform.HubServerDM
import com.gigforce.client_activation.client_activation.repository.AadhaarDetailsRepository
import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.toFirebaseTimeStamp
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

class JoiningFormViewModel : ViewModel() {
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    val updatedResult: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    val _dojObserver: MutableLiveData<JPApplicationDOJ> = MutableLiveData<JPApplicationDOJ>()
    val dojObserver: LiveData<JPApplicationDOJ> = _dojObserver
//    private val _observableAddApplicationSuccess: MutableLiveData<Boolean> = MutableLiveData()
//    val observableAddApplicationSuccess: MutableLiveData<Boolean> = _observableAddApplicationSuccess

    val aadharDetailsRepo = AadhaarDetailsRepository()
    val hubSubmissionRepo = HubSubmissionRepository()
    val statesResult: MutableLiveData<MutableList<State>> = MutableLiveData<MutableList<State>>()
    val citiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()

    val caCitiesResult: MutableLiveData<MutableList<City>> = MutableLiveData<MutableList<City>>()

    val verificationResult: MutableLiveData<VerificationBaseModel> =
        MutableLiveData<VerificationBaseModel>()

    val _hub_states = MutableLiveData<List<String>>()
    val hub_states: LiveData<List<String>> = _hub_states

    val _hub_cities = MutableLiveData<List<String>>()
    val hub_cities: LiveData<List<String>> = _hub_cities

    val _hub_names = MutableLiveData<List<String>>()
    val hub_names: LiveData<List<String>> = _hub_names

    val _hub_submitted_data = MutableLiveData<HubServerDM>()
    val hub_submitted_data: LiveData<HubServerDM> = _hub_submitted_data

    val _profileData = MutableLiveData<ProfileData>()
    val profileData: LiveData<ProfileData> = _profileData
    val _allBusinessLocactionsList = MutableLiveData<List<BusinessLocationDM>>()
    val allBusinessLocactionsList: LiveData<List<BusinessLocationDM>> = _allBusinessLocactionsList

    fun getStates() = viewModelScope.launch {
        try {
            val states = aadharDetailsRepo.getStatesFromDb()
            statesResult.postValue(states)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun getVerificationData(uid: String) = viewModelScope.launch {
        try {
            val veriData = aadharDetailsRepo.getVerificationDetails(uid)
            verificationResult.postValue(veriData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getCities(stateCode: String) = viewModelScope.launch {
        try {
            val cities = aadharDetailsRepo.getCities(stateCode)
            citiesResult.postValue(cities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentAddCities(stateCode: String) = viewModelScope.launch {
        try {
            val cities = aadharDetailsRepo.getCities(stateCode)
            caCitiesResult.postValue(cities)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun loadHubStates(mJobProfileId: String) = viewModelScope.launch {

        val jobProfile = FirebaseFirestore.getInstance().collection("Job_Profiles")
            .whereEqualTo("profileId", mJobProfileId).getOrThrow()
        if (jobProfile == null) {
            _hub_states.value = emptyList()
            return@launch
        }
        val jpDoc = jobProfile.documents[0]
        var businessId = jpDoc.get("businessId").toString()
        Log.d("businessId_data", businessId)
        val businessLocactionsSnapshot =
            FirebaseFirestore.getInstance().collection("Business_Locations")
                .whereEqualTo("business_id", businessId).whereEqualTo("type", "office").getOrThrow()
        if (businessLocactionsSnapshot == null) {
            _hub_states.value = emptyList()
            return@launch
        }
        val businessLocactions = arrayListOf<BusinessLocationDM>()
        businessLocactionsSnapshot.let { querySnapshot ->
            querySnapshot.forEach { snaphot ->
                var model = snaphot.toObject(BusinessLocationDM::class.java)
                model.id = snaphot.id
                businessLocactions.add(model)
            }
        }
        _allBusinessLocactionsList.value = businessLocactions
        var bLocationsList = arrayListOf<String>()
        businessLocactions.forEach {
            it.state?.name?.let {
                bLocationsList.add(it)
            }
        }
        _hub_states.value = bLocationsList.distinct()
    }

    fun loadHubCities(state: String) {
        var filteredHub = allBusinessLocactionsList.value?.filter { it.state?.name == state }
        var hubCitiesList = arrayListOf<String>()
        filteredHub?.forEach {
            it.city?.name?.let {
                hubCitiesList.add(it)

            }
        }
        _hub_cities.value = hubCitiesList
    }

    fun loadHubNames(state: String, city: String) {
        var filteredHub = allBusinessLocactionsList.value?.filter { it.state?.name == state }
            ?.filter { it.city?.name == city }
        var hubList = arrayListOf<String>()
        filteredHub?.forEach {
            it.name?.let {
                hubList.add(it)
            }
        }
        _hub_names.value = hubList
    }


    fun loadHubData(mJobProfileId: String) {
        FirebaseFirestore.getInstance().collection("JP_Applications")
            .whereEqualTo("jpid", mJobProfileId)
            .whereEqualTo(
                "gigerId",
                FirebaseAuthStateListener.getInstance().getCurrentSignInUserInfoOrThrow().uid
            ).get().addOnSuccessListener {
                if (it != null && !it.isEmpty && !it.documents.isNullOrEmpty())
                    FirebaseFirestore.getInstance().collection("JP_Applications")
                        .document(it.documents[0].id)
                        .collection("Submissions")
                        .whereEqualTo("type", "hub_location").get().addOnSuccessListener {
                            if (it != null && !it.isEmpty && !it.documents.isNullOrEmpty())
                                try {
                                    _hub_submitted_data.value =
                                        it.documents[0].toObject(HubServerDM::class.java)
                                } catch (e: Exception) {

                                }
                        }
            }


    }


    fun submitJoiningFormData(
        data: AadhaarDetailsDataModel,
        mJobProfileId: String,
        state: String,
        city: String,
        hubname: String,
        email: String,
        dateOfBirth: Date,
        fName: String,
        maritalStatus: String,
        emergencyContact: String,
        dateOfJoining: Date
    ) = viewModelScope.launch {
        try {
            var aadharDetailUpdated = aadharDetailsRepo.setAadhaarDetailsFromJoiningForm(uid, data)
            if (aadharDetailUpdated) {
                aadharDetailUpdated = aadharDetailsRepo.setProfileRelatedData(
                    uid,
                    email,
                    dateOfBirth,
                    fName,
                    maritalStatus,
                    emergencyContact
                )
            }
            if (aadharDetailUpdated) {
                val hubSubmissionUpdated = hubSubmissionRepo.submitHubData(
                    state,
                    getStateId(state, city, hubname) ?: "",
                    city,
                    getCityId(state, city, hubname) ?: "",
                    hubname,
                    getHubDocId(state, city, hubname) ?: "",
                    mJobProfileId
                )
                if (hubSubmissionUpdated?.id?.isNotBlank() == true) {
                    hubSubmissionUpdated.application.forEach { draft ->
                        if (draft.type == "jp_hub_location" || draft.type == "aadhar_hub_questionnaire") {
                            draft.isDone = true
                        }
                    }

                    FirebaseFirestore.getInstance()
                        .collection("JP_Applications")
                        .document(hubSubmissionUpdated.id)
                        .updateOrThrow(
                            mapOf(
                                "dateOfJoining" to dateOfJoining.toFirebaseTimeStamp(),
                                "application" to
                                        hubSubmissionUpdated.application,
                                "updatedAt" to Timestamp.now(),
                                "updatedBy" to StringConstants.APP.value
                            )
                        )
                    updatedResult.postValue(true)
                } else
                    updatedResult.postValue(false)
            } else updatedResult.postValue(false)

        } catch (e: Exception) {
            e.printStackTrace()
            updatedResult.postValue(false)
        }
    }

    fun getHubDocId(state: String, city: String, hub: String): String? {
        return _allBusinessLocactionsList.value?.filter { it.state?.name == state && it.name == hub && it.city?.name == city }
            ?.get(0)?.id
    }

    fun getStateId(state: String, city: String, hub: String): String? {
        return _allBusinessLocactionsList.value?.filter { it.state?.name == state && it.name == hub && it.city?.name == city }
            ?.get(0)?.state?.id
    }

    fun getCityId(state: String, city: String, hub: String): String? {
        return _allBusinessLocactionsList.value?.filter { it.state?.name == state && it.name == hub && it.city?.name == city }
            ?.get(0)?.city?.id
    }

    fun loadProfileData() = viewModelScope.launch {
        try {
            var profileDataSnapShot =
                FirebaseFirestore.getInstance().collection("Profiles").document(uid).getOrThrow()
            var profileData = profileDataSnapShot.toObject(ProfileData::class.java)
            profileData?.let {
                _profileData.postValue(it)
            }

        } catch (e: Exception) {

        }

    }

    fun loadApplicationDataDOJ(mJobProfileId: String) = viewModelScope.launch {
        try {
            var jpApplicationSnapshot =
                FirebaseFirestore.getInstance().collection("JP_Applications")
                    .whereEqualTo("jpid", mJobProfileId)
                    .whereEqualTo(
                        "gigerId",
                        FirebaseAuthStateListener.getInstance()
                            .getCurrentSignInUserInfoOrThrow().uid
                    ).getOrThrow()
            var jpApplication = jpApplicationSnapshot.toObjects(JPApplicationDOJ::class.java)[0]
            _dojObserver.postValue(jpApplication)
        } catch (e: Exception) {

        }

    }

}