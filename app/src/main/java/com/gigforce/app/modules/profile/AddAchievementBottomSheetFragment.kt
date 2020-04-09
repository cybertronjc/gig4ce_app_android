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
    var years: ArrayList<String> = ArrayList()
    var selectedYear: String = ""

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
            layout.add_achievement_location.setSelection(0)
            layout.add_achievement_year.setSelection(0)
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
                    selectedLocation = locations[position]
                    Log.d("Spinner", "selected " + locations[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //TODO("Not yet implemented")
                }
        }

        years.addAll(listOf("--year--", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020"))
        val yearAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, years)
        val yearSpinner = layout.add_achievement_year
        yearSpinner.adapter = yearAdapter
        yearSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedYear = years[position]
                Log.d("Spinner", "selected " + years[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
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
                location = selectedLocation,
                year = selectedYear
            )
        )
    }
}