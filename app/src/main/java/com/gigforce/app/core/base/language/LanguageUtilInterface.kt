package com.gigforce.app.core.base.language

import android.app.Dialog
import com.gigforce.common_ui.ConfirmationDialogOnClickListener

interface LanguageUtilInterface {
    fun confirmDialogForDeviceLanguageChanged(currentDeviceLanguageCode: String,buttonClickListener: ConfirmationDialogOnClickListener)
    fun showDialogIfDeviceLanguageChanged()
    fun getDeviceLanguageDialog(): Dialog?
    fun getChangedDeviceLanguageCode(deviceLanguage:String):String
    fun getLanguageCodeToName(currentDeviceLanguageCode: String): String
}