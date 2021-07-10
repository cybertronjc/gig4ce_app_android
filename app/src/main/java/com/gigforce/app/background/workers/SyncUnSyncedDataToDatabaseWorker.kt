package com.gigforce.app.background.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SyncUnSyncedDataToDatabaseWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(
    appContext = appContext,
    params = params
) {

    private val firebaseFirestore: FirebaseFirestore by lazy {
        Firebase.initialize(appContext)
        FirebaseFirestore.getInstance()
    }

    override suspend fun doWork(): Result {
        return try {
            trySyncingUnsyncedDataToServer()
            Log.d("SyncDataToDatabaseWrkr","unsynced data synced")

            Result.success()
        } catch (e: Exception) {

            if(e is CancellationException){

                Log.d("SyncDataToDatabaseWrkr","unsynced data cancelled")
                Result.retry()
            } else {

                CrashlyticsLogger.e(
                    "SyncDataToDatabaseWrkr",
                    "while syncing unsynced data",
                    e
                )
                Result.failure()
            }
        }
    }

    private suspend fun trySyncingUnsyncedDataToServer() = suspendCoroutine<Any?> { continuation ->
        firebaseFirestore.waitForPendingWrites().addOnSuccessListener {
            continuation.resume(null)
        }.addOnFailureListener {
            continuation.resumeWithException(it)
        }.addOnCanceledListener {
            continuation.resumeWithException(CancellationException("Some issue"))
        }
    }
}