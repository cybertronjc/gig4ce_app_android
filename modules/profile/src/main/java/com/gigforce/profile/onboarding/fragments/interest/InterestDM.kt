package com.gigforce.profile.onboarding.fragments.interest

data class InterestDM(
    var id: String,
    var icon: String,
    var image: Int,
    var index: Int,
    var skill:String,
    var selected: Boolean = false){

}