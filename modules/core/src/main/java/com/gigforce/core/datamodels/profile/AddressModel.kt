package com.gigforce.core.datamodels.profile

class AddressModel(
    var firstLine: String = "",
    var secondLine: String = "",
    var area: String = "",
    var city: String = "",
    var state: String = "",
    var pincode: String = "",
    var preferred_distance :Int = 0,
    var preferredDistanceActive:Boolean = false
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