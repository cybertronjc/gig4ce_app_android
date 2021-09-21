package com.gigforce.common_ui.chat.models

import android.os.Parcelable
import android.util.Patterns
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContactModel(

        @get:PropertyName("id")
        @set:PropertyName("id")
        var id: String? = null,

        @get:PropertyName("uid")
        @set:PropertyName("uid")
        var uid: String? = null,

        @get:PropertyName("mobile")
        @set:PropertyName("mobile")
        var mobile: String = "",

        @get:PropertyName("name")
        @set:PropertyName("name")
        var name: String? = null,

        @get:PropertyName("profileName")
        @set:PropertyName("profileName")
        var profileName: String? = null,

        @get:PropertyName("contactId")
        @set:PropertyName("contactId")
        var contactId: String? = null,

        @get:PropertyName("headerId")
        @set:PropertyName("headerId")
        var headerId: String? = null,

        @get:PropertyName("imageThumbnailPathInStorage")
        @set:PropertyName("imageThumbnailPathInStorage")
        var imageThumbnailPathInStorage: String? = null,

        @get:PropertyName("imagePathInStorage")
        @set:PropertyName("imagePathInStorage")
        var imagePathInStorage: String? = null,

        @get:PropertyName("imageUrl")
        @set:PropertyName("imageUrl")
        var imageUrl: String? = null,

        @get:PropertyName("createdOn")
        @set:PropertyName("createdOn")
        var createdOn: Timestamp = Timestamp.now(),

        @get:PropertyName("updatedOn")
        @set:PropertyName("updatedOn")
        var updatedOn: Timestamp = Timestamp.now(),

        @get:PropertyName("isGigForceUser")
        @set:PropertyName("isGigForceUser")
        var isGigForceUser: Boolean = false,

        @get:PropertyName("isUserGroupManager")
        @set:PropertyName("isUserGroupManager")
        var isUserGroupManager: Boolean = false,

        @get:PropertyName("isUserBlocked")
        @set:PropertyName("isUserBlocked")
        var isUserBlocked: Boolean = false,

        @get:PropertyName("deletedOn")
        @set:PropertyName("deletedOn")
        var deletedOn: Timestamp? = null,
) : Parcelable {

    @Exclude
    fun getUserProfileImageUrlOrPath(): String? {

        if (!imageUrl.isNullOrBlank()) {
            return imageUrl!!
        } else if (!imageThumbnailPathInStorage.isNullOrBlank()) {
            if (Patterns.WEB_URL.matcher(imageThumbnailPathInStorage).matches()) {
                return imageThumbnailPathInStorage!!
            } else {
                return if (imageThumbnailPathInStorage!!.startsWith("profile_pics/"))
                    imageThumbnailPathInStorage!!
                else
                    "profile_pics/$imageThumbnailPathInStorage"
            }
        } else if (!imagePathInStorage.isNullOrBlank()) {
            if (Patterns.WEB_URL.matcher(imagePathInStorage).matches()) {
                return imagePathInStorage!!
            } else {
                return if (imagePathInStorage!!.startsWith("profile_pics/"))
                    imagePathInStorage!!
                else
                    "profile_pics/$imagePathInStorage"
            }
        } else {
            return null
        }
    }
}