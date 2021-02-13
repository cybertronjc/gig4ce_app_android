package com.gigforce.client_activation.client_activation.models

import com.google.firebase.firestore.PropertyName

data class Company(

    @get:PropertyName("companyId")
    @set:PropertyName("companyId")
    var companyId: String = "",

    @get:PropertyName("companyName")
    @set:PropertyName("companyName")
    var companyName: String? = ""
)