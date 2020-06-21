package com.gigforce.app.core.base.dialog

import android.app.Dialog
import com.gigforce.app.core.base.BaseFragment

interface AppDialogsInterface {

    fun confirmDialogForDeviceLanguageChanged(currentDeviceLanguageCode: String,buttonClickListener: ConfirmationDialogOnClickListener)
    fun getDeviceLanguageDialog():Dialog?
    fun showConfirmationDialogType1(title: String, buttonClickListener: ConfirmationDialogOnClickListener)
    fun showConfirmationDialogType2(title: String, buttonClickListener: ConfirmationDialogOnClickListener)
    fun getDeviceLanguageChanged(deviceLanguage:String):String

    fun getLanguageCodeToName(currentDeviceLanguageCode: String): String
}
interface ConfirmationDialogOnClickListener {
    fun clickedOnYes(dialog: Dialog?)
    fun clickedOnNo(dialog: Dialog?)
}