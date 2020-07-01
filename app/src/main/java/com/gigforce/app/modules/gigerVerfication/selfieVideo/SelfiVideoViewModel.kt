package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationRepository
import com.gigforce.app.utils.Lse
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

class SelfiVideoViewModel constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val gigerVerificationRepository: GigerVerificationRepository = GigerVerificationRepository()
) : ViewModel() {

    private var selfieVideoUploadTask: UploadTask? = null

    private val _uploadSelfieState = MutableLiveData<Lse>()
    val uploadSelfieState: LiveData<Lse> get() = _uploadSelfieState

    fun uploadSelfieVideo(videoPath: File) {
        val videoFile = Uri.fromFile(videoPath)
        val fileRef =
            firebaseStorage.reference.child("$FB_SELFIE_VIDEO_FOLDER_NAME/${videoFile.lastPathSegment}")

        _uploadSelfieState.value = Lse.loading()
        selfieVideoUploadTask = fileRef.putFile(videoFile)
        selfieVideoUploadTask!!.addOnSuccessListener {
            getDownloadOfUploadedSelfieVideo(fileRef)
        }.addOnFailureListener {
            _uploadSelfieState.value = Lse.error(it.localizedMessage)
        }
    }

    private fun getDownloadOfUploadedSelfieVideo(fileRef: StorageReference) {
        fileRef.downloadUrl
            .addOnSuccessListener {
                setCompleteSelfieInfo(it)
            }.addOnFailureListener {
                _uploadSelfieState.value = Lse.error(it.localizedMessage)
            }
    }

    private fun setCompleteSelfieInfo(selfieVideoDownloadUri: Uri) {
        runCatching {

            gigerVerificationRepository.setDataAsKeyValue(
                SelfieVideoDataModel(
                    videoPath = selfieVideoDownloadUri.toString()
                )
            )
        }.onFailure {
            _uploadSelfieState.value = Lse.error(it.localizedMessage)
        }.onSuccess {
            _uploadSelfieState.value = Lse.success()
        }
    }

    override fun onCleared() {
        super.onCleared()
        selfieVideoUploadTask?.let {
            if (it.isInProgress) it.cancel()
        }
    }

    companion object {
        private const val FB_SELFIE_VIDEO_FOLDER_NAME = "verification_selfie_videos"
    }
}