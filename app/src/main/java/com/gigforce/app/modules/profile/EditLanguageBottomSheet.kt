package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Language
import kotlinx.android.synthetic.main.edit_language_bottom_sheet.*

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
            profile.languages?.let {
                val languages = it.sortedByDescending { language -> language.speakingSkill }
                language = languages[arrayLocation.toInt()]
                language_name.setText(language.name)
                mother_language.isChecked = language.isMotherLanguage == "true"
                language_speaking_level.progress = language.speakingSkill.toInt()
                language_writing_level.progress = language.writingSkill.toInt()
            }
        })
    }

    private fun setListeners() {
        delete.setOnClickListener {
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    profileViewModel!!.removeProfileLanguage(language)
                    findNavController().navigate(R.id.aboutExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
        }

        save.setOnClickListener {
            if (validateLanguage()) {
                Log.d("EditLanguage", "Editing Language")
                profileViewModel!!.removeProfileLanguage(language!!)
                var newLanguage: ArrayList<Language> = ArrayList()
                newLanguage.add(
                    Language(
                        name = language_name.text.toString(),
                        speakingSkill = language_speaking_level.progress.toString(),
                        writingSkill = language_writing_level.progress.toString(),
                        isMotherLanguage = if(mother_language.isChecked) "true" else "false"
                    )
                )
                profileViewModel!!.setProfileLanguage(newLanguage)
                findNavController().navigate(R.id.aboutExpandedFragment)
            }
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