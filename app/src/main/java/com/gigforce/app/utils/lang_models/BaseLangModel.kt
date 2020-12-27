package com.gigforce.app.utils.lang_models

import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorProfiles
import com.gigforce.app.utils.CheckCollectionTranslation
import com.gigforce.app.utils.TranslationNeeded
import com.gigforce.app.utils.CheckNestedTranslation
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

open class BaseLangModel {


    fun checkForLangTranslation() {
        try {
            val langMap = LangMapSingleton.langMap
            if (Locale.getDefault().language != "en") run {
                val declaredFields = this@BaseLangModel::class.java.declaredFields
                declaredFields.forEach {
                    it.isAccessible = true

                    if (it.getAnnotation(CheckNestedTranslation::class.java) != null) {
                        checkForNestedTranslation(it.get(this), langMap)
                    } else if (it.getAnnotation(CheckCollectionTranslation::class.java) != null) {
                        val annotation = it.getAnnotation(CheckCollectionTranslation::class.java)
                        val list: List<out BaseLangModel> = it.get(this) as List<out BaseLangModel>
                        list.forEachIndexed { index, listObj ->
                            checkForNestedTranslation(
                                listObj,
                                langMap,
                                checkCollection = true,
                                arrKey = annotation.langKey,
                                index = index
                            )
                        }

                    } else {

                        val annotation = it.getAnnotation(TranslationNeeded::class.java)
                        val obj = it.get(this)
                        if (obj is List<*> && !obj.isNullOrEmpty() && obj[0] is String) {
                            val item = langMap?.get(annotation.langKey)
                            if (item != null) {
                                it.set(this, item)
                            }
                        } else {
                            if (annotation != null) {
                                val item = langMap?.get(annotation.langKey)
                                if (item != null) {
                                    it.set(this, item)
                                }
                            }
                        }

                    }
                }
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .log("Language Translation" + e.message ?: "")

        }

    }


    private fun <T : Any> checkForNestedTranslation(
        nestedObj: T,
        langMap: MutableMap<String, Any>?,
        checkCollection: Boolean = false,
        arrKey: String = "",
        index: Int = -1
    ) {
        try {


            if (Locale.getDefault().language != "en") run {
                val declaredFields = nestedObj!!::class.java.declaredFields
                declaredFields.forEach {
                    it.isAccessible = true
                    val nestedLang = it.getAnnotation(CheckNestedTranslation::class.java)
                    if (nestedLang != null) {
                        if (checkCollection) {
                            val nestedList = langMap?.get(arrKey) as List<out BaseLangModel>
                            val map = nestedList[index] as? HashMap<String, HashMap<String, Any>>

                            checkForNestedTranslation(
                                it.get(nestedObj)
                                , map?.get(nestedLang.langKey)
                            )
                        } else {
                            checkForNestedTranslation(it.get(nestedObj), langMap)
                        }

                    } else if (it.getAnnotation(CheckCollectionTranslation::class.java) != null) {
                        val annotation = it.getAnnotation(CheckCollectionTranslation::class.java)
                        val list: List<out BaseLangModel> =
                            it.get(nestedObj) as List<out BaseLangModel>
                        list.forEachIndexed { index, listObj ->
                            checkForNestedTranslation(
                                listObj,
                                langMap,
                                checkCollection = true,
                                arrKey = annotation.langKey,
                                index = index
                            )
                        }

                    } else {
                        val annotation = it.getAnnotation(TranslationNeeded::class.java)
                        if (annotation != null) {
                            val obj = it.get(this)
                            if (obj is List<*> && !obj.isNullOrEmpty() && obj[0] is String) {
                                val item = langMap?.get(annotation.langKey)
                                if (item != null) {
                                    it.set(nestedObj, item)
                                }
                            } else if (checkCollection) {
                                val nestedList = langMap?.get(arrKey) as List<out BaseLangModel>
                                val map = nestedList[index] as? HashMap<String, Any>
                                it.set(nestedObj, map?.get(annotation.langKey))
                            } else {
                                val item = langMap?.get(annotation.langKey) ?: return@run
                                it.set(nestedObj, item)
                            }

                        }
                    }
                }

            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance()
                .log("Language Translation" + e.message ?: "")

        }

    }
}