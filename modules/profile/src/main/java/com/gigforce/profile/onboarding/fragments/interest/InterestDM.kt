package com.gigforce.profile.onboarding.fragments.interest
import com.gigforce.profile.models.SkillsDetails

data class InterestDM(
    var id: String = "",
    var icon: String = "",
    var image: Int = -1,
    var index: Int = -1,
    var isActive: Boolean = false,
    var skill:String = "",
    var skillDetails: List<SkillsDetails> = emptyList(),
    var selected: Boolean = false){

}