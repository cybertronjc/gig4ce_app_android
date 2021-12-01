package com.gigforce.verification.gigerVerfication

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lse
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UploadDrivingCertificateViewmodel : ViewModel() {
    val repository =
        UploadDrivingCertificateRepository()


    private val _documentUploadState: SingleLiveEvent<Lse> by lazy {
        SingleLiveEvent<Lse>();
    }
    val documentUploadState: SingleLiveEvent<Lse> get() = _documentUploadState

    fun uploadDLCer(
            mJobProfileId: String,
            frontImagePath: Uri?, type: String, title: String
    ) = viewModelScope.launch {

        _documentUploadState.postValue(Lse.loading())

        try {

            val frontImageFileNameAtServer =
                    uploadImage(frontImagePath!!)


            setInJPApplication(
                    mJobProfileId, type, title
            )
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    suspend fun setInJPApplication(
        jobProfileID: String,
            type: String,
            title: String
    ): JpApplication {
        val items = repository.getCollectionReference().whereEqualTo("jpid", jobProfileID)
                .whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        val submissions = repository.getCollectionReference().document(items.documents[0].id)
                .collection("Submissions").whereEqualTo("stepId", jobProfileID).whereEqualTo(
                        "title", title
                ).whereEqualTo("type", type).get().await()


        if (submissions?.documents.isNullOrEmpty()) {
            repository.db.collection("JP_Applications")
                    .document(items.documents[0].id).collection("Submissions")
                    .document().set(
                            mapOf(
                                    "title" to title,
                                    "type" to type,
                                    "stepId" to jobProfileID,
                                    "inserted_on" to Date()

                            )
                    ).addOnCompleteListener { complete ->
                        run {

                            if (complete.isSuccessful) {
                                val jpApplication =
                                        items.toObjects(JpApplication::class.java)[0]
                                jpApplication.activation.forEach { draft ->
                                    if (draft.title == title || draft.type == "onsite_document") {
                                        draft.isDone = true
                                        draft.status = ""

                                    }
                                }
                                repository.db.collection("JP_Applications")
                                        .document(items.documents[0].id)
                                        .update("activation", jpApplication.activation)
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                repository.db.collection("JP_Applications")
                                                    .document(items.documents[0].id).update(mapOf("updatedAt" to Timestamp.now(), "updatedBy" to FirebaseAuthStateListener.getInstance()
                                                        .getCurrentSignInUserInfoOrThrow().uid))
                                                _documentUploadState.postValue(Lse.success())

                                            }
                                        }
                            }
                        }
                    }
        } else {
            repository.db.collection("JP_Applications")
                    .document(items?.documents!![0].id)
                    .collection("Submissions")
                    .document(submissions?.documents?.get(0)?.id!!)
                    .update("isCheckoutDone", true)
                    .addOnCompleteListener { complete ->
                        if (complete.isSuccessful) {
                            val jpApplication =
                                    items.toObjects(JpApplication::class.java)[0]
                            jpApplication.activation.forEach { draft ->
                                if (draft.title == title || draft.type == "onsite_document") {
                                    draft.isDone = true
                                    draft.status = ""
                                }
                            }
                            repository.db.collection("JP_Applications")
                                    .document(items.documents[0].id)
                                    .update("activation", jpApplication.activation)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            repository.db.collection("JP_Applications")
                                                .document(items.documents[0].id).update(mapOf("updatedAt" to Timestamp.now(), "updatedBy" to FirebaseAuthStateListener.getInstance()
                                                    .getCurrentSignInUserInfoOrThrow().uid))
                                            _documentUploadState.postValue(Lse.success())
                                        }
                                    }
                        }
                    }
        }

        return toObject
    }


    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    private suspend fun uploadImage(image: Uri) =
            suspendCoroutine<String> { cont ->
                val fileNameAtServer = prepareUniqueImageName()
                val filePathOnServer = firebaseStorage.reference
                        .child("verification")
                        .child(fileNameAtServer)

                filePathOnServer
                        .putFile(image)
                        .addOnSuccessListener {
                            filePathOnServer
                                    .downloadUrl
                                    .addOnSuccessListener {
                                        cont.resume(it.toString())

                                    }.addOnFailureListener {
                                        cont.resumeWithException(it)
                                    }
                        }
                        .addOnFailureListener {
                            cont.resumeWithException(it)
                        }
            }

    private fun prepareUniqueImageName(): String {
        val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(Date())
        return repository.getUID() + timeStamp + ".jpg"
    }


}