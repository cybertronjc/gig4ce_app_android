package com.gigforce.app.modules.landingscreen.models

import android.graphics.drawable.Drawable

data class Dependency(
        var feature: String? = null,
        var priority: Int = 0,
        var title: String? = null,
        var drawable: Drawable? = null,
        var isDone: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        val dependency = other as Dependency
        return dependency.feature == feature

    }
}