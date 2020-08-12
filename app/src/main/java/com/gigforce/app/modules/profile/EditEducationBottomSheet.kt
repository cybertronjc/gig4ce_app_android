package com.gigforce.app.modules.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Education
import com.gigforce.app.utils.DropdownAdapter
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_education_bottom_sheet.cancel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditEducationBottomSheet : ProfileBaseBottomSheetFragment() {
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

        degrees.addAll(
            listOf(
                "<10th",
                "10th",
                "12th",
                getString(R.string.certificate),
                getString(R.string.diploma),
                getString(
                    R.string.bachelor
                ),
                getString(R.string.masters),
                getString(R.string.phd)
            )
        )
        val degreeAdapter = DropdownAdapter(this.requireContext(), degrees)
        val degreeSpinner = degree
        degreeSpinner.setAdapter(degreeAdapter)

        profileViewModel.userProfileData.observe(this, Observer { profile ->
            profile.educations?.let {
                val educations = it.sortedByDescending { education -> education.startYear!! }
                education = educations[arrayLocation!!.toInt()]
                institution.setText(education.institution)
                course.setText(education.course)
                selectedDegree = education.degree
                degree.setText(education.degree, false)
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
            DatePickerDialog(
                this.requireContext(),
                DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                    Log.d("TEMP", "tmp date")
                    selectedEndDate = "$i2/${i1 + 1}/$i"
                    end_date.setText(selectedEndDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        start_date.setOnClickListener {
            DatePickerDialog(
                this.requireContext(),
                DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                    Log.d("TEMP", "tmp date")
                    selectedStartDate = "$i2/${i1 + 1}/$i"
                    start_date.setText(selectedStartDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        delete.setOnClickListener {
            Log.d("EditEducation", "VOILA!")
            val dialog = getDeleteConfirmationDialog(requireContext())
            dialog.yes.setOnClickListener {
                profileViewModel.removeProfileEducation(education)
                findNavController().navigate(R.id.educationExpandedFragment)
                dialog.dismiss()
            }
            dialog.show()
        }

        save.setOnClickListener {
            Log.d("EditEducation", "updating")
            if (validateEducation()) {
                profileViewModel.removeProfileEducation(education)
                profileViewModel.setProfileEducation(
                    Education(
                        institution = institution.text.toString(),
                        course = course.text.toString(),
                        degree = degree.text.toString(),
                        startYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate.toString()),
                        endYear = SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate.toString())
                    )
                )
                findNavController().navigate(R.id.educationExpandedFragment)
            }
        }

        cancel.setOnClickListener {
            findNavController().navigate(R.id.educationExpandedFragment)
        }
    }

    private fun validateEducation(): Boolean {
        if (validation!!.isValidEducation(
                institution,
                course,
                degree.text.toString(),
                selectedStartDate,
                selectedEndDate
            )
        )
            return true
        else {
            showError(form_error, institution, course, degree, start_date, end_date)
            return false
        }
    }
}