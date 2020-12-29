package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

data class PincodeResponse(
    @SerializedName("Message")
    var message: String = "",
    @SerializedName("Status")
    var status: String = "",
    @SerializedName("PostOffice")
    var postOffice: ArrayList<PostalOffice>
)

data class PostalOffice(
    @SerializedName("Name")
    var name: String = "",
    @SerializedName("Description")
    var description: String = "",
    @SerializedName("BranchType")
    var branchType: String = "",
    @SerializedName("DeliveryStatus")
    var deliveryStatus: String = "",
    @SerializedName("Circle")
    var circle: String = "",
    @SerializedName("District")
    var district: String = "",
    @SerializedName("Division")
    var division: String = "",
    @SerializedName("Region")
    var region: String = "",
    @SerializedName("Block")
    var block: String = "",
    @SerializedName("State")
    var state: String = "",
    @SerializedName("Country")
    var country: String = "",
    @SerializedName("Pincode")
    var pincode: String = ""
)