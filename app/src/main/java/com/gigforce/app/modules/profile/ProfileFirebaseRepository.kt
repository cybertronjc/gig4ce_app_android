package com.gigforce.app.modules.profile

import android.util.Log
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.core.replace
import com.gigforce.app.modules.profile.models.*
import com.gigforce.app.utils.getOrThrow
import com.gigforce.app.utils.setOrThrow
import com.gigforce.app.utils.updateOrThrow
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProfileFirebaseRepository : BaseFirestoreDBRepository() {

    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    var profileCollectionName = "Profiles"
    var tagsCollectionName = "Tags"

    var COLLECTION_NAME = "Profiles"

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }


    //var uid = "UeXaZV3KctuZ8xXLCKGF" // Test user

    fun addNewTag(tag: String) {
        firebaseDB.collection(tagsCollectionName)
                .document("all_tags").update("tagName", FieldValue.arrayUnion(tag))
    }

//    fun getProfile(): DocumentReference {
//        return firebaseDB.collection(profileCollectionName).document(uid)
//    }

    fun createEmptyProfile(
            latitude: Double = 0.0,
            longitude: Double = 0.0,
            locationAddress: String = ""
    ) {
        firebaseDB.collection(profileCollectionName).document(uid).set(
                ProfileData(
                        contact = ArrayList(
                                listOf(
                                        Contact(
                                                phone = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString(),
                                                email = ""
                                        )
                                )
                        ),
                        loginMobile = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString(),
                        createdOn = Timestamp.now(),
                        lastLoginDetails = LastLoginDetails(
                                lastLoginTime = Timestamp.now(),
                                lastLoginLocationLatitude = latitude,
                                lastLoginLocationLongitude = longitude,
                                lastLoginFromAddress = locationAddress
                        )
                )
        )
    }

    private suspend fun createAndReturnEmptyProfile(
            latitude: Double = 0.0,
            longitude: Double = 0.0,
            locationAddress: String = ""
    ): ProfileData {
        val profile = ProfileData(
                contact = ArrayList(
                        listOf(
                                Contact(
                                        phone = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString(),
                                        email = ""
                                )
                        )
                ),
                createdOn = Timestamp.now(),
                firstLogin = Timestamp.now(),
                loginMobile = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString(),
                lastLoginDetails = LastLoginDetails(
                        lastLoginTime = Timestamp.now(),
                        lastLoginLocationLatitude = latitude,
                        lastLoginLocationLongitude = longitude,
                        lastLoginFromAddress = locationAddress
                )
        )
        firebaseDB
                .collection(profileCollectionName)
                .document(uid)
                .setOrThrow(
                        profile
                )

        return profile
    }


    suspend fun updateLoginInfoIfUserProfileExistElseCreateProfile(
            latitude: Double = 0.0,
            longitude: Double = 0.0,
            locationAddress: String = ""
    ): ProfileData {
        val docRef = firebaseDB
                .collection(profileCollectionName)
                .document(uid)
                .getOrThrow()

        if (docRef.exists()) {

            val firstLoginPresent = docRef.get("firstLogin") != null

            if (firstLoginPresent) {
                firebaseDB
                        .collection(profileCollectionName)
                        .document(uid)
                        .updateOrThrow(
                                mapOf(
                                        "lastLoginDetails.lastLoginTime" to Timestamp.now(),
                                        "lastLoginDetails.lastLoginLocationLatitude" to latitude,
                                        "lastLoginDetails.lastLoginLocationLongitude" to longitude,
                                        "lastLoginDetails.lastLoginFromAddress" to locationAddress
                                )
                        )
            } else {
                firebaseDB
                        .collection(profileCollectionName)
                        .document(uid)
                        .updateOrThrow(
                                mapOf(
                                        "lastLoginDetails.lastLoginTime" to Timestamp.now(),
                                        "lastLoginDetails.lastLoginLocationLatitude" to latitude,
                                        "lastLoginDetails.lastLoginLocationLongitude" to longitude,
                                        "lastLoginDetails.lastLoginFromAddress" to locationAddress,
                                        "firstLogin" to Timestamp.now()
                                )
                        )
            }

            return getProfileData()
        } else {
            return createAndReturnEmptyProfile(latitude, longitude, locationAddress)
        }
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        for (ed in education) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("educations", FieldValue.arrayUnion(ed))
        }
    }

    fun removeProfileEducation(education: Education) {
        firebaseDB.collection(profileCollectionName).document(uid)
                .update("educations", FieldValue.arrayRemove(education))
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        for (sk in skills) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("skills", FieldValue.arrayUnion(sk))
        }
    }

    fun removeProfileSkill(skill: String) {
        firebaseDB.collection(profileCollectionName).document(uid)
                .update("skills", FieldValue.arrayRemove(skill))
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        for (ach in achievements) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("achievements", FieldValue.arrayUnion(ach))
        }
    }

    fun removeProfileAchievement(achievement: Achievement) {
        firebaseDB.collection(profileCollectionName).document(uid)
                .update("achievements", FieldValue.arrayRemove(achievement))
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        for (contact in contacts) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("contact", FieldValue.arrayUnion(contact))
                    .addOnSuccessListener {
                        Log.d("REPOSITORY", "contact added successfully!")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Repository", exception.toString())
                    }
        }
    }

    fun setProfileLanguage(languages: ArrayList<Language>) {
        for (lang in languages) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("languages", FieldValue.arrayUnion(lang))
        }
    }

    fun removeProfileLanguage(language: Language) {
        firebaseDB.collection(profileCollectionName).document(uid)
                .update("languages", FieldValue.arrayRemove(language))
    }

    fun setProfileExperience(experiences: ArrayList<Experience>) {
        for (exp in experiences) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("experiences", FieldValue.arrayUnion(exp))
        }
    }

    fun removeProfileExperience(experience: Experience) {
        firebaseDB.collection(profileCollectionName).document(uid)
                .update("experiences", FieldValue.arrayRemove(experience))
    }

    fun setProfileTags(tags: ArrayList<String>) {
        for (tag in tags) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("tags", FieldValue.arrayUnion(tag))
        }
    }

    fun setProfileAvatarName(profileAvatarName: String) {
        firebaseDB.collection(profileCollectionName)
                .document(uid).update("profileAvatarName", profileAvatarName)
    }

    fun setProfileAvatarName(
            userId: String?,
            profileAvatarName: String,
            profileAvatarNameThumbnail: String? = null
    ) {
        firebaseDB.collection(profileCollectionName)
                .document(userId ?: getUID()).update(
                        mapOf(
                                "profileAvatarName" to profileAvatarName,
                                "profilePicThumbnail" to profileAvatarNameThumbnail
                        )
                )
    }

    fun removeProfileTag(tags: ArrayList<String>) {
        for (tag in tags) {
            firebaseDB.collection(profileCollectionName)
                    .document(uid).update("tags", FieldValue.arrayRemove(tag))
        }
    }

    fun setProfileBio(bio: String) {
        firebaseDB.collection(profileCollectionName)
                .document(uid).update("bio", bio)
    }

    fun setProfileAboutMe(aboutMe: String) {
        firebaseDB.collection(profileCollectionName)
                .document(uid).update("aboutMe", aboutMe)
    }

    fun addInviteToProfile() {

    }

    suspend fun getProfileData(userId: String? = null): ProfileData = suspendCoroutine { cont ->

        getCollectionReference()
                .document(userId ?: uid)
                .get()
                .addOnSuccessListener {

                    if (it.exists()) {
                        val profileData = it.toObject(ProfileData::class.java)
                                ?: throw  IllegalStateException("unable to parse profile object")
                        profileData.id = it.id
                        cont.resume(profileData)
                    } else {
                        cont.resume(ProfileData())
                    }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
    }

    fun getProfileRef(userId: String?) = getCollectionReference().document(userId ?: getUID())

    /**
     * Don't delete while refactoring. Base Repo doesn't cover this function
     */
    fun setAddress(address: AddressFirestoreModel) {
        firebaseDB.collection(profileCollectionName).document(uid)
                .update("address", FieldValue.delete())
        firebaseDB.collection(profileCollectionName).document(uid).update("address", address)
    }

    fun updateCurrentAddress(address : AddressModel){
        firebaseDB.collection(profileCollectionName)
                .document(uid)
                .update(mapOf(
                        "address.current.firstLine" to address.firstLine,
                        "address.current.secondLine" to address.secondLine,
                        "address.current.area" to address.area,
                        "address.current.city" to address.city,
                        "address.current.state" to address.state
                ) )
    }

    suspend fun setUserAsAmbassador() {
        firebaseDB
                .collection(profileCollectionName)
                .document(uid)
                .update("isUserAmbassador", true) //TODO replace with updateOrThrow
    }

    suspend fun updateUserDetails(
            uid: String,
            phoneNumber: String,
            name: String,
            dateOfBirth: Date,
            gender: String,
            pincode: String,
            highestQualification: String
    ) {

        var profileData = getProfileDataIfExist(userId = uid)

        if (profileData == null) {
            profileData = ProfileData(
                    name = name,
                    gender = gender,
                    loginMobile = getNumberWithNineone(phoneNumber),
                    address = AddressFirestoreModel(
                            current = AddressModel(pincode = pincode)
                    ),
                    dateOfBirth = Timestamp(dateOfBirth),
                    highestEducation = highestQualification,
                    contact = ArrayList(
                            listOf(
                                    Contact(
                                            phone = phoneNumber,
                                            email = ""
                                    )
                            )
                    ),
                    createdOn = Timestamp.now(),
                    isonboardingdone = true
            )
        } else {
            profileData.apply {
                this.name = name
                this.dateOfBirth = Timestamp(dateOfBirth)
                this.gender = gender
                this.address.current.pincode = pincode
                this.highestEducation = highestQualification
                this.isonboardingdone = true
            }
        }

        firebaseDB
                .collection(profileCollectionName)
                .document(uid)
                .setOrThrow(profileData)
    }

    private fun getNumberWithNineone(phoneNumber: String): String {
        if(!phoneNumber.contains("+91"))
            return "+91"+phoneNumber
        return phoneNumber
    }

    suspend fun updateCurrentAddressDetails(
            uid: String?,
            pinCode: String,
            addressLine1: String,
            addressLine2: String,
            state: String,
            city: String,
            preferredDistanceInKm: Int,
            readyToChangeLocationForWork: Boolean,
            homeCity: String = "",
            homeState: String = "",
            howDidYouCameToKnowOfCurrentJob: String = ""
    ) {
        if (uid == null) {
            firebaseDB
                    .collection(profileCollectionName)
                    .document(getUID())
                    .updateOrThrow(
                            mapOf(
                                    "address.current.firstLine" to addressLine1,
                                    "address.current.area" to addressLine2,
                                    "address.current.pincode" to pinCode,
                                    "address.current.state" to state,
                                    "address.current.city" to city,
                                    "address.current.empty" to false
                            )
                    )
        } else {
            firebaseDB
                    .collection(profileCollectionName)
                    .document(uid)
                    .updateOrThrow(
                            mapOf(
                                    "address.current.firstLine" to addressLine1,
                                    "address.current.area" to addressLine2,
                                    "address.current.pincode" to pinCode,
                                    "address.current.state" to state,
                                    "address.current.city" to city,
                                    "address.home.state" to homeState,
                                    "address.home.city" to homeCity,
                                    "address.howDidYouCameToKnowOfCurrentJob" to howDidYouCameToKnowOfCurrentJob,
                                    "address.current.empty" to false,
                                    "address.current.preferredDistanceActive" to true,
                                    "address.current.preferred_distance" to preferredDistanceInKm,
                                    "readyToChangeLocationForWork" to readyToChangeLocationForWork
                            )
                    )
        }
    }

    suspend fun submitSkills(
            uid: String,
            interest: List<String>
    ) {

        val skills = interest.map {
            Skill(
                    id = it
            )
        }
        db.collection(profileCollectionName)
                .document(uid)
                .updateOrThrow("skills", skills)
    }

    suspend fun submitExperience(experience: Experience): Task<Void> {
        return firebaseDB.collection(profileCollectionName)
                .document(uid).update("experiences", FieldValue.arrayUnion(experience))
    }

    suspend fun submitExperience(userId: String, experience: Experience) {
        firebaseDB.collection(profileCollectionName)
                .document(userId).updateOrThrow("experiences", FieldValue.arrayUnion(experience))
    }

    suspend fun updateExistingExperienceElseAdd(userId: String, experience: Experience) {
        val profileData = getProfileData(userId = userId)

        val updatedExpList = profileData.experiences?.replace(newValue = experience) {
            it.title == experience.title
        }

        val expMatch = updatedExpList?.find { it.title == experience.title }
        val mutableExpList = updatedExpList!!.toMutableList()
        if (expMatch == null) {
            mutableExpList.add(experience)
        }

        firebaseDB
                .collection(profileCollectionName)
                .document(userId)
                .updateOrThrow("experiences", mutableExpList)
    }

    suspend fun getProfileDataIfExist(userId: String? = null): ProfileData? =
            suspendCoroutine { cont ->

                getCollectionReference()
                        .document(userId ?: uid)
                        .get()
                        .addOnSuccessListener {

                            if (it.exists()) {
                                val profileData = it.toObject(ProfileData::class.java)
                                        ?: throw  IllegalStateException("unable to parse profile object")
                                profileData.id = it.id
                                cont.resume(profileData)
                            } else {
                                cont.resume(null)
                            }
                        }
                        .addOnFailureListener {
                            cont.resumeWithException(it)
                        }
            }

}