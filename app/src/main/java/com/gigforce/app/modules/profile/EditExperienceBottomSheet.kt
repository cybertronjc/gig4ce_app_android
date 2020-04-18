package com.gigforce.app.modules.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Experience
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.edit_experience.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditExperienceBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = EditExperienceBottomSheet()
    }

    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel
    var arrayLocation: String = ""
    var employments: ArrayList<String> = ArrayList()
    var selectedEmployment: String = ""
    var locations: ArrayList<String> = ArrayList()
    var selectedLocation: String = ""
    lateinit var experience: Experience
    var selectedStartDate: String = ""
    var selectedEndDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arrayLocation = it.getString("array_location")!!
        }
    }

    override fun onCreateView (
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.edit_experience, container, false)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        employments.addAll(listOf("--employment type--", "Full time", "internship", "Part time"))
        locations.addAll(listOf("--location--", "Hyderabad", "Bangalore"))

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        val employmentAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, employments)
        val employmentSpinner = layout.employment_type
        employmentSpinner.adapter = employmentAdapter
        employmentSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedEmployment = employments[position]
                Log.d("Spinner", "selected " + employments[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }

        layout.end_date.setOnClickListener {
            var calendar = Calendar.getInstance(TimeZone.getDefault())

            var dialog = DatePickerDialog(this.context!!, DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                Log.d("TEMP", "tmp date")
                selectedEndDate = "$i2/${i1+1}/$i"
                layout.end_date.setText(selectedEndDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }

        layout.start_date.setOnClickListener {
            var calendar = Calendar.getInstance(TimeZone.getDefault())

            var dialog = DatePickerDialog(this.context!!, DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                selectedStartDate = "$i2/${i1+1}/$i"
                Log.d("TEMP", "tmp date " + selectedStartDate)
                layout.start_date.setText(selectedStartDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }

        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        viewModel.userProfileData.observe(this, Observer { profile ->
            if (profile!!.Experience!!.size >= 0) {
                experience = profile.Experience!![arrayLocation.toInt()]
                layout.title.setText(experience.title)
                layout.employment_type.setSelection(employments.indexOf(experience.employmentType))
                layout.location.setSelection(locations.indexOf(experience.location))
                selectedStartDate = format.format(experience.startDate!!)
                selectedEndDate = format.format(experience.endDate!!)
                selectedLocation = experience.location
                selectedEmployment = experience.employmentType
                layout.start_date.setText(format.format(experience.startDate!!))
                layout.end_date.setText(format.format(experience.endDate!!))
            }
        })

        layout.delete_button.setOnClickListener {
            Log.d("EditExperience", "Deleting Experience")
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    viewModel.removeProfileExperience(experience!!)
                    findNavController().navigate(R.id.experienceExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
        }

        layout.save_button.setOnClickListener {
            Log.d("EditExperience", "Editing Experience")
            viewModel.removeProfileExperience(experience!!)
            val newExperience: ArrayList<Experience> = ArrayList()
            newExperience.add(
                Experience(
                    title = layout.title.text.toString(),
                    employmentType = selectedEmployment,
                    location = selectedLocation,
                    startDate = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate),
                    endDate = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate)
                )
            )
            viewModel.setProfileExperience(newExperience)
            findNavController().navigate(R.id.experienceExpandedFragment)
        }
    }
}