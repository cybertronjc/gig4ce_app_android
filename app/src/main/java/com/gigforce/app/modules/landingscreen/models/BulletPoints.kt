package com.gigforce.app.modules.landingscreen.models

data class BulletPoints(
    var showPoints: Int = 0, @JvmField var requiredShowPoints: Boolean=false,
    var pointsData: ArrayList<String>? = null,
    var title: String? = null,
    var url: String? = null

)