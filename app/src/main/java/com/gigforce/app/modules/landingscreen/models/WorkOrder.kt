package com.gigforce.app.modules.landingscreen.models

import android.os.Parcel
import android.os.Parcelable

data class WorkOrder(
        var business_id: String? = null,
        var business_name: String? = null,
        var requiredLessons: RequiredLessons? = null,
        var work_order_icon: String? = null,
        var work_order_title: String? = null,
        var profile_name: String? = null,
        var profile_id: String? = null,
        var locations: List<Locations>? = null,
        var payoutNote: String? = null,
        var queries: List<Queries>? = null,
        var id: String? = null,
        var info: List<BulletPoints>? = null,
        var subTitle: String = ""
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
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(business_id)
        parcel.writeString(business_name)
        parcel.writeParcelable(requiredLessons, flags)
        parcel.writeString(work_order_icon)
        parcel.writeString(work_order_title)
        parcel.writeString(profile_name)
        parcel.writeString(profile_id)
        parcel.writeTypedList(locations)
        parcel.writeString(payoutNote)
        parcel.writeTypedList(queries)
        parcel.writeString(id)
        parcel.writeTypedList(info)
        parcel.writeString(subTitle)
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