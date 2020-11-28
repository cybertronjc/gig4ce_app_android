package com.gigforce.app.modules.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class GigActivation(var icon: String = "", var status: String = "", var subTitle: String = "", var title: String = "",
                         var trainingTitle: String = "", var type: String = "", var workOrderId: String = "",
                         var requirements: RequirmentsGigActivation? = null, var dependency: List<DependencyGigActivation> = listOf(),
                         var instruction: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readParcelable(RequirmentsGigActivation::class.java.classLoader),
            parcel.createTypedArrayList(DependencyGigActivation) ?: listOf(),
            parcel.readString() ?: ""

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(icon)
        parcel.writeString(status)
        parcel.writeString(subTitle)
        parcel.writeString(title)
        parcel.writeString(trainingTitle)
        parcel.writeString(type)
        parcel.writeString(workOrderId)
        parcel.writeParcelable(requirements, flags)
        parcel.writeTypedList(dependency)
        parcel.writeString(instruction)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GigActivation> {
        override fun createFromParcel(parcel: Parcel): GigActivation {
            return GigActivation(parcel)
        }

        override fun newArray(size: Int): Array<GigActivation?> {
            return arrayOfNulls(size)
        }
    }
}