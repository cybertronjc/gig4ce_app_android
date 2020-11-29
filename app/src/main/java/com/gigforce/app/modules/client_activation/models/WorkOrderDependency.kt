package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable
import com.gigforce.app.modules.landingscreen.models.Dependency

data class WorkOrderDependency(
        var dependency: List<Dependency>? = null,
        var icon: String? = null,
        var subTitle: String? = null,
        var title: String? = null,
        var type: String? = null,
        var workOrderId: String? = null,
        var nextDependency: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.createTypedArrayList(Dependency),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString() ?: "") {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(dependency)
        parcel.writeString(icon)
        parcel.writeString(subTitle)
        parcel.writeString(title)
        parcel.writeString(type)
        parcel.writeString(workOrderId)
        parcel.writeString(nextDependency)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WorkOrderDependency> {
        override fun createFromParcel(parcel: Parcel): WorkOrderDependency {
            return WorkOrderDependency(parcel)
        }

        override fun newArray(size: Int): Array<WorkOrderDependency?> {
            return arrayOfNulls(size)
        }
    }
}