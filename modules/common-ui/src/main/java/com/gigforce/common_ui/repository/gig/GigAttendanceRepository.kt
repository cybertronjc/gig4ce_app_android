package com.gigforce.common_ui.repository.gig

import android.content.SharedPreferences
import com.gigforce.app.data.remote.bodyOrThrow
import com.gigforce.app.data.repositoriesImpl.gigs.GigAttendanceApiModel
import com.gigforce.app.data.repositoriesImpl.gigs.GigerAttendanceService
import com.gigforce.common_ui.viewdatamodels.gig.DeclineReason
import com.gigforce.common_ui.ext.bodyOrErrorBodyElseThrow
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.logger.GigforceLogger
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GigAttendanceRepository @Inject constructor(
    private val gigAttendanceService: GigerAttendanceService,
    private val logger: GigforceLogger,
    private val firebaseFirestore: FirebaseFirestore,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val TAG = "GigAttendanceRepository"

        private const val ATTENDANCE_PRESENT = "present"
        private const val ATTENDANCE_ABSENT = "absent"

        private const val TYPE_PRESENT_CHECK_IN = "check-in"
        private const val TYPE_PRESENT_CHECK_OUT = "check-out"

        private const val DOCUMENT_GIGER_DECLINE_OPTIONS = "Gig_Decline_Reasons_Giger"
        private const val DOCUMENT_TL_DECLINE_OPTIONS = "Gig_Decline_Reasons_TL"

        private const val DEFAULT_LANGUAGE_CODE = "en"

        private const val KEY_REASON = "reason"
        private const val KEY_REASON_ID = "reason_id"
    }

    private val configurationCollectionRef: CollectionReference by lazy {
        firebaseFirestore.collection("Configuration")
    }

    suspend fun markCheckIn(
        gigId: String,
        imagePathInFirebase: String?,
        latitude: Double?,
        longitude: Double?,
        markingAddress: String?,
        locationFake: Boolean?,
        locationAccuracy: Float?,
        distanceBetweenGigAndUser : Float? = null
    ) : GigAttendanceApiModel {
        logger.d(
            TAG,
            "Marking check-in ....",
            mapOf(
                "gig-id" to gigId,
                "imagePathInFirebase" to imagePathInFirebase,
                "latitude" to latitude,
                "longitude" to longitude,
                "locationFake" to locationFake,
                "locationAccuracy" to locationAccuracy
            )
        )

        try {
            val response = gigAttendanceService.markAttendance(
                markAttendanceRequest = com.gigforce.app.data.repositoriesImpl.gigs.models.MarkAttendanceRequest(
                    gigId = gigId,
                    attendance = ATTENDANCE_PRESENT,
                    type = TYPE_PRESENT_CHECK_IN,
                    imagePathInFirebase = imagePathInFirebase,
                    latitude = latitude,
                    longitude = longitude,
                    markingAddress = markingAddress ?: "",
                    locationAccuracy = locationAccuracy,
                    locationFake = locationFake
                )
            ).bodyOrErrorBodyElseThrow()

            if (!response.status) {
                throw Exception(response.message)
            } else {
                logger.d(
                    TAG,
                    "[Success] check-in marked"
                )

                return response.data!!
            }
        } catch (e: Exception) {

            logger.e(
                TAG,
                "marking check-in through api",
                e
            )
            throw e
        }
    }


    suspend fun markCheckOut(
        gigId: String,
        imagePathInFirebase: String,
        latitude: Double?,
        longitude: Double?,
        markingAddress: String?,
        locationFake: Boolean?,
        locationAccuracy: Float?,
        distanceBetweenGigAndUser : Float?
    ) : GigAttendanceApiModel {
        logger.d(
            TAG,
            "Marking check-out....",
            mapOf(
                "gig-id" to gigId,
                "imagePathInFirebase" to imagePathInFirebase,
                "latitude" to latitude,
                "longitude" to longitude,
                "locationFake" to locationFake,
                "locationAccuracy" to locationAccuracy
            )
        )

        try {
            val response = gigAttendanceService.markAttendance(
                markAttendanceRequest = com.gigforce.app.data.repositoriesImpl.gigs.models.MarkAttendanceRequest(
                    gigId = gigId,
                    attendance = ATTENDANCE_PRESENT,
                    type = TYPE_PRESENT_CHECK_OUT,
                    imagePathInFirebase = imagePathInFirebase,
                    latitude = latitude,
                    longitude = longitude,
                    markingAddress = markingAddress ?: "",
                    locationAccuracy = locationAccuracy,
                    locationFake = locationFake
                )
            ).bodyOrErrorBodyElseThrow()

            if (!response.status) {
                throw Exception(response.message)
            } else{
                logger.d(
                    TAG,
                    "[Success] check-out marked"
                )

                return response.data!!
            }
        } catch (e: Exception) {

            logger.e(
                TAG,
                "marking check-out through api",
                e
            )
            throw e
        }
    }

    suspend fun markDecline(
        gigId: String,
        reasonId: String,
        reason: String
    ) : GigAttendanceApiModel {
        logger.d(
            TAG,
            "Declining gig....",
            mapOf(
                "gig-id" to gigId,
                "reason-id" to reasonId,
                "reason" to reason
            )
        )

        try {
            val response = gigAttendanceService.markAttendance(
                markAttendanceRequest = com.gigforce.app.data.repositoriesImpl.gigs.models.MarkAttendanceRequest(
                    gigId = gigId,
                    attendance = ATTENDANCE_ABSENT,
                    absentReason = reasonId,
                    absentReasonLocalizedText = reason
                )
            ).bodyOrErrorBodyElseThrow()

            if (!response.status) {
                throw Exception(response.message)
            } else{
                logger.d(
                    TAG,
                    "[Failure] decline marked"
                )

                return response.data!!
            }
        } catch (e: Exception) {

            logger.e(
                TAG,
                "marking decline through api",
                e
            )
            throw e
        }
    }


    suspend fun getDeclineOptions(
        loadDataForTL: Boolean
    ): List<DeclineReason> {

        val languageCode = sharedPreferences.getString(AppConstants.APP_LANGUAGE_CODE, null)
            ?: DEFAULT_LANGUAGE_CODE

        return if (loadDataForTL) {
            configurationCollectionRef.document(DOCUMENT_TL_DECLINE_OPTIONS)
        } else {
            configurationCollectionRef.document(DOCUMENT_GIGER_DECLINE_OPTIONS)
        }.getOrThrow().run {

            if (this.exists()) {
                try {

                    if (get(languageCode) != null) {
                        desearalizeDeclineOptions(this.get(languageCode) as List<HashMap<String, String?>>)
                    } else if (this.get(DEFAULT_LANGUAGE_CODE) != null) {
                        desearalizeDeclineOptions(this.get(DEFAULT_LANGUAGE_CODE) as List<HashMap<String, String?>>)
                    } else {
                        getDefaultDeclineOptions(loadDataForTL)
                    }
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "Unable to fetch decline options, Reasons not in correct format",
                        e
                    )
                }
            } else {

                throw IllegalStateException("Unable to fetch decline options, Reasons not defined in configuration")
            }
        }
    }

    private fun desearalizeDeclineOptions(
        listOnFB: List<java.util.HashMap<String, String?>>
    ): List<DeclineReason> {
        return listOnFB.map {
            DeclineReason(
                reasonId = it.get(KEY_REASON_ID) ?: "",
                reason = it.get(KEY_REASON) ?: ""
            )
        }
    }

    private fun getDefaultDeclineOptions(loadDataForTL: Boolean): List<DeclineReason> {
        return emptyList()
    }

    suspend fun resolveAttendanceConflict(
        resolveId: String,
        optionSelected: Boolean
    ) : GigAttendanceApiModel {
        val response = gigAttendanceService.resolveAttendanceConflict(
            com.gigforce.app.data.repositoriesImpl.gigs.models.ResolveAttendanceRequest(
                optionSelected = com.gigforce.app.data.repositoriesImpl.gigs.models.ResolveAttendanceRequestOptions.fromBoolean(
                    optionSelected
                ),
                resolveId = resolveId
            )
        ).bodyOrErrorBodyElseThrow()

        if (!response.status) {
            throw Exception(response.message)
        } else{
            return response.data!!
        }
    }

    suspend fun getAttendanceDetails(
        gigId: String
    ): GigAttendanceApiModel {

        return gigAttendanceService.getGigDetailsWithAttendanceInfo(
            gigId
        ).bodyOrThrow()
            .firstOrNull() ?: throw Exception("No Gig details found")
    }
}