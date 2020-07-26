package com.gigforce.app.core.base.dialog

import android.app.Activity
import android.app.Dialog
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.app.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_select_language.*

open class AppDialogsImp(var activity: Activity) : AppDialogsInterface {

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

    override fun showConfirmationDialogType3(
        title: String,
        subTitle:String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.confirmation_custom_alert_type3)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        var width = displayMetrics.widthPixels
        var parentLayout = customialog?.findViewById<ConstraintLayout>(R.id.parent_cl)
        var lp = parentLayout?.layoutParams
        lp?.width = width-32
        val titleDialog = customialog?.findViewById(R.id.title) as TextView
        titleDialog.text = title
        val subTitleDialog = customialog?.findViewById(R.id.sub_title) as TextView
        subTitleDialog.text = subTitle
        val yesBtn = customialog?.findViewById(R.id.yes) as TextView
        val noBtn = customialog?.findViewById(R.id.cancel) as TextView
        yesBtn.setOnClickListener(View.OnClickListener {
            buttonClickListener.clickedOnYes(customialog)
        })
        noBtn.setOnClickListener(View.OnClickListener { buttonClickListener.clickedOnNo(customialog) })
        customialog?.show()
    }
    override fun showConfirmationDialogType5(
        title: String,
        buttonClickListener: ConfirmationDialogOnClickListener
    ) {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.confirmation_custom_alert_type5)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        var width = displayMetrics.widthPixels
        var parentLayout = customialog?.findViewById<ConstraintLayout>(R.id.parent_cl)
        var lp = parentLayout?.layoutParams
        lp?.width = width-32
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
    override fun showConfirmationDialogType4(
        title: String,
        subTitle:String,
        optionSelected: OptionSelected
    ) {
        var customialog: Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setCancelable(false)
        customialog?.setContentView(R.layout.confirmation_custom_alert_type4)
        val titleDialog = customialog?.findViewById(R.id.title) as TextView
        titleDialog.text = title
        val subTitleDialog = customialog?.findViewById(R.id.sub_title) as TextView
        subTitleDialog.text = subTitle
        val groupRadio = customialog?.findViewById<RadioGroup>(R.id.groupradio)
        val yesBtn = customialog?.findViewById(R.id.yes) as TextView
        yesBtn.setOnClickListener(View.OnClickListener {
            optionSelected.optionSelected(customialog,getOptionSelected(groupRadio))
        })
        customialog?.show()
    }

    fun getOptionSelected(groupradio:RadioGroup):String{
        val selectedId = groupradio.checkedRadioButtonId
        if (selectedId == -1) {
            return ""
        } else {
            val radioButton = groupradio.findViewById<RadioButton>(selectedId)
            val lang = radioButton.hint.toString()
            return lang
        }
    }

}