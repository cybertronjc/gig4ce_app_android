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
import com.gigforce.app.modules.profile.models.Education
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_achievement_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_education_bottom_sheet.*
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.end_date
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class AddEducationBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddEducationBottomSheetFragment()
    }

    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel
    var updates: ArrayList<Education> = ArrayList()
    var selectedEndDate: String = ""
    var selectedStartDate: String = ""
    var degrees: ArrayList<String> = ArrayList()
    var selectedDegree: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        layout = inflater.inflate(R.layout.add_education_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        layout.cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.add_more_button.setOnClickListener{
            if (validateEducation()) {
                addNewEducation()
                layout.institution_name.setText("")
                layout.course_name.setText("")
                layout.degree_name.setSelection(0)
                layout.start_date.setText("")
                layout.end_date.setText("")
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
                Log.d("TEMP", "tmp date")
                selectedStartDate = "$i2/${i1+1}/$i"
                layout.start_date.setText(selectedStartDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            dialog.show()
        }

        degrees.addAll(listOf("--degree--", "<10th", "10th", "12th", "Certificate", "Diploma", "Bachelor", "Masters", "PhD"))
        val degreeAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, degrees)
        val degreeSpinner = layout.degree_name
        degreeSpinner.adapter = degreeAdapter
        degreeSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDegree = degrees[position]
                Log.d("Spinner", "selected " + degrees[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }

        layout.save_button.setOnClickListener{
            if(validateEducation()) {
                addNewEducation()
                viewModel.setProfileEducation(updates)
                Toast.makeText(this.context, "Updated Education Section", Toast.LENGTH_LONG)
                this.findNavController().navigate(R.id.educationExpandedFragment)
            }
        }
    }

    private fun addNewEducation() {
        updates.add(Education(
            institution = layout.institution_name.text.toString(),
            course = layout.course_name.text.toString(),
            degree = selectedDegree,
            startYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate.toString()),
            endYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate.toString())
        ))
    }

    private fun validateEducation(): Boolean {
        if (layout.start_date.text.toString() == "")
            return false
        if (layout.institution_name.text.toString() == "")
            return false
        return true
    }
}