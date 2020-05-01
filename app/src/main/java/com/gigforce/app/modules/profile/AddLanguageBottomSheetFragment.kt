package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_language_bottom_sheet.view.*
import com.gigforce.app.modules.profile.models.Language
import kotlinx.android.synthetic.main.add_language_bottom_sheet.*
import kotlinx.android.synthetic.main.add_language_bottom_sheet.view.add_language_cancel

class AddLanguageBottomSheetFragment: ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = AddLanguageBottomSheetFragment()
    }

    var updates: ArrayList<Language> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.add_language_bottom_sheet, inflater, container)
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        add_language_add_more.setOnClickListener{
            addNewLanguage()

            add_language_name.setText("")
            add_language_speaking_level.progress = 0
            add_language_writing_level.progress = 0
        }

        mother_language.setOnCheckedChangeListener { mother_language, isChecked ->
        }

        add_language_cancel.setOnClickListener{
            findNavController().navigate(R.id.aboutExpandedFragment)
        }

        add_language_save.setOnClickListener{
            if (validateLanguage()) {
                addNewLanguage()

                profileViewModel!!.setProfileLanguage(updates)
                findNavController().navigate(R.id.aboutExpandedFragment)
            }
        }
    }

    private fun addNewLanguage() {
        hideError(form_error, add_language_name)
        updates.add(
            Language(
                name = add_language_name.text.toString(),
                speakingSkill = add_language_speaking_level.progress.toString(),
                writingSkill = add_language_writing_level.progress.toString(),
                isMotherLanguage = if(mother_language.isChecked) "true" else "false"
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