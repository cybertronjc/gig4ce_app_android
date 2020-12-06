package com.gigforce.app.modules.client_activation.models

data class Cities(var country_code: String = "", var name: String = "", var state_code: String = "") {
    override fun toString(): String {
        return name
    }

}