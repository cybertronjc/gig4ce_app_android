package com.gigforce.modules.feature_chat.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class ChatProfileData(

        var id: String? = null,
        var name: String = "",
        var profileAvatarName: String = "avatar.jpg",
        var loginMobile: String = ""
) {

    @Exclude
    fun hasUserUploadedProfilePicture() =
            profileAvatarName.isNotBlank() && profileAvatarName != "avatar.jpg"


}
