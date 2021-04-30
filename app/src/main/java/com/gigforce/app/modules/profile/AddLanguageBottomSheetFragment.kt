package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Language
import kotlinx.android.synthetic.main.add_language_bottom_sheet.*

class AddLanguageBottomSheetFragment : ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = AddLanguageBottomSheetFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.add_language_bottom_sheet, inflater, container)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        add_language_add_more.setOnClickListener {
            addNewLanguage()

            add_language_name.setText("")
            add_language_speaking_level.progress = 0
            arround_current_add_seekbar.progress = 0
        }

        mother_language.setOnCheckedChangeListener { mother_language, isChecked ->
        }

        add_language_cancel.setOnClickListener {
            this.dismiss()
        }

        add_language_save.setOnClickListener {
            if (validateLanguage()) {
                addNewLanguage()
                this.dismiss()
            }
        }
    }

    private fun addNewLanguage() {
        hideError(form_error, add_language_name)
        profileViewModel.setProfileLanguage(
            Language(
                name = add_language_name.text.toString(),
                speakingSkill = add_language_speaking_level.progress.toString(),
                writingSkill = arround_current_add_seekbar.progress.toString(),
                isMotherLanguage = mother_language.isChecked
            )
        )
    }

    private fun validateLanguage(): Boolean {
        if (validation!!.isValidLanguage(add_language_name))
            return true
        else {
            showError(form_error, add_language_name)
            return false
        }
    }
}