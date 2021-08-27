package com.gigforce.core.datamodels.verification

data class AadhaarDetailsDataModel (
    var frontImagePath: String? = "",
    var backImagePath: String? = "",
    var aadhaarCardNo: String? = "",
    var dateOfBirth: String = "",
    var fName: String = "",
    var addLine1: String = "",
    var addLine2: String = "",
    var state: String = "",
    var city: String = "",
    var pincode: String? = "",
    var landmark :String? = "",
    var currentAddSameAsParmanent : Boolean = true,
    var currentAddress : CurrentAddressDetailDataModel? = null
) {
}

data class CurrentAddressDetailDataModel(
    var addLine1: String? = "",
    var addLine2: String? = "",
    var state: String? = "",
    var city: String? = "",
    var pincode: String? = "",
    var landmark :String? = ""
)