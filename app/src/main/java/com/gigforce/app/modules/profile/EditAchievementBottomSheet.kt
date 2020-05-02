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
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.*

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
            profile.achievements?.let {
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

        delete.setOnClickListener {
            Log.d("EditAchievement", "Deleting Achievement")
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    profileViewModel!!.removeProfileAchievement(achievement)
                    findNavController().navigate(R.id.educationExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
        }

        save.setOnClickListener {
            if (validateAchievement()) {
                Log.d("EditAchievement", "Editing Achievement")
                profileViewModel!!.removeProfileAchievement(achievement!!)
                var newAchievement: ArrayList<Achievement> = ArrayList()
                newAchievement.add(
                    Achievement(
                        title = title.text.toString(),
                        issuingAuthority = authority.text.toString(),
                        year = year.text.toString(),
                        location = location.text.toString()
                    )
                )
                profileViewModel!!.setProfileAchievement(newAchievement)
                findNavController().navigate(R.id.educationExpandedFragment)
            }
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