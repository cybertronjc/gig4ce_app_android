package com.gigforce.core.datamodels.profile

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class ProfileData(
    var status: Boolean = true,
    var errormsg: String = "",
    var id: String? = null,
    var name: String = "",
    var aboutMe: String = "",
    var email: String = "",
    var bio: String = "",
    var profileAvatarName: String = "avatar.jpg",
    var isVerified: Boolean = false,
    var educations: ArrayList<Education>? = ArrayList<Education>(),

    @get:PropertyName("skills")
    @set:PropertyName("skills")
    var skills: ArrayList<Skill>? = ArrayList<Skill>(),

    var achievements: ArrayList<Achievement>? = ArrayList<Achievement>(),
    var languages: ArrayList<Language>? = ArrayList<Language>(),
    var contact: ArrayList<Contact>? = ArrayList<Contact>(),
    var experiences: List<Experience>? = ArrayList<Experience>(),
    var tags: ArrayList<String>? = ArrayList<String>(),
    var connections: Int = 0,
    var rating: Rating? = Rating(),
    var tasksDone: Int = 0,
    var address: AddressFirestoreModel = AddressFirestoreModel(),
    var ageGroup: String = "",
    var gender: String = "",
    var highestEducation: String = "",
    var workStatus: String = "",
    var isonboardingdone: Boolean = false,
    var checked: Boolean = false,
    var contactPhone: ArrayList<ContactPhone>? = null,
    var contactEmail: ArrayList<ContactEmail>? = null,
    var invited: ArrayList<Invites>? = null,
    var role_interests: ArrayList<RoleInterests>? = null,
    var gigerStatus : String = "lead",

    @get:PropertyName("companies")
    @set:PropertyName("companies")
    var companies: ArrayList<Company>? = null,

    @get:PropertyName("isUserAmbassador")
    @set:PropertyName("isUserAmbassador")
    var isUserAmbassador: Boolean = false,

    @get:PropertyName("dateOfBirth")
    @set:PropertyName("dateOfBirth")
    var dateOfBirth: Timestamp? = null,

    @get:PropertyName("enrolledBy")
    @set:PropertyName("enrolledBy")
    var enrolledBy: EnrollmentInfo? = null,

    @get:PropertyName("enrolledByLink")
    @set:PropertyName("enrolledByLink")
    var enrolledByLink:Boolean = false,

    @get:PropertyName("firstLogin")
    @set:PropertyName("firstLogin")
    var firstLogin: Timestamp? = null,

    @get:PropertyName("howYouCameToKnowAboutCurrentJob")
    @set:PropertyName("howYouCameToKnowAboutCurrentJob")
    var howYouCameToKnowAboutCurrentJob: String? = null,

    @get:PropertyName("readyToChangeLocationForWork")
    @set:PropertyName("readyToChangeLocationForWork")
    var readyToChangeLocationForWork: Boolean = false,

    @get:PropertyName("loginMobile")
    @set:PropertyName("loginMobile")
    var loginMobile: String = "",

    @get:PropertyName("profilePicThumbnail")
    @set:PropertyName("profilePicThumbnail")
    var profileAvatarThumbnail: String? = "",

    @get:PropertyName("createdOn")
    @set:PropertyName("createdOn")
    var createdOn: Timestamp = Timestamp.now(),

    @get:PropertyName("lastLoginDetails")
    @set:PropertyName("lastLoginDetails")
    var lastLoginDetails: LastLoginDetails? = null
) {

    @Exclude
    fun hasUserUploadedProfilePicture() =
        profileAvatarName.isNotBlank() && profileAvatarName != "avatar.jpg"


}

data class EnrollmentInfo(

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("enrolledOn")
    @set:PropertyName("enrolledOn")
    var enrolledOn: Timestamp? = null,

    @get:PropertyName("enrolledLocationLatitude")
    @set:PropertyName("enrolledLocationLatitude")
    var enrolledLocationLatitude: Double = 0.0,

    @get:PropertyName("enrolledLocationLongitude")
    @set:PropertyName("enrolledLocationLongitude")
    var enrolledLocationLongitude: Double = 0.0,

    @get:PropertyName("enrolledLocationAddress")
    @set:PropertyName("enrolledLocationAddress")
    var enrolledLocationAddress: String = ""

)

data class LastLoginDetails(
    @get:PropertyName("lastLoginTime")
    @set:PropertyName("lastLoginTime")
    var lastLoginTime: Timestamp? = null,

    @get:PropertyName("lastLoginLocationLatitude")
    @set:PropertyName("lastLoginLocationLatitude")
    var lastLoginLocationLatitude: Double = 0.0,

    @get:PropertyName("lastLoginLocationLongitude")
    @set:PropertyName("lastLoginLocationLongitude")
    var lastLoginLocationLongitude: Double = 0.0,

    @get:PropertyName("lastLoginFromAddress")
    @set:PropertyName("lastLoginFromAddress")
    var lastLoginFromAddress: String = ""
)