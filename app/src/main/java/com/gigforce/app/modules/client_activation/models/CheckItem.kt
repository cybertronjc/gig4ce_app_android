package com.gigforce.app.modules.client_activation.models

data class CheckItem(var content: String = "", @JvmField var isForKitCollection: Boolean = false) {

    override fun equals(other: Any?): Boolean {
        val o = other as CheckItem
        return o.content == content
    }
}