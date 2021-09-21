package com.gigforce.common_ui.chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class ChatGroup(

    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("groupAvatar")
    @set:PropertyName("groupAvatar")
    var groupAvatar: String = "",

    @get:PropertyName("groupAvatarThumbnail")
    @set:PropertyName("groupAvatarThumbnail")
    var groupAvatarThumbnail: String = "",

    @get:PropertyName("groupMedia")
    @set:PropertyName("groupMedia")
    var groupMedia: List<GroupMedia> = emptyList(),

    @get:PropertyName("groupMembers")
    @set:PropertyName("groupMembers")
    var groupMembers: List<ContactModel> = emptyList(),

    @get:PropertyName("deletedGroupMembers")
    @set:PropertyName("deletedGroupMembers")
    var deletedGroupMembers: List<ContactModel> = emptyList(),

    @get:PropertyName("creationDetails")
    @set:PropertyName("creationDetails")
    var creationDetails: GroupCreationDetails? = null,

    @get:PropertyName("groupDeactivated")
    @set:PropertyName("groupDeactivated")
    var groupDeactivated: Boolean = false,

    @get:PropertyName("onlyAdminCanPostInGroup")
    @set:PropertyName("onlyAdminCanPostInGroup")
    var onlyAdminCanPostInGroup: Boolean = false,

    @Exclude
    var currenUserRemovedFromGroup: Boolean = false,
    )



data class GroupCreationDetails(

    @get:PropertyName("createdBy")
    @set:PropertyName("createdBy")
    var createdBy: String = "",

    @get:PropertyName("creatorName")
    @set:PropertyName("creatorName")
    var creatorName: String = "",

    @get:PropertyName("createdOn")
    @set:PropertyName("createdOn")
    var createdOn: Timestamp = Timestamp.now()
)

