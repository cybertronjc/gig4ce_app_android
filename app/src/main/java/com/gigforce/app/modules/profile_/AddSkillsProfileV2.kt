package com.gigforce.app.modules.profile_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.layout_add_skills_profile_v2.*

class AddSkillsProfileV2 : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.layout_add_skills_profile_v2, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        intLangChipGroup()
        initSearchAutoComplete()
        initClicks()
    }

    private fun initClicks() {
        iv_back_application_add_skills.setOnClickListener { activity?.onBackPressed() }
    }

    private fun initSearchAutoComplete() {
//        act_add_skills_profile_v2.setAdapter(
//                resources.getStringArray(R.array.lang_array).toList()
//        )
        act_add_skills_profile_v2.setHint(getString(R.string.search_lang))
        act_add_skills_profile_v2.setContentPadding(
                paddingTop = resources.getDimensionPixelSize(
                        R.dimen.size_15
                ), paddingBottom = resources.getDimensionPixelSize(R.dimen.size_15),
                paddingLeft = resources.getDimensionPixelSize(R.dimen.size_5)
        )
    }

    private fun intLangChipGroup() {
        chip_search_add_skills.setOnCheckedChangeListener { buttonView, isChecked ->
            act_add_skills_profile_v2.isVisible = isChecked
            tv_save_add_skills.isVisible = isChecked
        }
        listOf(
                "Singing",
                "Coding",
                "Story Telling",
                "Driving"
        ).forEach { element ->
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
            chip_group_add_skills.addView(chip, 0)
        }

    }

}