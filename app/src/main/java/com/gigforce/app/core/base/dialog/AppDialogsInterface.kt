package com.gigforce.app.core.base.dialog

import android.app.Dialog
import com.gigforce.app.core.base.BaseFragment

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
        buttonClickListener: ConfirmationDialogOnClickListener
    )

    fun showConfirmationDialogType4(
        title: String,
        subTitle: String,
        buttonClickListener: OptionSelected
    )
}

interface ConfirmationDialogOnClickListener {
    fun clickedOnYes(dialog: Dialog?)
    fun clickedOnNo(dialog: Dialog?)
}

interface OptionSelected {
    fun optionSelected(dialog: Dialog?, option:String)
}