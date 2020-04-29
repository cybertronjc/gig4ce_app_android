package com.gigforce.app.modules.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.google.android.material.chip.Chip

abstract class ProfileBaseFragment: BaseFragment() {

    var profileViewModel: ProfileViewModel? = null
    var validation: ProfileValidation? = null

    init {
        validation = ProfileValidation()
    }

    fun setViewModel(viewModel: ProfileViewModel) {
        profileViewModel = viewModel
    }

    fun getViewModel(): ViewModel? {
        return profileViewModel
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
}