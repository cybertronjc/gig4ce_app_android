package com.gigforce.app.utils

import com.google.firebase.firestore.DocumentReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun DocumentReference.setOrThrow(obj: Any) = suspendCoroutine<Unit?> { cont ->
    set(obj).addOnSuccessListener { cont.resume(null) }
        .addOnFailureListener { cont.resumeWithException(it) }
}