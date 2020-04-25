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
    var Education: ArrayList<Education>? = ArrayList<Education>(),
    var Skill: ArrayList<String>? = ArrayList<String>(),
    var Achievement: ArrayList<Achievement>? = ArrayList<Achievement>(),
    var Language: ArrayList<Language>? = ArrayList<Language>(),
    var Contact: ArrayList<Contact>? = ArrayList<Contact>(),
    var Experience: ArrayList<Experience>? = ArrayList<Experience>(),
    var Tags: ArrayList<String>? = ArrayList<String>(),
    var connections: Int = 0,
    var rating: Rating? = null,
    var tasksDone: Int = 0,
    var address:AddressFirestoreModel = AddressFirestoreModel()


) {

}