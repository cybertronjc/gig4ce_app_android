package com.gigforce.app.modules.profile.models

import com.google.firebase.firestore.PropertyName

data class Skill2(

        @get:PropertyName("id")
        @set:PropertyName("id")
        var id: String = "",

        @get:PropertyName("skill")
        @set:PropertyName("skill")
        var skill: String = "",

        @get:PropertyName("roles")
        @set:PropertyName("roles")
        var roles: List<String> = emptyList()
)