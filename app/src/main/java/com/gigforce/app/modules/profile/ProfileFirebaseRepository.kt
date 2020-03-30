package com.gigforce.app.modules.profile

import android.util.Log
import com.gigforce.app.modules.profile.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.model.Document
import javax.security.auth.callback.Callback

class ProfileFirebaseRepository {

    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    var profileCollectionName = "Profiles"
    var tagsCollectionName = "Tags"
    //var uid = "UeXaZV3KctuZ8xXLCKGF" // Test user

    fun addNewTag(tag: String) {
        firebaseDB.collection(tagsCollectionName)
            .document("all_tags").update("tagName", FieldValue.arrayUnion(tag))
    }

    fun getProfile(): DocumentReference {
        return firebaseDB.collection(profileCollectionName).document(uid)
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        for(ed in education) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("Education", FieldValue.arrayUnion(ed))
        }
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        for(sk in skills) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("Skill", FieldValue.arrayUnion(sk))
        }
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        for (ach in achievements) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("Achievement", FieldValue.arrayUnion(ach))
        }
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        for (contact in contacts) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("Contact", FieldValue.arrayUnion(contact))
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
                .document(uid).update("Language", FieldValue.arrayUnion(lang))
        }
    }

    fun setProfileExperience(experiences: ArrayList<Experience>) {
        for (exp in experiences) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("Experience", FieldValue.arrayUnion(exp))
        }
    }

    fun setProfileTags(tag: String) {
            firebaseDB.collection(profileCollectionName)
                .document(uid).update("Tags", FieldValue.arrayUnion(tag))
    }

    fun setProfileAvatarName(profileAvatarName: String) {
        firebaseDB.collection(profileCollectionName)
            .document(uid).update("profileAvatarName",profileAvatarName)
    }


//    fun getProfileAvatarName() {
//        firebaseDB.collection(profileCollectionName)
//            .document(uid).update("profileAvatarName", profileAvatarName)
//    }

}
