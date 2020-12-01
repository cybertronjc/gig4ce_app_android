package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class WorkOrder(
        var businessId: String? = null,
        var businessName: String? = null,
        var requiredLessons: RequiredLessons? = null,
        var work_order_icon: String? = null,
        var work_order_title: String? = null,
        var profile_name: String? = null,
        var profile_id: String? = null,
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
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.createTypedArrayList(Locations),
            parcel.readString(),
            parcel.createTypedArrayList(Queries),
            parcel.readString(),
            parcel.createTypedArrayList(BulletPoints),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt() == 1,
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt()

            ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(businessId)
        parcel.writeString(businessName)
        parcel.writeParcelable(requiredLessons, flags)
        parcel.writeString(work_order_icon)
        parcel.writeString(work_order_title)
        parcel.writeString(profile_name)
        parcel.writeString(profile_id)
        parcel.writeTypedList(locationList)
        parcel.writeString(payoutNote)
        parcel.writeTypedList(queries)
        parcel.writeString(id)
        parcel.writeTypedList(info)
        parcel.writeString(title)
        parcel.writeString(nextDependency)
        parcel.writeString(coverImg)
        parcel.writeInt((if (defaultPayoutRequired) 1 else 0))
        parcel.writeString(profileId)
        parcel.writeString(profileName)
        parcel.writeInt(totalSteps)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WorkOrder> {
        override fun createFromParcel(parcel: Parcel): WorkOrder {
            return WorkOrder(parcel)
        }

        override fun newArray(size: Int): Array<WorkOrder?> {
            return arrayOfNulls(size)
        }
    }
}