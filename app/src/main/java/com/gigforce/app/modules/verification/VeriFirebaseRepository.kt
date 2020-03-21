package com.gigforce.app.modules.verification

import android.util.Log
import com.gigforce.app.modules.profile.models.*
import com.gigforce.app.modules.verification.models.Contact_Verification
import com.gigforce.app.modules.verification.models.VerificationData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.model.Document
import javax.security.auth.callback.Callback

class VeriFirebaseRepository {

    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    var verificationCollectionName = "Verification"

    fun getProfile(): DocumentReference {
        return firebaseDB.collection(verificationCollectionName).document(uid)
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        for(ed in education) {
            firebaseDB.collection(verificationCollectionName)
                .document(uid).update("Education", FieldValue.arrayUnion(ed))
        }
    }

    fun setProfileSkill(skills: ArrayList<String>) {
        for(sk in skills) {
            firebaseDB.collection(verificationCollectionName)
                .document(uid).update("Skill", FieldValue.arrayUnion(sk))
        }
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        for (ach in achievements) {
            firebaseDB.collection(verificationCollectionName)
                .document(uid).update("Achievement", FieldValue.arrayUnion(ach))
        }
    }

    fun setVeriContact(contacts: ArrayList<Contact_Verification>) {
        for (contact in contacts) {
            firebaseDB.collection(verificationCollectionName)
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
            firebaseDB.collection(verificationCollectionName)
                .document(uid).update("Language", FieldValue.arrayUnion(lang))
        }
    }

    fun setProfileExperience(experiences: ArrayList<Experience>) {
        for (exp in experiences) {
            firebaseDB.collection(verificationCollectionName)
                .document(uid).update("Experience", FieldValue.arrayUnion(exp))
        }
    }

    fun setProfileTags(tag: String) {
        firebaseDB.collection(verificationCollectionName)
            .document(uid).update("Tags", FieldValue.arrayUnion(tag))
    }
}
