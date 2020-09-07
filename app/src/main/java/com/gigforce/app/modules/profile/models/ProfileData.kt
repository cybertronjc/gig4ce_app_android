package com.gigforce.app.modules.profile.models

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
    var invited_by: ArrayList<Invites>? = null
) {

}