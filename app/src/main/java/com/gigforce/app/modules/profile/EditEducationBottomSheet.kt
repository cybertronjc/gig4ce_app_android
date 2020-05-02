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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.end_date
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.start_date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditEducationBottomSheet: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = EditEducationBottomSheet()
    }

    lateinit var education: Education
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
        inflateView(R.layout.edit_education_bottom_sheet, inflater, container)

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setListeners()
    }

    private fun initialize() {
        val format = SimpleDateFormat("dd/MM/yyyy")

        degrees.addAll(listOf("--degree--", "<10th", "10th", "12th", "Certificate", "Diploma", "Bachelor", "Masters", "PhD"))
        val degreeAdapter = ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, degrees)
        val degreeSpinner = degree
        degreeSpinner.adapter = degreeAdapter
        degreeSpinner.onItemSelectedListener = object:
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDegree = if(position == 0) "" else degrees[position]
                Log.d("Spinner", "selected " + degrees[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //TODO("Not yet implemented")
            }
        }

        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            profile.educations?.let {
                val educations = it.sortedByDescending { education -> education.startYear!! }
                education = educations[arrayLocation!!.toInt()]
                institution.setText(education.institution)
                course.setText(education.course)
                selectedDegree = education.degree
                degree.setSelection(degrees.indexOf(education.degree))
                selectedStartDate = format.format(education.startYear!!)
                selectedEndDate = format.format(education.endYear!!)
                start_date.setText(format.format(education.startYear!!))
                end_date.setText(format.format(education.endYear!!))
            }
        })
    }

    private fun setListeners() {
        var calendar = Calendar.getInstance(TimeZone.getDefault())

        end_date.setOnClickListener {
            DatePickerDialog(this.context!!, DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                Log.d("TEMP", "tmp date")
                selectedEndDate = "$i2/${i1+1}/$i"
                end_date.setText(selectedEndDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        start_date.setOnClickListener {
            DatePickerDialog(this.context!!, DatePickerDialog.OnDateSetListener{
                    datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                Log.d("TEMP", "tmp date")
                selectedStartDate = "$i2/${i1+1}/$i"
                start_date.setText(selectedStartDate)
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }



        delete.setOnClickListener {
            Log.d("EditEducation", "VOILA!")
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    profileViewModel!!.removeProfileEducation(education!!)
                    findNavController().navigate(R.id.educationExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
        }

        save.setOnClickListener {
            Log.d("EditEducation", "updating")
            if (validateEducation()) {
                profileViewModel!!.removeProfileEducation(education)
                profileViewModel!!.removeProfileEducation(education)
                var newEducation = ArrayList<Education>()
                newEducation.add(
                    Education(
                        institution = institution.text.toString(),
                        course = course.text.toString(),
                        degree = selectedDegree.toString(),
                        startYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate.toString()),
                        endYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate.toString())
                    )
                )
                profileViewModel!!.setProfileEducation(newEducation)
                findNavController().navigate(R.id.educationExpandedFragment)
            }
        }
    }

    private fun validateEducation(): Boolean {
        if (validation!!.isValidEducation(
                institution,
                course,
                selectedDegree,
                selectedStartDate,
                selectedEndDate))
            return true
        else {
            showError(form_error, institution, course, start_date, end_date)
            return false
        }
    }
}