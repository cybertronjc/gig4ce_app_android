package com.gigforce.profile.onboarding.fragments.interest
import com.gigforce.profile.models.SkillsDetails
import com.google.gson.annotations.SerializedName

data class InterestDM(
    var id: String = "",
    var icon: String = "",
    var image: Int = -1,
    var index: Int = -1,
    var isActive: Boolean = false,
    var skill:String = "",
    @SerializedName("skillDetails")var skillDetails: List<SkillsDetails>? = null,
    var selected: Boolean = false){

}