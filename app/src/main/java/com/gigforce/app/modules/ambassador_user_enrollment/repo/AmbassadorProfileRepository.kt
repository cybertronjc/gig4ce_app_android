package com.gigforce.app.modules.ambassador_user_enrollment.repo

import com.gigforce.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.core.datamodels.profile.Contact
import com.gigforce.core.datamodels.profile.LastLoginDetails
import com.gigforce.core.datamodels.profile.ProfileData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AmbassadorProfileRepository : BaseFirestoreDBRepository(){

    var firebaseDB = FirebaseFirestore.getInstance()
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    var profileCollectionName = "Profiles"
    var tagsCollectionName = "Tags"

    var COLLECTION_NAME = "Profiles"

    override fun getCollectionName(): String {
        return COLLECTION_NAME
    }

    suspend fun setUserAsAmbassador() {
        firebaseDB
            .collection(profileCollectionName)
            .document(uid)
            .update("isUserAmbassador", true) //TODO replace with updateOrThrow
    }

    override fun createEmptyProfile(){
        createEmptyUserProfile()
    }
    override fun createEmptyProfile(
        latitude: Double,
        longitude: Double,
        locationAddress: String
    ) {
        createEmptyUserProfile(latitude, longitude, locationAddress)
    }

    fun createEmptyUserProfile(
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        locationAddress: String = ""
    ) {
        firebaseDB.collection(profileCollectionName).document(uid).set(
            ProfileData(
                contact = ArrayList(
                    listOf(
                        Contact(
                            phone = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString(),
                            email = ""
                        )
                    )
                ),
                loginMobile = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString(),
                createdOn = Timestamp.now(),
                enrolledByLink = false,
                firstLogin = Timestamp.now(),
                lastLoginDetails = LastLoginDetails(
                    lastLoginTime = Timestamp.now(),
                    lastLoginLocationLatitude = latitude,
                    lastLoginLocationLongitude = longitude,
                    lastLoginFromAddress = locationAddress
                )
            )
        )
    }

}