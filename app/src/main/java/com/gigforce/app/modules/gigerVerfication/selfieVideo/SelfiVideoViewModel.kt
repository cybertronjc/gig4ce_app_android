package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationRepository
import com.gigforce.app.utils.Lse
import com.gigforce.core.SingleLiveEvent2
import com.gigforce.core.utils.EventLogs.setOrThrow
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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

    private val _selfieVideoUploadState = MutableLiveData<String>()
    val selfieVideoUploadProgressState: LiveData<String> get() = _selfieVideoUploadState

    fun uploadSelfieVideo(videoPath: File, transcodedFile: File) = viewModelScope.launch {
        _uploadSelfieState.value = Lse.loading()


        val transcodeVideo = try {
            transcodeVideo(videoPath, transcodedFile)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uploadSelfieState.value = Lse.error(e.localizedMessage)
            return@launch
        }
        val videoFile = Uri.fromFile(transcodeVideo)!!

        try {
            val fileRef =
                firebaseStorage.reference.child("$FB_SELFIE_VIDEO_FOLDER_NAME/${videoFile.lastPathSegment}")
            val taskSnapshot =
                uploadselfieVideo(fileRef, videoFile) //fileRef.putFileOrThrow(videoFile)

            val fileName = taskSnapshot.metadata?.reference?.name.toString()
            setCompleteSelfieInfo(fileName, videoPath, transcodedFile)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _uploadSelfieState.value = Lse.error(e.localizedMessage)
        }
    }

    private suspend fun uploadselfieVideo(
        fileRef: StorageReference,
        videoFile: Uri
    ) = suspendCancellableCoroutine<UploadTask.TaskSnapshot> { cont ->

        val uploadSelfieVideoTask = fileRef.putFile(videoFile)

        cont.invokeOnCancellation {
            if (!uploadSelfieVideoTask.isComplete)
                uploadSelfieVideoTask.cancel()
        }

        uploadSelfieVideoTask
            .addOnSuccessListener {
                _selfieVideoUploadState.value = "Video Uploaded, Saving Info.."
                cont.resume(it)
            }
            .addOnProgressListener {
                val progress = (it.bytesTransferred.toDouble() / it.totalByteCount.toDouble()) * 100
                _selfieVideoUploadState.value =
                    "Uploading Video ${String.format("%.2f", progress)} %"
            }
            .addOnFailureListener { cont.resumeWithException(it) }

    }

    suspend fun transcodeVideo(source: File, dest: File) =
        suspendCancellableCoroutine<File> { cont ->

            val transcodeJob = Transcoder.into(dest.path)
                .addDataSource(source.path)
                .setListener(object : TranscoderListener {
                    override fun onTranscodeCompleted(successCode: Int) {
                        _selfieVideoUploadState.value = "Video Compressed"
                        cont.resume(dest)
                    }

                    override fun onTranscodeProgress(progress: Double) {
                        val progressInTens = progress * 100
                        _selfieVideoUploadState.value =
                            "Compressing Video ${String.format("%.2f", progressInTens)} %"
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
            FirebaseCrashlytics.getInstance().recordException(e)
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