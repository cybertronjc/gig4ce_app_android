package com.gigforce.core.extensions

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StreamDownloadTask
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
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

suspend fun StorageReference.putBytesOrThrow(bytes: ByteArray) =
    suspendCancellableCoroutine<UploadTask.TaskSnapshot> { cont ->
        val putFileTask = putBytes(bytes)

        cont.invokeOnCancellation {
            if (!putFileTask.isComplete)
                putFileTask.cancel()
        }

        putFileTask
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

suspend fun Query.getOrThrow() = suspendCoroutine<QuerySnapshot> { cont ->
    get().addOnSuccessListener {
        cont.resume(it)
    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}

suspend fun CollectionReference.addOrThrow(data: Any) =
    suspendCoroutine<DocumentReference> { cont ->
        add(data).addOnSuccessListener {
            cont.resume(it)
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

suspend fun DocumentReference.getOrThrow() = suspendCoroutine<DocumentSnapshot> { cont ->
    get().addOnSuccessListener {
        cont.resume(it)
    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}

suspend inline fun <reified T> DocumentReference.getAndSerializeOrThrow() = suspendCoroutine<T> { cont ->

    get().addOnSuccessListener {

        if(!it.exists()){
            cont.resumeWithException(Exception("no document exist for ${it.id}"))
            return@addOnSuccessListener
        }

       try {
           val serializedData = it.toObject(T::class.java) ?: throw Exception("unable to serialize data to required object")
           cont.resume(serializedData)
       } catch (e: Exception) {
           cont.resumeWithException(e)
       }

    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}

suspend inline fun <reified T> DocumentReference.getOrNull() = suspendCoroutine<T?> { cont ->

    get().addOnSuccessListener {

        if(!it.exists()){
            cont.resume(null)
            return@addOnSuccessListener
        }

        try {
            val serializedData = it.toObject(T::class.java) ?: throw Exception("unable to serialize data to required object")
            cont.resume(serializedData)
        } catch (e: Exception) {
            cont.resume(null)
        }

    }.addOnFailureListener {
        cont.resume(null)
    }
}

suspend fun WriteBatch.commitOrThrow() = suspendCoroutine<Void?> { cont ->
    commit().addOnSuccessListener {
        cont.resume(null)
    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}

suspend fun DocumentReference.updateOrThrow(field: String, value: Any) =
    suspendCoroutine<Void?> { cont ->
        update(field, value).addOnSuccessListener {
            cont.resume(it)
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

suspend fun DocumentReference.updateOrThrow(values: Map<String, Any?>) =
    suspendCoroutine<Void?> { cont ->
        update(values).addOnSuccessListener {
            cont.resume(it)
        }.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }

suspend fun StorageReference.getDownloadUrlOrThrow() = suspendCoroutine<Uri> { cont ->
    downloadUrl.addOnSuccessListener {
        cont.resume(it)
    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}

suspend fun StorageReference.getDownloadUrlOrReturnNull() = suspendCoroutine<Uri?> { cont ->
    downloadUrl.addOnSuccessListener {
        cont.resume(it)
    }.addOnFailureListener {
        cont.resume(null)
    }
}

suspend fun StorageReference.getFileOrThrow(
    destinationFile: File
) = suspendCoroutine<FileDownloadTask.TaskSnapshot> { cont ->

    getFile(destinationFile).addOnSuccessListener {

        cont.resume(it)
    }.addOnFailureListener {

        cont.resumeWithException(it)
    }
}


suspend fun DocumentReference.deleteOrThrow() = suspendCoroutine<Unit?> { cont ->
    delete().addOnSuccessListener { cont.resume(null) }
        .addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun StorageReference.getFileOrThrow(destinationFile: Uri) =
    suspendCoroutine<FileDownloadTask.TaskSnapshot> { cont ->
        getFile(destinationFile).addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

suspend fun StorageReference.getStreamOrThrow(
    streamProcessor: StreamDownloadTask.StreamProcessor
) = suspendCoroutine<StreamDownloadTask.TaskSnapshot> { cont ->
    getStream(streamProcessor)
        .addOnSuccessListener { cont.resume(it) }
        .addOnFailureListener { cont.resumeWithException(it) }
}


