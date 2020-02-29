package com.gigforce.app.modules.onboarding.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.app.modules.onboarding.models.Profile
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.core.FirestoreClient
import com.google.firebase.storage.FirebaseStorage

class ProfileManager(val id: String) {
    /*
    Input: userId (GigerId) ie doc id of FB

     */

    private val _profileDoc: MutableLiveData<Profile> = MutableLiveData<Profile>()
    val profileDoc:LiveData<Profile>
        get() = _profileDoc

    private lateinit var profileDocRef: DocumentReference

    init {
        profileDocRef = FirebaseFirestore.getInstance().collection("Profiles").document(id)

        profileDocRef
            .addSnapshotListener { snapshot, exception ->
                if(exception == null) {

                    // post the value to LiveData
                    _profileDoc.postValue(snapshot?.toObject(Profile::class.java))
                }
            }
    }

    // update value
    fun updateValue(key:String, value:Any) {
        //TODO: update value to firestore
    }

    fun updateName(value:String) {
        updateValue("name", value)
    }
}