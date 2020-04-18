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
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Experience
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_experience_bottom_sheet.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddExperienceBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddExperienceBottomSheet()
    }

    private lateinit var viewModel: ProfileViewModel
    private lateinit var layout: View
    var updates: ArrayList<Experience> = ArrayList()
    var employments: ArrayList<String> = ArrayList()
    var selectedEmployment: String = ""
    var selectedStartDate: String = ""
    var selectedEndDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.add_experience_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        layout.add_experience_cancel.setOnClickListener{
            this.findNavController().navigate(R.id.experienceExpandedFragment)
        }

        layout.add_experience_add_more.setOnClickListener{
            if (validateExperience()) {
                addNewExperience()
                layout.add_experience_title.setText("")
                layout.add_experience_company.setText("")
                layout.add_experience_employment_type.setSelection(0)
                layout.add_experience_location.setText("")
                layout.add_experience_start_date.setText("")
                layout.add_experience_end_date.setText("")
            }
            else {
                Toast.makeText(this.context, "Invalid Entry", Toast.LENGTH_LONG).show()
            }
        }

        employments.addAll(listOf("--employment type--", "Full time", "internship", "Part time"))
        val employmentAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, employments)
        val employmentSpinner = layout.add_experience_employment_type
        employmentSpinner.adapter = employmentAdapter
        employmentSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedEmployment = if (position != 0) employments[position] else ""
                Log.d("Spinner", "selected " + employments[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }


        layout.add_experience_end_date.setOnClickListener {
            var calendar = Calendar.getInstance(TimeZone.getDefault())

            var dialog = DatePickerDialog(this.context!!, DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                Log.d("TEMP", "tmp date")
                selectedEndDate = "$i2/${i1+1}/$i"
                layout.add_experience_end_date.setText(selectedEndDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }

        layout.add_experience_start_date.setOnClickListener {
            var calendar = Calendar.getInstance(TimeZone.getDefault())

            var dialog = DatePickerDialog(this.context!!, DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                Log.d("TEMP", "tmp date")
                selectedStartDate = "$i2/${i1+1}/$i"
                layout.add_experience_start_date.setText(selectedStartDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }

        layout.add_experience_save.setOnClickListener{
            if (validateExperience()) {
                addNewExperience()

                viewModel.setProfileExperience(updates)
                this.findNavController().navigate(R.id.experienceExpandedFragment)
            }
            else {
                Toast.makeText(this.context, "Invalid Entry", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun addNewExperience() {
        updates.add(
            Experience(
                title = layout.add_experience_title.text.toString(),
                company = layout.add_experience_company.text.toString(),
                employmentType = selectedEmployment,
                location = layout.add_experience_location.text.toString(),
                startDate = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate),
                endDate = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate)
            )

        )
    }

    private fun validateExperience(): Boolean {
        if (layout.add_experience_title.text.toString() == "")
            return false
        if (layout.add_experience_company.text.toString() == "")
            return false
        if (selectedEmployment == "")
            return false
        if (layout.add_experience_location.text.toString() == "")
            return false
        if (selectedStartDate == "")
            return false
        if (selectedEndDate == "")
        if (selectedEmployment == "")
            return false
        return true
    }
}