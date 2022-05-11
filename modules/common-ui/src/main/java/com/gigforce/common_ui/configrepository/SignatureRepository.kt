package com.gigforce.common_ui.configrepository

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.metaDataHelper.FileMetaDataExtractor
import com.gigforce.common_ui.remote.SignatureImageService
import com.gigforce.core.date.DateUtil
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.gigforce.common_ui.remote.verification.VerificationKycService
import com.gigforce.common_ui.viewdatamodels.verification.SubmitSignatureRequest
import com.gigforce.common_ui.viewdatamodels.verification.signature.SignatureUploadResponse
import com.gigforce.core.extensions.getDownloadUrlOrReturnNull


@Singleton
class SignatureRepository @Inject constructor(
    private val signatureImageService: SignatureImageService,
    private val firebaseStorage: FirebaseStorage,
    private val metaDataExtractor: FileMetaDataExtractor,
    private val verificationKycService: VerificationKycService
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

    suspend fun uploadSignature(
        uri : Uri,
        userId : String
    ) : SignatureUploadResponse {

        val imagePathInFirebase = uploadSignatureImageToFirebase(uri)
        val fullImageUrl = createFullUrl(imagePathInFirebase)
        updateSignatureInVerification(
            SubmitSignatureRequest(
                updateForUserId = userId,
                signatureFirebasePath = imagePathInFirebase,
                signatureImageFullUrl = fullImageUrl,
                backgroundRemoved = false
            )
        )

        return SignatureUploadResponse(
            signatureFirebasePath = imagePathInFirebase,
            signatureFullUrl = fullImageUrl,
            backgroundRemoved = false
        )
    }

    private suspend fun uploadSignatureImageToFirebase(
        uri: Uri
    ): String = suspendCancellableCoroutine { cont ->

        val uploadFileTask = firebaseStorage
            .reference
            .child(DIRECTORY_SIGNATURES)
            .child(createImageFile(uri))
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

    private suspend fun updateSignatureInVerification(
        submitSignatureRequest: SubmitSignatureRequest
    ){

        verificationKycService
            .uploadSignature(submitSignatureRequest)
            .bodyOrThrow()
    }

    private fun createImageFile(
        uri : Uri
    ): String {
       val fileMimeType =  try {
            metaDataExtractor.getMimeTypeOrThrow(uri)
        } catch (e: Exception) {
            MimeTypes.PNG
        }

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(fileMimeType)
        return "IMG-${DateUtil.getFullDateTimeStamp()}.$extension"
    }

    private suspend fun createFullUrl(
        imagePathOnFirebase: String
    ): String {
        return firebaseStorage
            .reference
            .child(imagePathOnFirebase)
            .getDownloadUrlOrReturnNull()?.toString() ?: ""
    }

}