package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Achievement
import com.gigforce.app.modules.profile.models.Skill
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_achievement_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.view.*
import java.text.SimpleDateFormat

class AddAchievementBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddAchievementBottomSheetFragment()
    }

    lateinit var layout: View
    var updates: ArrayList<Achievement> = ArrayList()
    lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.add_achievement_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        layout.add_achievement_cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.add_achievement_add_more_button.setOnClickListener{
            addNewAchievement()

            layout.add_achievement_title.setText("")
            layout.add_achievement_authority.setText("")
            layout.add_achievement_location.setText("")
            layout.add_achievement_year.setText("")
        }

        layout.add_achievement_save_button.setOnClickListener{
            addNewAchievement()

            viewModel.setProfileAchievement(updates)
            Toast.makeText(this.context, "Updated Achievement Section", Toast.LENGTH_LONG)
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }
    }

    private fun addNewAchievement() {
        updates.add(
            Achievement(
                title = layout.add_achievement_title.text.toString(),
                issuingAuthority = layout.add_achievement_authority.text.toString(),
                location = layout.add_achievement_location.text.toString(),
                //year = SimpleDateFormat("dd/MM/yyyy").parse(layout.add_achievement_year.text.toString())
                year = layout.add_achievement_year.text.toString()
            )
        )
    }
}