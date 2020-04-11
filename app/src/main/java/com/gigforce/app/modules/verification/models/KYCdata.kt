package com.gigforce.app.modules.verification.models

import java.util.*

data class Address(
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var pincode: String = ""
){
}

data class DL(
    var id_number:String = "",
    var name_on_card:String = "",
    var fathers_name:String = "",
    var date_of_birth:String = "",
    var date_of_validity:String = "",
    var address:String = "",
    var district:String = "",
    var pincode:String = "",
    var state:String = "",
    var street_address:String = ""
){
}

data class VoterID(
    var id_number:String = "",
    var name_on_card:String = "",
    var fathers_name:String = "",
    var date_of_birth:String = "",
    var address:String = "",
    var state:String = "",
    var district:String = "",
    var street_address:String = "",
    var house_number:String = "",
    var pincode:String = "",
    var gender:String = "",
    var age:String = "",
    var year_of_birth:String = ""
){
}

data class Passport(
    var id_number:String = "",
    var first_name:String = "",
    var last_name:String = "",
    var name_on_card:String = "",
    var fathers_name:String = "",
    var mothers_name:String = "",
    var nationality:String = "",
    var date_of_birth:String = "",
    var place_of_birth:String = "",
    var date_of_issue:String = "",
    var date_of_expiry:String = "",
    var place_of_issue:String = "",
    var address:String = "",
    var gender:String = "",
    var name_of_spouse:String = ""
){
}

data class Bank(
    var bankAcName: String = "",
    var bankAcNo: String = "",
    var bankIfsc: String = "",
    var bankName: String = ""
){
}