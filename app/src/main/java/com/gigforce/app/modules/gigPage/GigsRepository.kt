package com.gigforce.app.modules.gigPage

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAttendance
import com.gigforce.app.utils.getOrThrow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
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

    suspend fun getTodaysUpcomingGigs(date: LocalDate): List<Gig> {

        val dateFull = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val querySnap =
            getCurrentUserGigs()
            .whereGreaterThan("startDateTime", dateFull)
            .getOrThrow()

        val tomorrow = date.plusDays(1)
        return extractGigs(querySnap)
            .filter {
                it.startDateTime!! > Timestamp.now()
                        && (it.endDateTime == null
                        || it.endDateTime!!.toLocalDate().isBefore(tomorrow))
            }

    }

    private fun extractGigs(querySnapshot: QuerySnapshot): MutableList<Gig> {
        val userGigs: MutableList<Gig> = mutableListOf()
        querySnapshot.documents.forEach { t ->
            t.toObject(Gig::class.java)?.let {
                it.gigId = t.id
                userGigs.add(it)
            }
        }
        return userGigs
    }

    companion object {
        private const val COLLECTION_NAME = "Gigs"
    }
}