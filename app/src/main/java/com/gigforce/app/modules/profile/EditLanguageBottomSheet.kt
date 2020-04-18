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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.edit_language_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_language_bottom_sheet.view.delete
import kotlinx.android.synthetic.main.edit_language_bottom_sheet.view.save

class EditLanguageBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = EditLanguageBottomSheet()
    }

    lateinit var layout: View
    var arrayLocation: String = ""
    lateinit var viewModel: ProfileViewModel
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
        layout = inflater.inflate(R.layout.edit_language_bottom_sheet, container, false)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lateinit var language: Language
        var isMotherLanguage = "false"

        viewModel.userProfileData.observe(this, Observer { profile ->
            if (profile!!.Language!!.size >= 0) {
                language = profile!!.Language!![arrayLocation.toInt()]
                layout.language_name.setText(language.name)
                if (language.isMotherLanguage.toString() == "true") {
                    layout.mother_language.isChecked = true
                }
                layout.language_speaking_level.progress = language.speakingSkill.toInt()
                layout.language_writing_level.progress = language.writingSkill.toInt()
            }
        })

        layout.delete.setOnClickListener {
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    viewModel.removeProfileLanguage(language)
                    findNavController().navigate(R.id.aboutExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
        }

        layout.save.setOnClickListener {
            if (validateLanguage()) {
                Log.d("EditLanguage", "Editing Language")
                viewModel.removeProfileLanguage(language!!)
                var newLanguage: ArrayList<Language> = ArrayList()
                newLanguage.add(
                    Language(
                        name = layout.language_name.text.toString(),
                        speakingSkill = layout.language_speaking_level.progress.toString(),
                        writingSkill = layout.language_writing_level.progress.toString(),
                        isMotherLanguage = layout.mother_language.isChecked.toString()
                    )
                )
                viewModel.setProfileLanguage(newLanguage)
                findNavController().navigate(R.id.aboutExpandedFragment)
            }
        }
    }

    private fun validateLanguage(): Boolean {
        if (layout.language_name.text.toString() == "")
            return false
        return true
    }
}