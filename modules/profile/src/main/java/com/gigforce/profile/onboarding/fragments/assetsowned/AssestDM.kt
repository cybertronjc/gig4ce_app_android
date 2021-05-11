package com.gigforce.profile.onboarding.fragments.assetsowned

data class AssestDM(
    var id: String = "",
    var icon: String = "",
    var assetsType: String = "",
    var image: Int = -1,
    var index: Int = -1,
    var isActive: Boolean = false,
    var name:String = "",
    var selected: Boolean = false
)
