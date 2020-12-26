package com.gigforce.app.utils.lang_models

import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorProfiles
import com.gigforce.app.utils.Lang
import com.gigforce.app.utils.NestedLang
import java.util.*

open class BaseLangModel {


    fun checkForLangTranslation() {
        val langMap = LangMapSingleton.langMap
        if (Locale.getDefault().language != "en") run {
            val declaredFields = AmbassadorProfiles::class.java.declaredFields
            declaredFields.forEach {
                val nestedLang = it.getAnnotation(NestedLang::class.java)
                if (nestedLang != null) {
                    it.isAccessible=true
                    checkForLangTranslation(it.get(this))
                } else {
                    val annotation = it.getAnnotation(Lang::class.java)
                    if (annotation != null) {
                        val item = langMap?.get(annotation.langKey)
                        if (item != null) {
                            it.isAccessible = true
                            it.set(this, item)
                        }
                    }
                }
            }
        }

    }

    private fun <T : Any> checkForLangTranslation(nestedObj: T) {


        val langMap = LangMapSingleton.langMap
        if (Locale.getDefault().language != "en") run {
            val declaredFields = nestedObj!!::class.java.declaredFields
            declaredFields.forEach {
                val nestedLang = it.getAnnotation(NestedLang::class.java)
                if (nestedLang != null) {
                    it.isAccessible=true
                    checkForLangTranslation(it.get(nestedObj))
                } else {
                    val annotation = it.getAnnotation(Lang::class.java)
                    if (annotation != null) {
                        val item = langMap?.get(annotation.langKey)
                        if (item != null) {
                            it.isAccessible = true
                            it.set(nestedObj, item)
                        }
                    }
                }
            }

        }

    }
}