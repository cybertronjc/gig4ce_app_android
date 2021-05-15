package com.gigforce.app.modules.profile

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.gigforce.app.R
import com.gigforce.core.viewmodels.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*

abstract class ProfileBaseBottomSheetFragment : BottomSheetDialogFragment() {
    var mView: View? = null
    var validation: ProfileValidation? = null
    val profileViewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()

    init {
        validation = ProfileValidation()
    }

    open fun activate(view: View?) {}

    open fun inflateView(
        resource: Int, inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        mView = inflater.inflate(resource, container, false)
        activate(mView)
        return mView
    }

    fun getFragmentView(): View {
        return mView!!
    }

    open fun addCrossableChip(context: Context, name: String): Chip {
        var chip = addChip(context, name)

        chip.setCloseIconResource(R.drawable.ic_close)
        chip.isCloseIconVisible = true
        return chip
    }

    open fun addChip(context: Context, name: String): Chip {
        var chip = Chip(context)
        chip.text = " #$name "
        chip.isClickable = false
        chip.setTextAppearanceResource(R.style.chipTextDefaultColor)
        chip.setChipStrokeColorResource(R.color.colorPrimary)
        chip.setChipStrokeWidthResource(R.dimen.border_width)
        chip.setChipBackgroundColorResource(R.color.fui_transparent)
        return chip
    }

    fun showError(formError: TextView, vararg views: View?) {
        formError.visibility = View.VISIBLE
        for (view in views) {
            (view as EditText).setHintTextColor(resources.getColor(R.color.colorError))
        }
    }

    fun showErrorText(error: String, formError: TextView, vararg views: View?) {
        formError.text = error
        formError.visibility = View.VISIBLE
        for (view in views) {
            (view as EditText).setHintTextColor(resources.getColor(R.color.colorError))
        }
    }

    fun hideError(formError: TextView, vararg views: View?) {
        formError.visibility = View.GONE
        for (view in views) {
            (view as EditText).setHintTextColor(resources.getColor(R.color.colorHint))
        }
    }

    fun getDeleteConfirmationDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.delete_confirmation_dialog)

        dialog.cancel.setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }

    fun showNumberPicker(context: Context, text: EditText, value: Int? = null) {
        var selectedYear: Int = 0
        val linearLayout = RelativeLayout(context)
        val aNumberPicker = NumberPicker(context)
        aNumberPicker.maxValue = 2050
        aNumberPicker.minValue = 1950

        aNumberPicker.value = value ?: 2012

        val params = RelativeLayout.LayoutParams(50, 50)
        val numPicerParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        numPicerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)

        linearLayout.layoutParams = params
        linearLayout.addView(aNumberPicker, numPicerParams)

        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(getString(R.string.sel_year))
        alertDialogBuilder.setView(linearLayout)
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton(getString(R.string.okay),
                DialogInterface.OnClickListener { dialog, id ->
                    Log.d(
                        "NUMBERPICKER",
                        "New Quantity Value : " + aNumberPicker.value
                    )
                    text.setText(aNumberPicker.value.toString())
                })
            .setNegativeButton(getString(R.string.cancel),
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }
}