package com.gigforce.app.modules.chatmodule.repository

import android.util.Log
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.chatmodule.models.ContactModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.commitOrThrow
import com.gigforce.app.utils.getDownloadUrlOrThrow
import com.gigforce.app.utils.getOrThrow
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ChatContactsRepository constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : BaseFirestoreDBRepository() {

    private val userChatCollectionRef: DocumentReference by lazy {
        FirebaseFirestore.getInstance()
            .collection(COLLECTION_CHATS)
            .document(getUID())
    }

    private val userChatContactsCollectionRef: CollectionReference by lazy {
        userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
    }


    override fun getCollectionName(): String {
        return COLLECTION_CHATS
    }

    fun getUserContacts(): CollectionReference {
        return userChatCollectionRef
            .collection(COLLECTION_CHATS_CONTACTS)
    }


    suspend fun updateContacts(newContacts: List<ContactModel>) {

        val querySnap = getUserContacts().getOrThrow()
        val oldContacts = querySnap.documents.map {
            it.toObject(ContactModel::class.java)!!.apply {
                id = it.id
            }
        }

        val allProfilesQuery = db.collection("Profiles").getOrThrow()
        val allProfiles = allProfilesQuery.documents.map {
            val profile = it.toObject(ProfileData::class.java)!!
            profile.id = it.id
            profile
        }

        val sum = oldContacts + newContacts
        val newAddedContacts = sum.groupBy { it.mobile }
            .filter { it.value.size == 1 }
            .flatMap { it.value }

        if (newAddedContacts.isNotEmpty()) {
            val batch = db.batch()
            newAddedContacts.forEach { pickedContact ->

                Log.d("Tag","Check")

                val profile = allProfiles.find { profileData ->

                    var contactFound = false
                    for (c in profileData.contact!!) {
                        if (c.phone.contains(pickedContact.mobile)) {
                            contactFound = true
                            break
                        }
                    }

                    contactFound
                }

                if (profile != null) {
                    pickedContact.isGigForceUser = true
                    pickedContact.uid = profile.id

                    pickedContact.imageUrl =
                        if (profile.profileAvatarName.isBlank() || profile.profileAvatarName == "avatar.jpg")
                            null
                        else {
                            firebaseStorage
                                .reference
                                .child("profile_pics")
                                .child(profile.profileAvatarName)
                                .getDownloadUrlOrThrow().toString()
                        }

                }
                val contactRef = userChatContactsCollectionRef.document(pickedContact.mobile)
                batch.set(contactRef, pickedContact)
            }
            batch.commitOrThrow()
        }
    }

    companion object {
        const val COLLECTION_CHATS = "chats"
        const val COLLECTION_CHATS_CONTACTS = "contacts"
    }

}