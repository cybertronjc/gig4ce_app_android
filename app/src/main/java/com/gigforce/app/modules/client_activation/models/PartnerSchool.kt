package com.gigforce.app.modules.client_activation.models

data class PartnerSchool(
    var jobProfileId: String = "", var type: String = "", var addressList
    : List<PartnerSchoolDetails> = listOf()
)

