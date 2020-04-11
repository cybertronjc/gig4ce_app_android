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
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.view.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.view.end_date
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.view.start_date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditEducationBottomSheet: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = EditEducationBottomSheet()
    }

    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel

    var education: Education? = null
    var arrayLocation: String? = ""

    var degrees: ArrayList<String> = ArrayList()
    var selectedDegree: String? = ""

    lateinit var selectedStartDate: String
    lateinit var selectedEndDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arrayLocation = it.getString("array_location")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("LOCATION", "MYLOC" + arrayLocation.toString())
        layout = inflater.inflate(R.layout.edit_education_bottom_sheet, container, false)
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        degrees.addAll(listOf("--degree--", "Btech", "BA", "MA", "MS", "polytech"))
        val degreeAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, degrees)
        val degreeSpinner = layout.degree
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

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val format = SimpleDateFormat("dd/MM/yyyy")
        lateinit var education: Education

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

        viewModel.userProfileData.observe(this, Observer { profile ->
            if (profile!!.Education!!.size >= 0) {
                education = profile!!.Education!![arrayLocation!!.toInt()]
                layout.institution.setText(education.institution)
                layout.course.setText(education.course)
                selectedDegree = education.degree
                layout.degree.setSelection(degrees.indexOf(education.degree))
                selectedStartDate = format.format(education.startYear!!)
                selectedEndDate = format.format(education.endYear!!)
                layout.start_date.setText(format.format(education.startYear!!))
                layout.end_date.setText(format.format(education.endYear!!))
            }
        })

        layout.delete.setOnClickListener {
            Log.d("EditEducation", "VOILA!")
            viewModel.removeProfileEducation(education!!)
            findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.save.setOnClickListener {
            Log.d("EditEducation", "updating")
            viewModel.removeProfileEducation(education)
            var newEducation = ArrayList<Education>()
            newEducation.add(Education(
                institution = layout.institution.text.toString(),
                course = layout.course.text.toString(),
                degree = selectedDegree.toString(),
                startYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate.toString()),
                endYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate.toString())
            ))
            viewModel.setProfileEducation(newEducation)
            findNavController().navigate(R.id.educationExpandedFragment)
        }
    }
}