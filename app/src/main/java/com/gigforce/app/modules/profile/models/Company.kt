package com.gigforce.app.modules.profile.models

import com.google.firebase.firestore.PropertyName

data class Company(

    @get:PropertyName("companyId")
    @set:PropertyName("companyId")
    var companyId: String = "",

    @get:PropertyName("companyName")
    @set:PropertyName("companyName")
    var companyName: String? = ""
)