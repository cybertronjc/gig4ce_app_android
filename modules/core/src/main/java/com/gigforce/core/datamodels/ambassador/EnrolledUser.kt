package com.gigforce.core.datamodels.ambassador

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EnrolledUser(

        @get:PropertyName("id")
        @set:PropertyName("id")
        var id: String = "",

        @get:PropertyName("uid")
        @set:PropertyName("uid")
        var uid: String = "",

        @get:PropertyName("enrolledOn")
        @set:PropertyName("enrolledOn")
        var enrolledOn: Timestamp = Timestamp.now(),

        @get:PropertyName("enrolledBy")
        @set:PropertyName("enrolledBy")
        var enrolledBy: String = "",

        @get:PropertyName("enrolledByName")
        @set:PropertyName("enrolledByName")
        var enrolledByName: String = "",

        @get:PropertyName("name")
        @set:PropertyName("name")
        var name: String = "",

        @get:PropertyName("mobileNumber")
        @set:PropertyName("mobileNumber")
        var mobileNumber: String = "",

        @get:PropertyName("profilePic")
        @set:PropertyName("profilePic")
        var profilePic: String? = null,

        @get:PropertyName("profilePic_thumbnail")
        @set:PropertyName("profilePic_thumbnail")
        var profileAvatarThumbnail: String? = null,

        @get:PropertyName("enrollmentStepsCompleted")
        @set:PropertyName("enrollmentStepsCompleted")
        var enrollmentStepsCompleted: EnrollmentStepsCompleted = EnrollmentStepsCompleted(),

        @get:PropertyName("locationLogs")
        @set:PropertyName("locationLogs")
        var locationLogs: List<LocationLog> = emptyList()

) : Parcelable

@Parcelize
data class EnrollmentStepsCompleted(
        @get:PropertyName("profilePicUploaded")
        @set:PropertyName("profilePicUploaded")
        var profilePicUploaded: Boolean = false,

        @get:PropertyName("userDetailsUploaded")
        @set:PropertyName("userDetailsUploaded")
        var userDetailsUploaded: Boolean = false,

        @get:PropertyName("currentAddressUploaded")
        @set:PropertyName("currentAddressUploaded")
        var currentAddressUploaded: Boolean = false,

        @get:PropertyName("interestUploaded")
        @set:PropertyName("interestUploaded")
        var interestUploaded: Boolean = false,

        @get:PropertyName("aadharDetailsUploaded")
        @set:PropertyName("aadharDetailsUploaded")
        var aadharDetailsUploaded: Boolean = false,

        @get:PropertyName("bankDetailsUploaded")
        @set:PropertyName("bankDetailsUploaded")
        var bankDetailsUploaded: Boolean = false,

        @get:PropertyName("panDetailsUploaded")
        @set:PropertyName("panDetailsUploaded")
        var panDetailsUploaded: Boolean = false,

        @get:PropertyName("drivingLicenseDetailsUploaded")
        @set:PropertyName("drivingLicenseDetailsUploaded")
        var drivingLicenseDetailsUploaded: Boolean = false,

        @get:PropertyName("experienceUploaded")
        @set:PropertyName("experienceUploaded")
        var experienceUploaded: Boolean = false
) : Parcelable {

    @Exclude
    fun allStepsCompleted() = aadharDetailsUploaded
            || panDetailsUploaded
            || drivingLicenseDetailsUploaded

}


@Parcelize
data class LocationLog(
        @get:PropertyName("completeAddress")
        @set:PropertyName("completeAddress")
        var completeAddress: String = "",

        @get:PropertyName("latitude")
        @set:PropertyName("latitude")
        var latitude: Double = 0.0,

        @get:PropertyName("longitude")
        @set:PropertyName("longitude")
        var longitude: Double = 0.0,

        @get:PropertyName("entryType")
        @set:PropertyName("entryType")
        var entryType: String = "",

        @get:PropertyName("addedOn")
        @set:PropertyName("addedOn")
        var addedOn: Timestamp = Timestamp.now(),

        @get:PropertyName("editedUsingMasterOtp")
        @set:PropertyName("editedUsingMasterOtp")
        var editedUsingMasterOtp: Boolean = false,
) : Parcelable