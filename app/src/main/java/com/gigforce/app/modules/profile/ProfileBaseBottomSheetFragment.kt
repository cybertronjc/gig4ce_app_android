package com.gigforce.app.modules.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

abstract class ProfileBaseBottomSheetFragment: BottomSheetDialogFragment() {
    var mView: View? = null
    var validation: ProfileValidation? = null
    val profileViewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()

    init {
        validation = ProfileValidation()
    }

    open fun activate(view:View?){}

    open fun inflateView(
        resource: Int, inflater: LayoutInflater,
        container: ViewGroup?
    ): View? {
        mView = inflater.inflate(resource, container, false)
        activate(mView)
        return mView
    }

    fun getFragmentView():View{
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

    fun hideError(formError: TextView, vararg views: View?) {
        formError.visibility = View.GONE
        for (view in views) {
            (view as EditText).setHintTextColor(resources.getColor(R.color.colorHint))
        }
    }
}