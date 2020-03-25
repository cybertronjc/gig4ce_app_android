package com.gigforce.app.modules.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel
    var updates: ArrayList<Education> = ArrayList()
    var selectedEndDate: String = ""
    var selectedStartDate: String = ""

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
            addNewEducation()
            layout.institution_name.setText("")
            layout.course_name.setText("")
            layout.degree_name.setText("")
            layout.start_date.setText("")
            layout.end_date.setText("")
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

        layout.save_button.setOnClickListener{
            addNewEducation()
            viewModel.setProfileEducation(updates)
            Toast.makeText(this.context, "Updated Education Section", Toast.LENGTH_LONG)
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }
    }

    private fun addNewEducation() {
        updates.add(Education(
            institution = layout.institution_name.text.toString(),
            course = layout.course_name.text.toString(),
            degree = layout.degree_name.text.toString(),
            startYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate.toString()),
            endYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate.toString())
        ))
    }
}