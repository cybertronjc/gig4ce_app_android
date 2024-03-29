package com.gigforce.common_ui.viewdatamodels.client_activation

import android.os.Parcel
import android.os.Parcelable

data class JobProfile(
    var businessId: String? = "",
    var businessName: String? = "",
    var requiredMedia: RequiredMedia? = RequiredMedia(),
    var locationList: List<Locations>? = listOf(),
    var payoutNote: String? = "",
    var queries: List<Queries>? = listOf(),
    var id: String? = "",
    var info: List<BulletPoints>? = listOf(),
    var title: String = "",
    var nextDependency: String = "",
    var coverImg: String = "",
    var defaultPayoutRequired: Boolean = false,
    var profileId: String = "",
    var profileName: String = "",
    var totalSteps: Int = 0,
    var cardImage: String = "",
    var cardTitle:String = "",
    var subTitle: String = "",
    var priority : Long?  = 10000
//        var tags: List<String> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(RequiredMedia::class.java.classLoader),
        parcel.createTypedArrayList(Locations),
        parcel.readString(),
        parcel.createTypedArrayList(Queries),
        parcel.readString(),
        parcel.createTypedArrayList(BulletPoints),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(businessId)
        parcel.writeString(businessName)
        parcel.writeParcelable(requiredMedia, flags)
        parcel.writeTypedList(locationList)
        parcel.writeString(payoutNote)
        parcel.writeTypedList(queries)
        parcel.writeString(id)
        parcel.writeTypedList(info)
        parcel.writeString(title)
        parcel.writeString(nextDependency)
        parcel.writeString(coverImg)
        parcel.writeByte(if (defaultPayoutRequired) 1 else 0)
        parcel.writeString(profileId)
        parcel.writeString(profileName)
        parcel.writeInt(totalSteps)
        parcel.writeString(cardImage)
        parcel.writeString(cardTitle)
        parcel.writeString(subTitle)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JobProfile> {
        override fun createFromParcel(parcel: Parcel): JobProfile {
            return JobProfile(parcel)
        }

        override fun newArray(size: Int): Array<JobProfile?> {
            return arrayOfNulls(size)
        }
    }
}