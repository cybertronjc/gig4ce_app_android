package com.gigforce.client_activation.client_activation.models

import android.os.Parcel
import android.os.Parcelable

data class PartnerSchool(
    var jobProfileId: String = "", var type: String = "",var alertMessage:String = "", var addressList
    : List<PartnerSchoolDetails> = listOf(), var headerTitle: String = "",var documentTitle : String = "",var documentSubTitle : String = "",var documentInfo : List<String> = listOf(),
    var checkoutConfig: DocReceiving? = null, var timeSlots: List<String> = listOf(),
    var addressHeader: AddressHeader? = null

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString()?:"",
        parcel.createTypedArrayList(PartnerSchoolDetails) ?: listOf(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf(),
        parcel.readParcelable(DocReceiving::class.java.classLoader),
        parcel.createStringArrayList() ?: listOf(),
        parcel.readParcelable(AddressHeader::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobProfileId)
        parcel.writeString(type)
        parcel.writeTypedList(addressList)
        parcel.writeString(headerTitle)
        parcel.writeStringList(documentInfo)
        parcel.writeParcelable(checkoutConfig, flags)
        parcel.writeStringList(timeSlots)
        parcel.writeParcelable(addressHeader, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PartnerSchool> {
        override fun createFromParcel(parcel: Parcel): PartnerSchool {
            return PartnerSchool(parcel)
        }

        override fun newArray(size: Int): Array<PartnerSchool?> {
            return arrayOfNulls(size)
        }
    }
}