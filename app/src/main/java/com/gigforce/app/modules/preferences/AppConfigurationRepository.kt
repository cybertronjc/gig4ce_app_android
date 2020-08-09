package com.gigforce.app.modules.preferences

import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AppConfigurationRepository constructor(
    private val firebaseFireStore : FirebaseFirestore = FirebaseFirestore.getInstance()
)  {

    companion object{
        const val COLLECTION_NAME = "configurations"

        const val DOCUMENT_LANGUAGE = "languages"
    }

    suspend fun getActiveLanguages() : List<String>  = suspendCoroutine {cont ->
        firebaseFireStore
            .collection(COLLECTION_NAME)
            .document(DOCUMENT_LANGUAGE)
            .get()
            .addOnSuccessListener {

               val activeLanguages =  it.get("activeLanguages") as List<String>
                cont.resume(activeLanguages)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }


}