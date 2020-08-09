package com.gigforce.app.modules.gigPage

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class GigsRepository : BaseFirestoreDBRepository() {

    override fun getCollectionName(): String = COLLECTION_NAME

   open fun getCurrentUserGigs() = getCollectionReference().whereEqualTo("gigerId", getUID())

    fun markAttendance(markAttendance: GigAttendance, gigId: String) {
        getCollectionReference().document(gigId).update(markAttendance.tableName, markAttendance)
    }

    suspend fun getGig(gigId: String) = suspendCoroutine<Gig> { cont ->
        getCollectionReference()
            .document(gigId)
            .get()
            .addOnSuccessListener {

                runCatching {
                    val gig = it.toObject(Gig::class.java)
                    gig?.gigId = it.id
                    gig!!
                }.onSuccess {
                    cont.resume(it)
                }.onFailure {
                    cont.resumeWithException(it)
                }
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}