package com.gigforce.app.utils

import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun DocumentReference.setOrThrow(obj: Any) = suspendCoroutine<Unit?> { cont ->
    set(obj).addOnSuccessListener { cont.resume(null) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun StorageReference.putFileOrThrow(file : Uri) = suspendCoroutine<UploadTask.TaskSnapshot> { cont ->
    putFile(file).addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
}