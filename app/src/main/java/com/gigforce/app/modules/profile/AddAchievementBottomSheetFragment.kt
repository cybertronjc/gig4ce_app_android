package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import kotlinx.android.synthetic.main.edit_achievement_bottom_sheet.view.*
import java.text.SimpleDateFormat

class AddAchievementBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddAchievementBottomSheetFragment()
    }

    lateinit var layout: View
    var updates: ArrayList<Achievement> = ArrayList()
    lateinit var viewModel: ProfileViewModel
    var locations: ArrayList<String> = ArrayList()
    var selectedLocation: String = ""

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
            if (validateAchievement()) {
                addNewAchievement()

                layout.add_achievement_title.setText("")
                layout.add_achievement_authority.setText("")
                layout.add_achievement_location.setSelection(0)
                layout.add_achievement_year.setText("")
            }
            else {
                Toast.makeText(this.context, "Invalid Entry", Toast.LENGTH_LONG).show()
            }
        }

        locations.add("--location--")
        locations.add("Hyderabad")
        locations.add("Bangalore")
        val locationAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, locations)
        val locationSpinner = layout.add_achievement_location
        locationSpinner.adapter = locationAdapter
        locationSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedLocation = if (position != 0) locations[position] else ""
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //TODO("Not yet implemented")
                }
        }

        layout.add_achievement_save_button.setOnClickListener{
            if (validateAchievement()) {
                addNewAchievement()

                viewModel.setProfileAchievement(updates)
                Toast.makeText(this.context, "Updated Achievement Section", Toast.LENGTH_LONG).show()
                this.findNavController().navigate(R.id.educationExpandedFragment)
            }
            else {
                Toast.makeText(this.context, "Invalid Entry", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addNewAchievement() {
        updates.add(
            Achievement(
                title = layout.add_achievement_title.text.toString(),
                issuingAuthority = layout.add_achievement_authority.text.toString(),
                location = selectedLocation,
                year = layout.add_achievement_year.text.toString()
            )
        )
    }

    private fun validateAchievement(): Boolean {
        if (layout.add_achievement_title.text.toString() == "") {
            return false
        }
        if (layout.add_achievement_authority.text.toString() == "") {
            return false
        }
        if (layout.add_achievement_year.text.toString() == "" ||
                layout.add_achievement_year.text.toString().length != 4) {
            return false
        }
        if (selectedLocation == "") {
            return false
        }
        return true
    }
}