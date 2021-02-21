package com.gigforce.core.base.utilfeatures

interface UtilAndValidationInterface {
    fun showToast(Message: String)
    fun isNullOrWhiteSpace(str: String): Boolean
    fun showToastLong(Message: String, Duration: Int)
    fun getCurrentVersion(): String
    fun updateResources(language: String)
}