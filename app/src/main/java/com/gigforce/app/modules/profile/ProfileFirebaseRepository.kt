package com.gigforce.app.modules.profile

import com.gigforce.app.modules.profile.models.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFirebaseRepository {

    var firebaseDB = FirebaseFirestore.getInstance()
    //var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    var uid = "UeXaZV3KctuZ8xXLCKGF" // Test user

    fun getProfile(): DocumentReference {
        return firebaseDB.collection("user_profiles").document(uid)
    }

    fun setProfileEducation(education: ArrayList<Education>) {
        for(ed in education) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Education", FieldValue.arrayUnion(
                    mapOf<String, Any>(
                        "institution" to ed.institution,
                        "course" to ed.course,
                        "degree" to ed.degree,
                        "startYear" to ed.startYear!!,
                        "endYear" to ed.endYear!!
                    )
                ))
        }
    }

    fun setProfileSkill(skills: ArrayList<Skill>) {
        for(sk in skills) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Skill", FieldValue.arrayUnion(
                    mapOf<String, Any>(
                        "category" to sk.category,
                        "nameOfSkill" to sk.nameOfSkill
                    )
                ))
        }
    }

    fun setProfileAchievement(achievements: ArrayList<Achievement>) {
        for (ach in achievements) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Achievement", FieldValue.arrayUnion(
                    mapOf<String, Any> (
                        "title" to ach.title,
                        "issuingAuthority" to ach.issuingAuthority,
                        "location" to ach.location,
                        "year" to ach.year!!
                    )
                ))
        }
    }

    fun setProfileContact(contacts: ArrayList<Contact>) {
        for (contact in contacts) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Contact", FieldValue.arrayUnion(
                    mapOf<String, Any> (
                        "phone" to contact.phone,
                        "email" to contact.email
                    )
                ))
        }
    }

    fun setProfileLanguage(languages: ArrayList<Language>) {
        for (lang in languages) {
            firebaseDB.collection("user_profiles")
                .document(uid).update("Language", FieldValue.arrayUnion(
                    mapOf<String, Any> (
                        "name" to lang.name,
                        "speakingSkill" to lang.speakingSkill,
                        "writingSkill" to lang.writingSkill
                    )
                ))
        }
    }
}