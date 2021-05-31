package com.gigforce.common_ui

import android.app.Dialog

interface AppDialogsInterface {

    fun showConfirmationDialogType1(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    )

    fun showConfirmationDialogType2(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    )

    fun showConfirmationDialogType3(
        title: String,
        subTitle: String,
        yesButtonText:String = "Yes",
        noButtonText:String = "No",
        buttonClickListener: ConfirmationDialogOnClickListener
    )
    fun showConfirmationDialogType5(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    )
    fun showConfirmationDialogType4(
        title: String,
        subTitle: String,
        buttonClickListener: OptionSelected
    )
    fun showConfirmationDialogType7(
            title: String,
            buttonClickListener: ConfirmationDialogOnClickListener
    )
}

interface ConfirmationDialogOnClickListener {
    fun clickedOnYes(dialog: Dialog?)
    fun clickedOnNo(dialog: Dialog?)
}

interface OptionSelected {
    fun optionSelected(dialog: Dialog?, option:String)
}