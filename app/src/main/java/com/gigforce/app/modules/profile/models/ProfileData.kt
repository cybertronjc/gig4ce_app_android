package com.gigforce.app.modules.profile.models

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
    var skills: ArrayList<Skill>? = ArrayList<Skill>(),
    var achievements: ArrayList<Achievement>? = ArrayList<Achievement>(),
    var languages: ArrayList<Language>? = ArrayList<Language>(),
    var contact: ArrayList<Contact>? = ArrayList<Contact>(),
    var experiences: ArrayList<Experience>? = ArrayList<Experience>(),
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

    @get:PropertyName("companies")
    @set:PropertyName("companies")
    var companies: ArrayList<Company>? = null,

    @get:PropertyName("isUserAmbassador")
    @set:PropertyName("isUserAmbassador")
    var isUserAmbassador: Boolean = false,

    @get:PropertyName("dateOfBirth")
    @set:PropertyName("dateOfBirth")
    var dateOfBirth: Timestamp = Timestamp.now()

) {

    @Exclude
    fun hasUserUploadedProfilePicture() = profileAvatarName.isNotBlank() && profileAvatarName != "avatar.jpg"
}