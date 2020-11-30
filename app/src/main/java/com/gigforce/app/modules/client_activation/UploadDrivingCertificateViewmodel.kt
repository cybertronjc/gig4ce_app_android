package com.gigforce.app.modules.client_activation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.DrivingCertificate
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UploadDrivingCertificateViewmodel : ViewModel() {
    val repository = UploadDrivingCertificateRepository()


    private val _documentUploadState: SingleLiveEvent<Lse> by lazy {
        SingleLiveEvent<Lse>();
    }
    val documentUploadState: SingleLiveEvent<Lse> get() = _documentUploadState

    fun uploadDLCer(
            mWorkOrderID: String,
            frontImagePath: Uri?
    ) = viewModelScope.launch {

        _documentUploadState.postValue(Lse.loading())

        try {
            val model = getJPApplication(mWorkOrderID)

            val frontImageFileNameAtServer =
                    uploadImage(frontImagePath!!)


            model.drivingCert = DrivingCertificate(
                    verified = false,
                    frontImage = frontImageFileNameAtServer

            )
            repository.getCollectionReference().document(model.id).set(model)
            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    suspend fun getJPApplication(workOrderID: String): JpApplication {
        val items = repository.getCollectionReference().whereEqualTo("jpid", workOrderID).whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        val toObject = items.toObjects(JpApplication::class.java).get(0)
        toObject.id = items.documents[0].id
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