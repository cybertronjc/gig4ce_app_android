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

class AddLanguageBottomSheetFragment: BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = AddLanguageBottomSheetFragment()
    }

    lateinit var viewModel: ProfileViewModel
    lateinit var layout: View
    var updates: ArrayList<Language> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.add_language_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        layout.add_language_add_more.setOnClickListener{
            addNewLanguage()

            layout.add_language_name.setText("")
            layout.add_language_speaking_level.progress = 0
            layout.add_language_writing_level.progress = 0
        }

        layout.add_language_save.setOnClickListener{
            addNewLanguage()

            viewModel.setProfileLanguage(updates)
            findNavController().navigate(R.id.aboutExpandedFragment)
        }

        layout.add_language_cancel.setOnClickListener{
            findNavController().navigate(R.id.aboutExpandedFragment)
        }

    }

    private fun addNewLanguage() {
        updates.add(
            Language(
                name = layout.add_language_name.text.toString(),
                speakingSkill = layout.add_language_speaking_level.progress.toString(),
                writingSkill = layout.add_language_writing_level.progress.toString(),
                isMotherLanguage = layout.mother_language.isChecked.toString()
            )
        )
    }
}