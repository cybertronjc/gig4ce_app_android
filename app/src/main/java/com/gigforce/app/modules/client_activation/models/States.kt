package com.gigforce.app.modules.client_activation.models

data class States(var country_code: String = "", var name: String = "", var id: String = "") {

    override fun toString(): String {
        return name
    }
}