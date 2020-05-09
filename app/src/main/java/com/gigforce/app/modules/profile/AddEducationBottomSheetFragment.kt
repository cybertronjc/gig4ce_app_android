package com.gigforce.app.modules.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.utils.DropdownAdapter
import kotlinx.android.synthetic.main.add_education_bottom_sheet.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddEducationBottomSheetFragment: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = AddEducationBottomSheetFragment()
    }

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
        inflateView(R.layout.add_education_bottom_sheet, inflater, container)

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {

        var calendar = Calendar.getInstance(TimeZone.getDefault())

        start_date.setOnClickListener {
            DatePickerDialog(this.requireContext(), DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                Log.d("TEMP", "tmp date")
                selectedStartDate = "$i2/${i1+1}/$i"
                start_date.setText(selectedStartDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        end_date.setOnClickListener {
            DatePickerDialog(this.requireContext(), DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                Log.d("TEMP", "tmp date")
                selectedEndDate = "$i2/${i1+1}/$i"
                end_date.setText(selectedEndDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        degrees.addAll(listOf("<10th", "10th", "12th", "Certificate", "Diploma", "Bachelor", "Masters", "PhD"))
        val degreeAdapter = DropdownAdapter(this.requireContext(), degrees)
        val degreeSpinner = degree_name
        degreeSpinner.setAdapter(degreeAdapter)

        add_more_button.setOnClickListener{
            if (validateEducation()) {
                hideError(form_error, institution_name, course_name, start_date, end_date)
                addNewEducation()
                institution_name.setText("")
                course_name.setText("")
                degree_name.setText("")
                start_date.setText("")
                end_date.setText("")
            }
        }

        save_button.setOnClickListener{
            if(validateEducation()) {
                addNewEducation()
                profileViewModel!!.setProfileEducation(updates)
                Toast.makeText(this.context, "Updated Education Section", Toast.LENGTH_LONG).show()
                this.findNavController().navigate(R.id.educationExpandedFragment)
            }
        }

        cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

    }

    private fun addNewEducation() {
        hideError(form_error, institution_name, course_name, degree_name, start_date, end_date)
        updates.add(Education(
            institution = institution_name.text.toString(),
            course = course_name.text.toString(),
            degree = degree_name.text.toString(),
            startYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate.toString()),
            endYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate.toString())
        ))
    }

    private fun validateEducation(): Boolean {
        if (validation!!.isValidEducation(
                institution_name,
                course_name,
                degree_name.text.toString(),
                selectedStartDate,
                selectedEndDate))
            return true
        else {
            showError(form_error, institution_name, course_name, degree_name, start_date, end_date)
            return false
        }
    }
}