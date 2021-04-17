package com.gigforce.profile.repository

import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.profile.models.OnboardingProfileData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class OnboardingProfileFirebaseRepository : BaseFirestoreDBRepository() {

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

    fun setProfileThumbNail(profileAvatarName: String) {
        firebaseDB.collection(profileCollectionName)
                .document(uid).update("profilePicThumbnail", profileAvatarName)

    }

    fun setProfileAvatarName(
            profileAvatarName: String,
            profileAvatarNameThumbnail: String? = null
    ) {
        firebaseDB.collection(profileCollectionName)
                .document(getUID()).update(
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

    suspend fun getProfileData(userId: String? = null): OnboardingProfileData =
            suspendCoroutine { cont ->

                getCollectionReference()
                        .document(userId ?: uid)
                        .get()
                        .addOnSuccessListener {

                            if (it.exists()) {
                                val profileData = it.toObject(OnboardingProfileData::class.java)
                                        ?: throw  IllegalStateException("unable to parse profile object")
                                profileData.id = it.id
                                cont.resume(profileData)
                            } else {
                                cont.resume(OnboardingProfileData())
                            }
                        }
                        .addOnFailureListener {
                            cont.resumeWithException(it)
                        }
            }

    suspend fun setPreferredJobLocation(cityId: String, cityName: String, stateCode: String, subLocation: List<String>) {
        firebaseDB.collection(profileCollectionName)
                .document(uid)
                .updateOrThrow(mapOf(
                        "preferredJobLocation.city_id" to cityId,
                        "preferredJobLocation.city_name" to cityName,
                        "preferredJobLocation.sub_location" to subLocation,
                        "preferredJobLocation.state_code" to stateCode
                ))
    }


}