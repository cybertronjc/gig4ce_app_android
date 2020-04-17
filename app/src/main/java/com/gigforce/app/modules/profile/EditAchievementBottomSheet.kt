package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import java.text.SimpleDateFormat

class EditAchievementBottomSheet: BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = EditAchievementBottomSheet()
    }

    lateinit var layout: View
    var arrayLocation: String = ""
    var locations: ArrayList<String> = ArrayList()
    var years: ArrayList<String> = ArrayList()
    var selectedLocation: String = ""
    var selectedYear: String = ""
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
        locations.addAll(listOf("--location--", "Hyderabad", "Bangalore", "Delhi", "Mumbai"))
        years.addAll(listOf("--year--", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019"))

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val format = SimpleDateFormat("dd/MM/yyyy")
        lateinit var achievement: Achievement

        val yearAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, years)
        val yearSpinner = layout.year
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

        val locationAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, locations)
        val locationSpinner = layout.location
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

        viewModel.userProfileData.observe(this, Observer { profile ->
            if (profile!!.Education!!.size >= 0) {
                achievement = profile!!.Achievement!![arrayLocation!!.toInt()]
                layout.title.setText(achievement.title)
                layout.authority.setText(achievement.issuingAuthority)
                selectedLocation = achievement.location.toString()
                layout.location.setSelection(locations.indexOf(selectedLocation))
                selectedYear = achievement.year.toString()
                layout.year.setSelection(years.indexOf(selectedYear))
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
                        location = selectedLocation,
                        year = selectedYear
                    )
                )
                viewModel.setProfileAchievement(newAchievement)
                findNavController().navigate(R.id.educationExpandedFragment)
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
        if (layout.year.toString() == "") {
            return false
        }
        return true
    }
}