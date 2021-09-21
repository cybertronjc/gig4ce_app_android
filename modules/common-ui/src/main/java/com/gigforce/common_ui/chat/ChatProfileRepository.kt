package com.gigforce.modules.feature_chat.repositories

import com.gigforce.core.fb.BaseFirestoreDBRepository
import com.gigforce.common_ui.chat.models.ChatProfileData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatProfileFirebaseRepository : BaseFirestoreDBRepository() {


    suspend fun getProfileDataIfExist(userId: String? = null): ChatProfileData? =
            suspendCoroutine { cont ->

                getCollectionReference()
                        .document(userId ?: getUID())
                        .get()
                        .addOnSuccessListener {

                            if (it.exists()) {
                                val profileData = it.toObject(ChatProfileData::class.java)
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

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    companion object {
        var COLLECTION_NAME = "Profiles"
    }
}