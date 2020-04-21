package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Achievement
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.view.delete
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.view.save
import kotlinx.android.synthetic.main.fragment_select_language.view.*
import java.text.SimpleDateFormat

class EditAchievementBottomSheet: BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = EditAchievementBottomSheet()
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
        layout = inflater.inflate(R.layout.edit_achievement_bottom_sheet, container, false)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lateinit var achievement: Achievement

        viewModel.userProfileData.observe(this, Observer { profile ->
            if (profile!!.Education!!.size >= 0) {
                achievement = profile!!.Achievement!![arrayLocation!!.toInt()]
                layout.title.setText(achievement.title)
                layout.authority.setText(achievement.issuingAuthority)
                layout.year.setText(achievement.year)
                layout.location.setText(achievement.location)
            }
        })

        layout.delete.setOnClickListener {
            Log.d("EditAchievement", "Deleting Achievement")
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    viewModel.removeProfileAchievement(achievement)
                    findNavController().navigate(R.id.educationExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
        }

        layout.save.setOnClickListener {
            if (validateAchievement()) {
                Log.d("EditAchievement", "Editing Achievement")
                viewModel.removeProfileAchievement(achievement!!)
                var newAchievement: ArrayList<Achievement> = ArrayList()
                newAchievement.add(
                    Achievement(
                        title = layout.title.text.toString(),
                        issuingAuthority = layout.authority.text.toString(),
                        year = layout.year.text.toString(),
                        location = layout.location.text.toString()
                    )
                )
                viewModel.setProfileAchievement(newAchievement)
                findNavController().navigate(R.id.educationExpandedFragment)
            }
            else {
                Toast.makeText(this.context, "Invalid Entry", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun validateAchievement(): Boolean {
        if (layout.title.text.toString() == "") {
            return false
        }
        if (layout.authority.text.toString() == "") {
            return false
        }
        if (layout.year.text.toString() == "" ||
            layout.year.text.toString().length != 4) {
            return false
        }
        if (layout.location.text.toString() == "") {
            return false
        }
        return true
    }
}