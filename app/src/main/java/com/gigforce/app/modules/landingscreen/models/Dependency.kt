package com.gigforce.app.modules.landingscreen.models

import android.graphics.drawable.Drawable
import com.google.firebase.firestore.Exclude

data class Dependency(
        var docType: String = "",
        var type: String? = null,
        var title: String? = null,
        var status: String = "",
        var course: String = "",
        var moduleId: String = "",
        var lessonId: String = "",
        @JvmField
        var isSlotBooked: Boolean = false,

        @JvmField
        var isDone: Boolean = false,

        @get:Exclude
        var drawable: Drawable? = null

) {
    override fun equals(other: Any?): Boolean {
        val dependency = other as Dependency
        return dependency.type == type
    }
}