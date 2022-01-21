package com.gigforce.common_ui.chat.models

import com.gigforce.core.SimpleDVM
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class MessageReceivingInfo(

        @get:PropertyName("uid")
        @set:PropertyName("uid")
        var uid: String = "",

        @get:PropertyName("readOn")
        @set:PropertyName("readOn")
        var readOn: Timestamp? = null,

        @get:PropertyName("deliveredOn")
        @set:PropertyName("deliveredOn")
        var deliveredOn: Timestamp? = null,

        @get:PropertyName("profileName")
        @set:PropertyName("profileName")
        var profileName: String = "",

        @get:PropertyName("profilePicture")
        @set:PropertyName("profilePicture")
        var profilePicture: String = "",
) : SimpleDVM(ViewTypes.GROUP_MESSAGE_READ_INFO)


