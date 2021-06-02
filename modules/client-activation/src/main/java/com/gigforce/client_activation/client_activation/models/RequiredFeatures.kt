package com.gigforce.client_activation.client_activation.models

data class RequiredFeatures(
    var title: String? = "",
    var type: String? = "",
    var status: String? = "",
    var isDone: Boolean = false,
    var refresh: Boolean = false,

    ) {
}