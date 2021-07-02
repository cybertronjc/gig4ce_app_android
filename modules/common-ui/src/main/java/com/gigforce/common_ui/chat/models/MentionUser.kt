package com.gigforce.common_ui.chat.models

import com.google.firebase.firestore.PropertyName

data class MentionUser(

    @get:PropertyName("startFrom")
    @set:PropertyName("startFrom")
    var startFrom: Int = -1,

    @get:PropertyName("endTo")
    @set:PropertyName("endTo")
    var endTo: Int = -1,

    @get:PropertyName("userMentionedUid")
    @set:PropertyName("userMentionedUid")
    var userMentionedUid: String ="",

    @get:PropertyName("profileName")
    @set:PropertyName("profileName")
    var profileName: String = "",

    @get:PropertyName("profilePicture")
    @set:PropertyName("profilePicture")
    var profilePicture: String = "",
)