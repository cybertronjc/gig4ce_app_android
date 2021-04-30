package com.gigforce.app.modules.profile

import android.content.Context
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.google.android.material.chip.Chip

abstract class ProfileBaseFragment: BaseFragment() {

    val profileViewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()
    var validation: ProfileValidation? = null

    init {
        validation = ProfileValidation()
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