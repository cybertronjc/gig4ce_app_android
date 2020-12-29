package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.google.firebase.firestore.PropertyName

data class PincodeResponse(
    @get:PropertyName("Message")
    @set:PropertyName("Message")
    var message: String = "",
    @get:PropertyName("Status")
    @set:PropertyName("Status")
    var status: String = "",
    @get:PropertyName("PostOffice")
    @set:PropertyName("PostOffice")
    var postOffice: List<PostalOffice>
)

data class PostalOffice(
    @get:PropertyName("Name")
    @set:PropertyName("Name")
    var name: String = "",
    @get:PropertyName("Description")
    @set:PropertyName("Description")
    var description: String = "",
    @get:PropertyName("BranchType")
    @set:PropertyName("BranchType")
    var branchType: String = "",
    @get:PropertyName("DeliveryStatus")
    @set:PropertyName("DeliveryStatus")
    var deliveryStatus: String = "",
    @get:PropertyName("Circle")
    @set:PropertyName("Circle")
    var circle: String = "",
    @get:PropertyName("District")
    @set:PropertyName("District")
    var district: String = "",
    @get:PropertyName("Division")
    @set:PropertyName("Division")
    var division: String = "",
    @get:PropertyName("Region")
    @set:PropertyName("Region")
    var region: String = "",
    @get:PropertyName("Block")
    @set:PropertyName("Block")
    var block: String = "",
    @get:PropertyName("State")
    @set:PropertyName("State")
    var state: String = "",
    @get:PropertyName("Country")
    @set:PropertyName("Country")
    var country: String = "",
    @get:PropertyName("Pincode")
    @set:PropertyName("Pincode")
    var pincode: String = ""
)