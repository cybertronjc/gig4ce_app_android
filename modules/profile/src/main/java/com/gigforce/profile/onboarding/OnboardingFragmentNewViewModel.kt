package com.gigforce.profile.onboarding

import androidx.lifecycle.ViewModel
import com.gigforce.profile.datamodel.EnrollmentInfo
import com.gigforce.profile.datamodel.Invites
import com.gigforce.profile.datamodel.ProfileData
import com.gigforce.profile.onboarding.adapter.MultiviewsAdapter
import com.gigforce.profile.onboarding.models.*
import com.gigforce.profile.repo.ProfileFirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class OnboardingFragmentNewViewModel : ViewModel() {

    var profileFirebaseRepository = ProfileFirebaseRepository()

    fun saveUserName(username: String) {
        profileFirebaseRepository.setDataAsKeyValue("name", username)
    }

    fun saveAgeGroup(ageGroup: String) {
        profileFirebaseRepository.setDataAsKeyValue("ageGroup", ageGroup)


    }

    fun selectYourGender(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("gender", selectedDataFromRecycler)
    }

    fun saveHighestQualification(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("highestEducation", selectedDataFromRecycler)
    }

    fun saveWorkStatus(selectedDataFromRecycler: String) {
        profileFirebaseRepository.setDataAsKeyValue("workStatus", selectedDataFromRecycler)
    }

    fun saveTotalExperience(totalExperience: String) {
        profileFirebaseRepository.setDataAsKeyValue("totalExperience", totalExperience)

    }

    fun saveInterest(selectedInterest: ArrayList<String>) {
        profileFirebaseRepository.setData("interests", selectedInterest)

    }
    fun saveJobPreference(fullTimeJob: Boolean) {
        profileFirebaseRepository.setDataAsKeyValue("jobType", fullTimeJob)

    }

    fun saveDaysPreference(workingDays: ArrayList<String>) {
        profileFirebaseRepository.setData("workingDays",workingDays)
    }

    fun saveTimeSlots(timeSlots: ArrayList<String>) {
        profileFirebaseRepository.setData("timeSlots",timeSlots)
    }
    fun saveAssets(assetsData: Map<String, Any>) {
        profileFirebaseRepository.setData(assetsData)
    }
    fun onboardingCompleted(){
        profileFirebaseRepository.setDataAsKeyValue("isonboardingdone", true)
    }
    fun setOnboardingCompleted(
        invite: String?,
        inviteByAmbassador: String,
        ambassadorLatitude:Double,
        ambassadorLongitude:Double,
        roleID: String,
        jobProfileId: String,username:String
    ) {
        if (!invite.isNullOrEmpty()) {
            var listener: ListenerRegistration? = null
            listener = profileFirebaseRepository.getCollectionReference()
                .document(invite).addSnapshotListener { snapshot, err ->
                    listener?.remove()
                    run {
                        val obj = snapshot?.toObject(ProfileData::class.java) ?: return@run
                        if (obj?.invited == null) {
                            profileFirebaseRepository.getCollectionReference()
                                .document(invite)
                                .update(
                                    "invited",
                                    arrayListOf(
                                        Invites(
                                            profileFirebaseRepository.getUID(),
                                            Date(),
                                            roleID,
                                            jobProfileId
                                        )
                                    )
                                )

//                                    EnrolledUser(uid = profileFirebaseRepository.uid,enrolledBy = invite, enrolledByLink = true )
                        } else {
                            profileFirebaseRepository.getCollectionReference()
                                .document(invite)
                                .update(
                                    "invited",
                                    FieldValue.arrayUnion(
                                        Invites(
                                            profileFirebaseRepository.getUID(),
                                            Date(), roleID, jobProfileId
                                        )
                                    )
                                )

                        }
                        if (inviteByAmbassador.isNotBlank()) {

                            profileFirebaseRepository.getDBCollection()
                                .update(
                                    "enrolledBy",
                                    EnrollmentInfo(id = invite, enrolledOn = Timestamp.now())

                                )
                            profileFirebaseRepository.getDBCollection()
                                .update(
                                    "enrolledByLink",
                                    true

                                )
                            profileFirebaseRepository.db.collection("Ambassador_Enrolled_User")
                                .document(invite).collection("Enrolled_Users")
                                .document(profileFirebaseRepository.uid).set(
                                    mapOf(
                                        "uid" to profileFirebaseRepository.uid,
                                        "enrolledBy" to invite,
                                        "enrolledOn" to Timestamp.now(),
                                        "enrolledByLink" to true,
                                        "name" to username,
                                        "mobileNumber" to getNumberWithoutNineone(FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()),
                                        "locationLogs" to FieldValue.arrayUnion(
                                            mapOf(
                                                "userDetailsUploaded" to true,
                                                "latitude" to ambassadorLatitude,
                                                "longitude" to ambassadorLongitude,
                                                "entryType" to "create_by_user",
                                                "addedOn" to Timestamp.now()
                                            )
                                        )
                                    )
                                )
                        }

                    }
                }


        }

        profileFirebaseRepository.setDataAsKeyValue("isonboardingdone", true)
    }

    fun getNumberWithoutNineone(mobileNumber:String):String{
        if(mobileNumber.contains("+91")){
            return mobileNumber.takeLast(10)
        }
        return mobileNumber
    }




}