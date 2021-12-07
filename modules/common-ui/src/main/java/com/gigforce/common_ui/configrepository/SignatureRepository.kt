package com.gigforce.common_ui.configrepository

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.SignatureImageService
import com.gigforce.core.date.DateHelper
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class SignatureRepository @Inject constructor(
    private val signatureImageService: SignatureImageService,
    private val firebaseStorage: FirebaseStorage
) {

    companion object {
        private const val DIRECTORY_SIGNATURES = "Signatures"
    }


    suspend fun removeBackgroundFromSignature(
        uri: Uri
    ): String {
        val file = uri.toFile()
        val imageBody = RequestBody.create(MediaType.parse("image/*"), file)
        val formData = MultipartBody.Part.createFormData("file", file.name, imageBody)

        return signatureImageService.uploadSignatureImage(
            formData
        ).bodyOrThrow()
    }

    suspend fun uploadSignatureImageToFirebase(
        uri: Uri
    ): String = suspendCancellableCoroutine { cont ->

        val uploadFileTask = firebaseStorage
            .reference
            .child(DIRECTORY_SIGNATURES)
            .child(createImageFile())
            .putFile(uri)

        uploadFileTask.addOnSuccessListener {
            cont.resume(it.metadata!!.path)
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }

        cont.invokeOnCancellation {

            if(!uploadFileTask.isCanceled) {
                uploadFileTask.cancel()
            }
        }
    }

    private fun createImageFile(
        mimeType: String = MimeTypes.PNG
    ): String {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        return "IMG-${DateHelper.getFullDateTimeStamp()}.$extension"
    }
}