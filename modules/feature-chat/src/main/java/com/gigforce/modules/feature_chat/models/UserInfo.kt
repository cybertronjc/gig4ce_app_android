package com.gigforce.modules.feature_chat.models

import com.google.firebase.firestore.PropertyName

data class UserInfo(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("profilePic")
    @set:PropertyName("profilePic")
    var profilePic: String = "",

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = ""
)