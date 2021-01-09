package com.gigforce.app.modules.questionnaire.models

import com.gigforce.app.modules.client_activation.models.Cities
import com.google.firebase.firestore.Exclude

data class Options(
        @field:JvmField var isAnswer: Boolean = false,
        var answer: String = "",
        var type: String = "",
        var options: MutableList<String> = mutableListOf(),
        var selectedItemPosition: Int = -1,
        var dropDownHint: String = "",
      var cities: MutableList<Cities>? = null


)