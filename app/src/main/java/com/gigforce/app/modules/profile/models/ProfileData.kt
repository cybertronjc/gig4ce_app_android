package com.gigforce.app.modules.profile.models

import kotlin.collections.ArrayList

data class ProfileData(
    val id: String? = null,
    var name: String = "",
    var aboutMe: String = "",
    var email: String = "",
    var bio: String = "",
    var profileAvatarName: String = "avatar.jpg",
    var isVerified: Boolean = false,
    var educations: ArrayList<Education>? = ArrayList<Education>(),
    var skills: ArrayList<String>? = ArrayList<String>(),
    var achievements: ArrayList<Achievement>? = ArrayList<Achievement>(),
    var languages: ArrayList<Language>? = ArrayList<Language>(),
    var contact: ArrayList<Contact>? = ArrayList<Contact>(),
    var experiences: ArrayList<Experience>? = ArrayList<Experience>(),
    var tags: ArrayList<String>? = ArrayList<String>(),
    var connections: Int = 0,
    var rating: Rating? = null,
    var tasksDone: Int = 0,
    var address:AddressFirestoreModel = AddressFirestoreModel()


) {

}