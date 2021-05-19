package com.gigforce.app.core.base.language

import android.app.Activity
import android.app.Dialog
import android.content.res.Resources
import android.view.Window
import android.widget.TextView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.common_ui.ConfirmationDialogOnClickListener
import com.gigforce.user_preferences.PreferencesRepository

class LanguageUtilImp : LanguageUtilInterface {
    var baseFragment: BaseFragment
    var activity: Activity
    lateinit var preferencesRepositoryForBaseFragment: PreferencesRepository

    constructor(baseFragment: BaseFragment) {
        this.baseFragment = baseFragment
        this.activity = baseFragment.requireActivity()
    }

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
        currentDeviceLanguageCode: String, buttonClickListener: ConfirmationDialogOnClickListener
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

    override fun getDeviceLanguageDialog(): Dialog? {
        return languageSelectionDialog
    }

    override fun getChangedDeviceLanguageCode(deviceLanguage: String): String {
        var currentDeviceLanguageCode =
            Resources.getSystem().getConfiguration().locale.getLanguage()
//        var currentDeviceLanguageName = getLanguageCodeToName(currentDeviceLanguageCode)
        if (!currentDeviceLanguageCode.equals(
                deviceLanguage
            )
        ) {
            return currentDeviceLanguageCode
        }
        return ""
    }

    //Dialogs
    override fun showDialogIfDeviceLanguageChanged() {
        var currentDeviceLanguageCode =
            getChangedDeviceLanguageCode(baseFragment.getLastStoredDeviceLanguage()!!)
        if (!(currentDeviceLanguageCode.equals(""))) {
            preferencesRepositoryForBaseFragment =
                PreferencesRepository()
            confirmDialogForDeviceLanguageChanged(currentDeviceLanguageCode,
                object :
                    ConfirmationDialogOnClickListener {
                    override fun clickedOnYes(dialog: Dialog?) {
                        baseFragment.saveDeviceLanguage(currentDeviceLanguageCode)
                        baseFragment.saveAppLanuageCode(currentDeviceLanguageCode)
                        baseFragment.saveAppLanguageName(
                            getLanguageCodeToName(
                                currentDeviceLanguageCode
                            )
                        )
                        baseFragment.updateResources(currentDeviceLanguageCode)
                        preferencesRepositoryForBaseFragment.setDataAsKeyValue(
                            "languageName",
                            getLanguageCodeToName(currentDeviceLanguageCode)
                        )
                        preferencesRepositoryForBaseFragment.setDataAsKeyValue(
                            "languageCode",
                            currentDeviceLanguageCode
                        )
                        dialog?.dismiss()
                    }

                    override fun clickedOnNo(dialog: Dialog?) {
                        baseFragment.saveDeviceLanguage(currentDeviceLanguageCode)
                        dialog!!.dismiss()
                    }
                })
        }
    }

}