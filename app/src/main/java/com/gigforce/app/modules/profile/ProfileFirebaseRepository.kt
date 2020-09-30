package com.gigforce.app.modules.profile

import android.util.Log
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.profile.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.model.Document
import kotlinx.coroutines.tasks.await
import javax.security.auth.callback.Callback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProfileFirebaseRepository: BaseFirestoreDBRepository() {

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

    fun createEmptyProfile() {
        firebaseDB.collection(profileCollectionName).document(uid).set(ProfileData(
            contact = ArrayList(listOf(
                Contact(phone = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString(),
                        email = "")))
        ))
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        for(ed in education) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("educations", FieldValue.arrayUnion(ed))
        }
    }

    fun removeProfileEducation(education: Education) {
        firebaseDB.collection(profileCollectionName).document(uid).update("educations", FieldValue.arrayRemove(education))
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        for(sk in skills) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("skills", FieldValue.arrayUnion(sk))
        }
    }

    fun removeProfileSkill(skill: String) {
        firebaseDB.collection(profileCollectionName).document(uid).update("skills", FieldValue.arrayRemove(skill))
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        for (ach in achievements) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("achievements", FieldValue.arrayUnion(ach))
        }
    }

    fun removeProfileAchievement(achievement: Achievement) {
        firebaseDB.collection(profileCollectionName).document(uid).update("achievements", FieldValue.arrayRemove(achievement))
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        for (contact in contacts) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("contact", FieldValue.arrayUnion(contact))
                .addOnSuccessListener {
                    Log.d("REPOSITORY", "contact added successfully!")
                }
                .addOnFailureListener{
                    exception ->  Log.d("Repository", exception.toString())
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
        firebaseDB.collection(profileCollectionName).document(uid).update("languages", FieldValue.arrayRemove(language))
    }

    fun setProfileExperience(experiences: ArrayList<Experience>) {
        for (exp in experiences) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("experiences", FieldValue.arrayUnion(exp))
        }
    }

    fun removeProfileExperience(experience: Experience) {
        firebaseDB.collection(profileCollectionName).document(uid).update("experiences", FieldValue.arrayRemove(experience))
    }

    fun setProfileTags(tags: ArrayList<String>) {
        for (tag in tags) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("tags", FieldValue.arrayUnion(tag))
        }
    }

    fun setProfileAvatarName(profileAvatarName: String) {
        firebaseDB.collection(profileCollectionName)
            .document(uid).update("profileAvatarName",profileAvatarName)
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
    fun addInviteToProfile(){

    }

    suspend fun getProfileData() : ProfileData = suspendCoroutine {cont ->

        getCollectionReference()
            .document(uid)
            .get()
            .addOnSuccessListener {
                val profileData = it.toObject(ProfileData::class.java) ?: throw  IllegalStateException("unable to parse profile object")
                cont.resume(profileData)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    /**
     * Don't delete while refactoring. Base Repo doesn't cover this function
     */
    fun setAddress(address: AddressFirestoreModel){
        firebaseDB.collection(profileCollectionName).document(uid).update("address",FieldValue.delete())
        firebaseDB.collection(profileCollectionName).document(uid).update("address",address)
    }
}