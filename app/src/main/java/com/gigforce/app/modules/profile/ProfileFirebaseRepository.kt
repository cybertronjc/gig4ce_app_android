package com.gigforce.app.modules.profile

import com.gigforce.app.modules.profile.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFirebaseRepository {

    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    //var uid = "UeXaZV3KctuZ8xXLCKGF" // Test user

    fun getProfile(): DocumentReference {
        return firebaseDB.collection("Profiles").document(uid)
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        for(ed in education) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Education", FieldValue.arrayUnion(ed))
        }
    }

    fun setProfileSkill(skills: ArrayList<Skill>) {
        for(sk in skills) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Skill", FieldValue.arrayUnion(sk))
        }
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        for (ach in achievements) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Achievement", FieldValue.arrayUnion(ach))
        }
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        for (contact in contacts) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Contact", FieldValue.arrayUnion(contact))
        }
    }

    fun setProfileLanguage(languages: ArrayList<Language>) {
        for (lang in languages) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Language", FieldValue.arrayUnion(lang))
        }
    }
}