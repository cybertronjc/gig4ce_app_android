package com.gigforce.app.modules.profile.models

class AddressModel(
    var city: String = "",
    var firstLine: String = "",
    var pincode: String = "",
    var secondLine: String = "",
    var state: String = "",
    var area: String = ""
) {

    fun isEmpty(): Boolean {
        return city == "" && firstLine == "" && pincode == "" && secondLine == "" && state == "" && area == ""
    }

    fun isSame(address1: AddressModel): Boolean {
        return !address1.isEmpty() &&
        !this.isEmpty() &&
        address1 == this
    }

}