package com.gigforce.app.core.base.dialog

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.view.View
import android.view.Window
import android.widget.TextView
import com.gigforce.app.R

open class AppDialogsImp(var activity: Activity) : AppDialogsInterface {

    var languageSelectionDialog: Dialog? = null
    companion object {
        var englishCode = "en"
        var hindiCode = "hi"
        var telguCode = "te"
        var gujratiCode = "gu"
        var punjabiCode = "pa"
        var françaisCode = "fr"
        var marathiCode = "mr"
    }
    override fun getLanguageCodeToName(currentDeviceLanguageCode: String): String {
        when (currentDeviceLanguageCode) {
            englishCode -> return activity.resources.getString(R.string.english)
            hindiCode -> return activity.resources.getString(R.string.hindi)
            telguCode -> return activity.resources.getString(R.string.telgu)
            gujratiCode -> return activity.resources.getString(R.string.gujrati)
            punjabiCode -> return activity.resources.getString(R.string.punjabi)
            françaisCode -> return activity.resources.getString(R.string.francais)
            marathiCode -> return activity.resources.getString(R.string.marathi)
            else -> return ""
        }
    }

    override fun confirmDialogForDeviceLanguageChanged(
        currentDeviceLanguageCode: String,buttonClickListener: ConfirmationDialogOnClickListener
    ) {

        languageSelectionDialog = activity?.let { Dialog(it) }
        languageSelectionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        languageSelectionDialog?.setCancelable(false)
        languageSelectionDialog?.setContentView(R.layout.confirmation_custom_alert_type1)
        val titleDialog = languageSelectionDialog?.findViewById(R.id.title) as TextView
        titleDialog.text =
            "Your device language changed to " + getLanguageCodeToName(currentDeviceLanguageCode) + ". Do you want to continue with this language?"
        val yesBtn = languageSelectionDialog?.findViewById(R.id.yes) as TextView
        val noBtn = languageSelectionDialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener {
            buttonClickListener.clickedOnYes(languageSelectionDialog)

        }
        noBtn.setOnClickListener {
            buttonClickListener.clickedOnNo(languageSelectionDialog)

        }
        languageSelectionDialog?.show()
    }

    override fun getDeviceLanguageDialog():Dialog? {
        return languageSelectionDialog
    }

    //Confirmation dialog start
    // this dialog having right side yes button with gradient. Need to create one having swipable functionality
    override fun showConfirmationDialogType1(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.confirmation_custom_alert_type1)
        val titleDialog = customialog?.findViewById(R.id.title) as TextView
        titleDialog.text = title
        val yesBtn = customialog?.findViewById(R.id.yes) as TextView
        val noBtn = customialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener(View.OnClickListener {
            buttonClickListener.clickedOnYes(customialog)
        })
        noBtn.setOnClickListener(View.OnClickListener { buttonClickListener.clickedOnNo(customialog) })
        customialog?.show()
    }

    override fun showConfirmationDialogType2(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.confirmation_custom_alert_type2)
        val titleDialog = customialog?.findViewById(R.id.title) as TextView
        titleDialog.text = title
        val yesBtn = customialog?.findViewById(R.id.yes) as TextView
        val noBtn = customialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener(View.OnClickListener {
            buttonClickListener.clickedOnYes(customialog)
        })
        noBtn.setOnClickListener(View.OnClickListener { buttonClickListener.clickedOnNo(customialog) })
        customialog?.show()
    }

    override fun getDeviceLanguageChanged(deviceLanguage:String): String {
        var currentDeviceLanguageCode =
            Resources.getSystem().getConfiguration().locale.getLanguage()
        var currentDeviceLanguageName = getLanguageCodeToName(currentDeviceLanguageCode)
        if (!currentDeviceLanguageName.equals("") && !currentDeviceLanguageCode.equals(
                deviceLanguage
            )
        ) {
            return currentDeviceLanguageCode
        }
        return ""
    }
}