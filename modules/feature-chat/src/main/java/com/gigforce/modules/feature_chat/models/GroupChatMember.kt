package com.gigforce.modules.feature_chat.models

import android.os.Parcel
import android.os.Parcelable
import com.linkedin.android.spyglass.mentions.Mentionable
import com.linkedin.android.spyglass.mentions.Mentionable.MentionDeleteStyle
import com.linkedin.android.spyglass.mentions.Mentionable.MentionDisplayMode

class GroupChatMember : Mentionable {
    val name: String
    val uid : String
    val profilePicture: String

    constructor(
        name: String,
        uid : String,
        profilePicture : String
    ) {
        this.name = name
        this.uid = uid
        this.profilePicture = profilePicture
    }

    // --------------------------------------------------
    // Mentionable Implementation
    // --------------------------------------------------
    override fun getTextForDisplayMode(mode: MentionDisplayMode): String {
        return when (mode) {
            MentionDisplayMode.FULL -> name
            MentionDisplayMode.PARTIAL, MentionDisplayMode.NONE -> ""
            else -> ""
        }
    }

    override fun getDeleteStyle(): MentionDeleteStyle {
        // Note: Cities do not support partial deletion
        // i.e. "San Francisco" -> DEL -> ""
        return MentionDeleteStyle.PARTIAL_NAME_DELETE
    }

    override fun getSuggestibleId(): Int {
        return name.hashCode()
    }

    override fun getSuggestiblePrimaryText(): String {
        return name
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(uid)
        dest.writeString(profilePicture)
    }

    constructor(`in`: Parcel) {
        name = `in`.readString()!!
        uid= `in`.readString()!!
        profilePicture= `in`.readString()!!
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<GroupChatMember?> = object : Parcelable.Creator<GroupChatMember?> {
            override fun createFromParcel(`in`: Parcel): GroupChatMember? {
                return GroupChatMember(`in`)
            }

            override fun newArray(size: Int): Array<GroupChatMember?> {
                return arrayOfNulls(size)
            }
        }
    }
}