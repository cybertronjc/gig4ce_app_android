package com.gigforce.app.utils

import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun DocumentReference.setOrThrow(obj: Any) = suspendCoroutine<Unit?> { cont ->
    set(obj).addOnSuccessListener { cont.resume(null) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun StorageReference.putFileOrThrow(file: Uri) =
    suspendCancellableCoroutine<UploadTask.TaskSnapshot> { cont ->
        val putFileTask = putFile(file)

        cont.invokeOnCancellation {
            if (!putFileTask.isComplete)
                putFileTask.cancel()
        }

        putFileTask
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }