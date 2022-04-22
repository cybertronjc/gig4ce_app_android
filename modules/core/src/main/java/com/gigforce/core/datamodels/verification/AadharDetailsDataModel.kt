package com.gigforce.core.datamodels.verification

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AadhaarDetailsDataModel(

        @SerializedName("frontImagePath")
        var frontImagePath: String? = "",

        @SerializedName("backImagePath")
        var backImagePath: String? = "",

        @SerializedName("aadhaarCardNo")
        var aadhaarCardNo: String? = "",

        @SerializedName("dateOfBirth")
        var dateOfBirth: String = "",

        @SerializedName("fName")
        var fName: String = "",

        @SerializedName("addLine1")
        var addLine1: String = "",

        @SerializedName("addLine2")
        var addLine2: String = "",

        @SerializedName("state")
        var state: String = "",

        @SerializedName("city")
        var city: String = "",

        @SerializedName("pincode")
        var pincode: String? = "",

        @SerializedName("landmark")
        var landmark: String? = "",

        @SerializedName("currentAddSameAsParmanent")
        var currentAddSameAsParmanent: Boolean = true,

        @SerializedName("currentAddress")
        var currentAddress: CurrentAddressDetailDataModel? = null,

        @SerializedName("name")
        var name: String? = "",

        @SerializedName("verified")
        var verified : Boolean?=false
) : VerificationUserSubmittedData, Parcelable{

        fun getDOBDate():Int?{
                if(dateOfBirth.isNotBlank()){
                        try {
                                val arr = dateOfBirth.split("-")
                                val date = arr[0].toInt()
                                return date
                        }catch (e:Exception){
                                return null
                        }

                }else return null
        }

        fun getDOBMonth():Int?{
                if(dateOfBirth.isNotBlank()){
                        try {
                                val arr = dateOfBirth.split("-")
                                val month = arr[1].toInt()
                                return month
                        }catch (e:Exception){
                                return null
                        }

                }else return null
        }


        fun getDOBYear():Int?{
                if(dateOfBirth.isNotBlank()){
                        try {
                                val arr = dateOfBirth.split("-")
                                val year = arr[2].toInt()
                                return year
                        }catch (e:Exception){
                                return null
                        }

                }else return null
        }
}

@Parcelize
data class CurrentAddressDetailDataModel(

        @SerializedName("addLine1")
        var addLine1: String? = "",

        @SerializedName("addLine2")
        var addLine2: String? = "",

        @SerializedName("state")
        var state: String? = "",

        @SerializedName("city")
        var city: String? = "",

        @SerializedName("pincode")
        var pincode: String? = "",

        @SerializedName("landmark")
        var landmark: String? = ""
) : Parcelable