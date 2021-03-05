package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.core.datamodels.profile.Language
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import kotlinx.android.synthetic.main.edit_language_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_language_bottom_sheet.cancel

class EditLanguageBottomSheet: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = EditLanguageBottomSheet()
    }

    var arrayLocation: String = ""
    lateinit var language: Language
    var isMotherLanguage = "false"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arrayLocation = it.getString("array_location")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.edit_language_bottom_sheet, inflater, container)

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialize()
        setListeners()
    }

    private fun initialize() {
        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            profile?.languages?.let {
                val languages = it.sortedByDescending { language -> language.speakingSkill }
                language = languages[arrayLocation.toInt()]
                language_name.setText(language.name)
                mother_language.isChecked = language.isMotherLanguage
                language_speaking_level.progress = language.speakingSkill.toInt()
                language_writing_level.progress = language.writingSkill.toInt()
            }
        })
    }

    private fun setListeners() {
        delete.setOnClickListener {
            val dialog = getDeleteConfirmationDialog(requireContext())
            dialog.yes.setOnClickListener {
                profileViewModel.removeProfileLanguage(language)
                findNavController().navigate(R.id.aboutExpandedFragment)
                dialog .dismiss()
            }
            dialog.show()

        }

        save.setOnClickListener {
            if (validateLanguage()) {
                Log.d("EditLanguage", "Editing Language")
                profileViewModel!!.removeProfileLanguage(language!!)
                profileViewModel.setProfileLanguage(
                    Language(
                        name = language_name.text.toString(),
                        speakingSkill = language_speaking_level.progress.toString(),
                        writingSkill = language_writing_level.progress.toString(),
                        isMotherLanguage = mother_language.isChecked
                    )
                )
                findNavController().navigate(R.id.aboutExpandedFragment)
            }
        }

        cancel.setOnClickListener {
            findNavController().navigate(R.id.aboutExpandedFragment)
        }

    }

    private fun validateLanguage(): Boolean {
        if (validation!!.isValidLanguage(language_name))
            return true
        else {
            showError(form_error, language_name)
            return false
        }
    }
}