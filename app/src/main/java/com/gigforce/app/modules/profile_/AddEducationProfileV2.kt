package com.gigforce.app.modules.profile_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.fragment_add_education.*

class AddEducationProfileV2 : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_add_education, inflater, container)

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
        iv_back_application_add_education_v2.setOnClickListener { activity?.onBackPressed() }
    }

    private fun initSearchAutoComplete() {
        act_langs_add_education_profile_v2.setAdapter(
            resources.getStringArray(R.array.lang_array).toList()
        )
        act_langs_add_education_profile_v2.setHint("Search Language")
        act_langs_add_education_profile_v2.setContentPadding(
            paddingTop = resources.getDimensionPixelSize(
                R.dimen.size_15
            ), paddingBottom = resources.getDimensionPixelSize(R.dimen.size_15),
            paddingLeft = resources.getDimensionPixelSize(R.dimen.size_5)
        )
    }

    private fun intLangChipGroup() {
        val listOf = listOf(
            "10th",
            "12th",
            "Diploma",
            "Graduation",
            "Post Graduation",
            "<10th"
        )
        listOf.forEach { element ->
            val chip = Chip(requireContext())
            val drawable = ChipDrawable.createFromAttributes(
                requireContext(),
                null,
                0,
                R.style.AppSingleChoiceChip2
            )
            chip.setChipDrawable(drawable)
            chip.text = element
            chip.tag = element

            chip.chipStrokeWidth = 1f
            chip_group_add_education.addView(chip, 0)
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    listOf.forEach {
                        val nesChip = chip_group_add_education.findViewWithTag<Chip>(it)
                        if (nesChip.tag != buttonView.tag) {
                            chip_group_add_education.removeView(nesChip)
                        }
                    }


                } else {
                    chip_group_add_education.removeAllViews()
                    intLangChipGroup()
                }

            }
        }
    }


}