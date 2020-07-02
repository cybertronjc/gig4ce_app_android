package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationRepository
import com.gigforce.app.utils.Lse
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.File

class SelfiVideoViewModel constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val gigerVerificationRepository: GigerVerificationRepository = GigerVerificationRepository()
) : GigVerificationViewModel() {

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
            val fileName = it.metadata?.reference?.name.toString()
            setCompleteSelfieInfo(fileName)
        }.addOnFailureListener {
            _uploadSelfieState.value = Lse.error(it.localizedMessage)
        }
    }


    private fun setCompleteSelfieInfo(selfieVideoFileName: String) {
        runCatching {

            gigerVerificationRepository.setDataAsKeyValue(
                SelfieVideoDataModel(
                    videoPath = selfieVideoFileName,
                    verified = false
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