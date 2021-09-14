package com.gigforce.core.datamodels.profile

data class PFESICDataModel(
    var esicNumber: String? = "",
    var uanNumber: String? = "",
    var pfNumber: String? = "",
    var nomineeName: String? = "",
    var rNomineeName: String? = "",
    var dobNominee: String? = "",
    var signature: String? = "",
    var isAlreadyExists: Boolean = false
)
