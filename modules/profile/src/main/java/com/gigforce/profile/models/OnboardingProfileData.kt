package com.gigforce.profile.models

import com.google.firebase.firestore.Exclude

data class OnboardingProfileData(

        var id: String? = null,
        var name: String = "",
        var profileAvatarName: String = "avatar.jpg"
) {

    @Exclude
    fun hasUserUploadedProfilePicture() =
            profileAvatarName.isNotBlank() && profileAvatarName != "avatar.jpg"


}