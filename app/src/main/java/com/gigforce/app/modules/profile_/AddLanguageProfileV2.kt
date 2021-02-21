package com.gigforce.app.modules.profile_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.children
import androidx.core.view.isVisible
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
        initSearchAutoComplete()
        initClicks()
    }

    private fun initClicks() {
        iv_back_application_add_language_v2.setOnClickListener { activity?.onBackPressed() }
    }

    private fun initSearchAutoComplete() {
        val arrayAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line,resources.getStringArray(R.array.lang_array))
        act_langs_add_lang_profile_v2.setAdapter(arrayAdapter)
        act_langs_add_lang_profile_v2.hint = getString(R.string.search_lang)

        act_langs_add_lang_profile_v2.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            run {
                val selection = parent.getItemAtPosition(position) as String
                val chipToAdd = getChipToAdd(selection)
                act_langs_add_lang_profile_v2.setText("")
                language_chip_group_add_language.children.forEach {
                    if (it.tag == chipToAdd.tag) {
                        showToast("Language Already Added!!!")
                        return@run
                    }
                }
                language_chip_group_add_language.addView(chipToAdd, 0)

            }
        }

    }

    private fun intLangChipGroup() {
        chip_search_add_language.setOnCheckedChangeListener { _, isChecked ->
            act_langs_add_lang_profile_v2.isVisible = isChecked
            tv_save_add_lang.isVisible = isChecked
        }
        listOf(
                "English",
                "Hindi",
                "Telugu",
                "Kannada",
                "Bengali",
                "Bhojpuri",
                "Tamil",
                "Marathi"
        ).forEach { element ->
            language_chip_group_add_language.addView(getChipToAdd(element), 0)
        }

    }

    fun getChipToAdd(text: String): Chip {
        val chip = Chip(requireContext())
        val drawable = ChipDrawable.createFromAttributes(
                requireContext(),
                null,
                0,
                R.style.AppSingleChoiceChip2
        )
        chip.setChipDrawable(drawable)
        chip.tag=text
        chip.text = text
        chip.chipStrokeWidth = 1f
        return chip
    }


}