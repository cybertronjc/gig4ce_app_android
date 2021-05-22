package com.gigforce.common_ui.chat.models

import com.google.firebase.firestore.Exclude

data class ChatProfileData(

        var id: String? = null,
        var name: String = "",
        var profileAvatarName: String = "avatar.jpg"
) {

    @Exclude
    fun hasUserUploadedProfilePicture() =
            profileAvatarName.isNotBlank() && profileAvatarName != "avatar.jpg"


}
