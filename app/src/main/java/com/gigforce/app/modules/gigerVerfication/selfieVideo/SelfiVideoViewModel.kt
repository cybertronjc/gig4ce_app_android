package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationRepository
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.SingleLiveEvent2
import com.gigforce.app.utils.putFileOrThrow
import com.gigforce.app.utils.setOrThrow
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class SelfiVideoViewModel constructor(
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val gigerVerificationRepository: GigerVerificationRepository = GigerVerificationRepository()
) : GigVerificationViewModel() {

    private var selfieVideoUploadTask: UploadTask? = null

    private val _uploadSelfieState = SingleLiveEvent2<Lse>()
    val uploadSelfieState: LiveData<Lse> get() = _uploadSelfieState

    fun uploadSelfieVideo(videoPath: File, transcodedFile: File) = viewModelScope.launch {
        _uploadSelfieState.value = Lse.loading()


        val transcodeVideo = try {
            transcodeVideo(videoPath, transcodedFile)
        } catch (e: Exception) {
            _uploadSelfieState.value = Lse.error(e.localizedMessage)
            return@launch
        }
        val videoFile = Uri.fromFile(transcodeVideo)!!

        try {
            val fileRef =
                firebaseStorage.reference.child("$FB_SELFIE_VIDEO_FOLDER_NAME/${videoFile.lastPathSegment}")
            val taskSnapshot = fileRef.putFileOrThrow(videoFile)
            val fileName = taskSnapshot.metadata?.reference?.name.toString()
            setCompleteSelfieInfo(fileName, videoPath, transcodedFile)
        } catch (e: Exception) {
            _uploadSelfieState.value = Lse.error(e.localizedMessage)
        }
    }

    suspend fun transcodeVideo(source: File, dest: File) =
        suspendCancellableCoroutine<File> { cont ->

            val transcodeJob = Transcoder.into(dest.path)
                .addDataSource(source.path)
                .setListener(object : TranscoderListener {
                    override fun onTranscodeCompleted(successCode: Int) {
                        cont.resume(dest)
                    }

                    override fun onTranscodeProgress(progress: Double) {
                    }

                    override fun onTranscodeCanceled() {
                        cont.resumeWithException(CancellationException("Video Compresssion Cancelled"))
                    }

                    override fun onTranscodeFailed(exception: Throwable) {
                        cont.resumeWithException(exception)
                    }
                }).transcode()

            cont.invokeOnCancellation {
                if (!transcodeJob.isCancelled) {
                    transcodeJob.cancel(true)
                }
            }

        }


    private suspend fun setCompleteSelfieInfo(
        selfieVideoFileName: String,
        videoPath: File,
        transcodedFile: File
    ) {

        try {
            val model = getVerificationModel()
            model.selfie_video = SelfieVideoDataModel(
                videoPath = selfieVideoFileName,
                verified = false
            )
            gigerVerificationRepository.getDBCollection().setOrThrow(model)
        } catch (e: Exception) {
            _uploadSelfieState.value = Lse.error(e.localizedMessage)
            return
        }

        //File Cleanup
        try {
            videoPath.delete()
            transcodedFile.delete()
            _uploadSelfieState.value = Lse.success()
        } catch (e: Exception) {
            _uploadSelfieState.value = Lse.success()
        }
    }

    override fun onCleared() {
        super.onCleared()
        _uploadSelfieState.value = null

        selfieVideoUploadTask?.let {
            if (it.isInProgress) it.cancel()
        }
    }

    companion object {
        private const val FB_SELFIE_VIDEO_FOLDER_NAME = "verification_selfie_videos"
    }
}