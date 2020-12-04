package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class JobProfile(
    var businessId: String? = null,
    var businessName: String? = null,
    var requiredLessons: RequiredLessons? = null,
    var locationList: List<Locations>? = null,
    var payoutNote: String? = null,
    var queries: List<Queries>? = null,
    var id: String? = null,
    var info: List<BulletPoints>? = null,
    var title: String = "",
    var nextDependency: String = "",
    var coverImg: String = "",
    var defaultPayoutRequired: Boolean = false,
    var profileId: String = "",
    var profileName: String = "",
    var totalSteps: Int = 0
//        var tags: List<String> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(RequiredLessons::class.java.classLoader),
        parcel.createTypedArrayList(Locations),
        parcel.readString(),
        parcel.createTypedArrayList(Queries),
        parcel.readString(),
        parcel.createTypedArrayList(BulletPoints),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readByte() != 0.toByte(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(businessId)
        parcel.writeString(businessName)
        parcel.writeParcelable(requiredLessons, flags)
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