package com.gigforce.core.datamodels.client_activation

data class Cities(var country_code: String = "", var name: String = "", var state_code: String = "") {
    override fun toString(): String {
        return name
    }
    override fun equals(other: Any?): Boolean {
        val state = other as Cities
        return name == state.name
    }

}