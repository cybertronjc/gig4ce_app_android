package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.utils.Lse
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class SelfiVideoViewModel constructor(
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : GigVerificationViewModel() {

    private val _uploadSelfieState = MutableLiveData<Lse>()
    val uploadSelfieState: LiveData<Lse> get() = _uploadSelfieState

    fun uploadSelfieVideo(videoPath: File) {
        val videoFile = Uri.fromFile(videoPath)
        val fileRef = firebaseStorage.reference.child("$FB_SELFIE_VIDEO_FOLDER_NAME/${videoFile.lastPathSegment}")

        _uploadSelfieState.value = Lse.loading()
        fileRef.putFile(videoFile)
                .addOnSuccessListener {
                    _uploadSelfieState.value = Lse.success()
                }
                .addOnFailureListener {
                    _uploadSelfieState.value = Lse.error(it.localizedMessage)
                }
    }


    companion object {
        private const val FB_SELFIE_VIDEO_FOLDER_NAME = "verification_selfie_videos"
    }
}