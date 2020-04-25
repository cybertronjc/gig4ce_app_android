package com.gigforce.app.modules.profile.models

class AddressModel(
    var city:String="",
    var firstLine:String="",
    var pincode:String="",
    var secondLine:String="",
    var state:String=""
) {
    var currentAddress = "current"
    var permanentAddress:String = "home"
    fun isEmpty():Boolean{
        return city=="" && firstLine=="" && pincode=="" && secondLine=="" && state==""
    }

}