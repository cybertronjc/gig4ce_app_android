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
import com.gigforce.app.modules.profile.models.Achievement
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.cancel
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.title

class EditAchievementBottomSheet: ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = EditAchievementBottomSheet()
    }

    var arrayLocation: String = ""
    lateinit var achievement: Achievement

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
       inflateView(R.layout.edit_achievement_bottom_sheet, inflater, container)

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()
    }

    private fun initialize() {
        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            profile?.achievements?.let {
                val achievements = it.sortedByDescending { achievement -> achievement.year }
                achievement = achievements[arrayLocation!!.toInt()]
                title.setText(achievement.title)
                authority.setText(achievement.issuingAuthority)
                year.setText(achievement.year)
                location.setText(achievement.location)
            }
        })
    }

    private fun setListeners() {

        year.setOnClickListener {
            showNumberPicker(requireContext(), year, year.text.toString().toInt())
        }

        delete.setOnClickListener {
            Log.d("EditAchievement", "Deleting Achievement")
            val dialog = getDeleteConfirmationDialog(requireContext())
            dialog.yes.setOnClickListener {
                profileViewModel.removeProfileAchievement(achievement)
                findNavController().navigate(R.id.educationExpandedFragment)
                dialog .dismiss()
            }
            dialog.show()
        }

        save.setOnClickListener {
            if (validateAchievement()) {
                Log.d("EditAchievement", "Editing Achievement")
                profileViewModel.removeProfileAchievement(achievement!!)
                profileViewModel.setProfileAchievement(
                    Achievement(
                        title = title.text.toString(),
                        issuingAuthority = authority.text.toString(),
                        year = year.text.toString(),
                        location = location.text.toString()
                    )
                )
                findNavController().navigate(R.id.educationExpandedFragment)
            }
        }

        cancel.setOnClickListener {
            findNavController().navigate(R.id.educationExpandedFragment)
        }

    }

    private fun validateAchievement(): Boolean {
        if (validation!!.isValidAchievement(
                title,
                authority,
                year
            )
        ) {
            return true
        } else {
            showError(form_error, title, authority, year)
            return false
        }
    }
}