package com.gigforce.app.modules.profile_

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.layout_add_lang_profile_v2.*


class AddLanguageProfileV2 : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_add_lang_profile_v2, inflater, container)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        intLangChipGroup()
    }

    private fun intLangChipGroup() {

        listOf("English", "Hindi", "Marathi", "Punjabi").forEach { element ->
            val chip = Chip(requireContext())
            val drawable = ChipDrawable.createFromAttributes(
                requireContext(),
                null,
                0,
                R.style.AppSingleChoiceChip2
            )
            chip.setChipDrawable(drawable)
            chip.text = element
            chip.chipStrokeWidth = 1f
            language_chip_group_add_language.addView(chip, 0)
        }

    }
}