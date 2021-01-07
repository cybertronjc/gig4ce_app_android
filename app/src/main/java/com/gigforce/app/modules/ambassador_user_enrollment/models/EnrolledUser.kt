package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

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
    var enrolledBy : String ="",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name : String ="",

    @get:PropertyName("profilePic")
    @set:PropertyName("profilePic")
    var profilePic : String = "",

    @get:PropertyName("enrollmentStepsCompleted")
    @set:PropertyName("enrollmentStepsCompleted")
    var enrollmentStepsCompleted : EnrollmentStepsCompleted? = null,
    @get:PropertyName("lat")
    @set:PropertyName("lat")
    var lat : String? = null,
    @get:PropertyName("lon")
    @set:PropertyName("lon")
    var lon : String? = null
)

data class EnrollmentStepsCompleted(
    @get:PropertyName("profilePicUploaded")
    @set:PropertyName("profilePicUploaded")
    var profilePicUploaded : Boolean = false,

    @get:PropertyName("userDetailsUploaded")
    @set:PropertyName("userDetailsUploaded")
    var userDetailsUploaded : Boolean = false,

    @get:PropertyName("currentAddressUploaded")
    @set:PropertyName("currentAddressUploaded")
    var currentAddressUploaded : Boolean = false,

    @get:PropertyName("interestUploaded")
    @set:PropertyName("interestUploaded")
    var interestUploaded : Boolean = false,

    @get:PropertyName("aadharDetailsUploaded")
    @set:PropertyName("aadharDetailsUploaded")
    var aadharDetailsUploaded : Boolean = false,

    @get:PropertyName("bankDetailsUploaded")
    @set:PropertyName("bankDetailsUploaded")
    var bankDetailsUploaded : Boolean = false,

    @get:PropertyName("panDetailsUploaded")
    @set:PropertyName("panDetailsUploaded")
    var panDetailsUploaded : Boolean = false,

    @get:PropertyName("drivingLicenseDetailsUploaded")
    @set:PropertyName("drivingLicenseDetailsUploaded")
    var drivingLicenseDetailsUploaded : Boolean = false,

    @get:PropertyName("experienceUploaded")
    @set:PropertyName("experienceUploaded")
    var experienceUploaded : Boolean = false
) {

    fun allStepsCompleted() = profilePicUploaded
            && userDetailsUploaded
            && currentAddressUploaded
            && interestUploaded
            && experienceUploaded
            && aadharDetailsUploaded
            && panDetailsUploaded
            && drivingLicenseDetailsUploaded
            && bankDetailsUploaded
}