package com.gigforce.app.modules.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.utils.DropdownAdapter
import kotlinx.android.synthetic.main.add_experience_bottom_sheet.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddExperienceBottomSheet : ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = AddExperienceBottomSheet()
    }

    var employments: ArrayList<String> = ArrayList()
    var selectedEmployment: String = ""
    var selectedStartDate: String = ""
    var selectedEndDate: String = ""
    var currentlyWorkHere: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.add_experience_bottom_sheet, inflater, container)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        employments.addAll(listOf("Full time", "Internship", "Part time"))
        val employmentAdapter = DropdownAdapter(this.requireContext(), employments)
        val employmentSpinner = employment_type
        employmentSpinner.setAdapter(employmentAdapter)

        val calendar = Calendar.getInstance(TimeZone.getDefault())
        end_date.setOnClickListener {
            if (selectedStartDate.isEmpty()) {
                Toast.makeText(activity, getString(R.string.select_start_date), Toast.LENGTH_LONG)
                    .show();
                return@setOnClickListener
            }
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
            val datePicker = DatePickerDialog(
                this.requireContext(),

                DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                    Log.d("TEMP", "tmp date")
                    selectedEndDate = "$i2/${i1 + 1}/$i"
                    end_date.setText(selectedEndDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            );
            val calDate = Calendar.getInstance()
            calDate.time = dateFormatter.parse(selectedStartDate)
            calDate.add(Calendar.DATE, 1)
            datePicker.datePicker.minDate = calDate.timeInMillis
            //minus number would decrement the days
            datePicker.show()
        }

        start_date.setOnClickListener {
            val datePicker = DatePickerDialog(
                this.requireContext(),
                DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                    Log.d("TEMP", "tmp date")
                    selectedStartDate = "$i2/${i1 + 1}/$i"
                    start_date.setText(selectedStartDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.maxDate = System.currentTimeMillis();
            datePicker.show()
        }

        currently_work_here.setOnCheckedChangeListener { currently_work_here, isChecked ->
            Toast.makeText(context, "CHECKED", Toast.LENGTH_LONG).show()
            currentlyWorkHere = isChecked
            if (isChecked) {
                end_date.setText("")
                selectedEndDate = ""
                end_date.isEnabled = false
            } else {
                end_date.isEnabled = true
            }
        }

        add_more.setOnClickListener {
            if (validateExperience()) {
                addNewExperience()
                title.setText("")
                company.setText("")
                employment_type.setText("")
                location.setText("")
                start_date.setText("")
                end_date.setText("")
                currently_work_here.isChecked = false
                selectedStartDate = ""
                selectedEndDate = ""
                selectedEmployment = ""
                currentlyWorkHere = false
            }
        }

        cancel_button.setOnClickListener {
            this.findNavController().navigate(R.id.experienceExpandedFragment)
        }

        save_button.setOnClickListener {
            if (validateExperience()) {
                addNewExperience()
                this.findNavController().navigate(R.id.experienceExpandedFragment)
            }
        }

    }

    private fun addNewExperience() {
        hideError(form_error, title, company, employment_type, location, start_date, end_date)
        title.requestFocus()
        profileViewModel.setProfileExperience(
            Experience(
                title = title.text.toString(),
                company = company.text.toString(),
                employmentType = employment_type.text.toString(),
                location = location.text.toString(),
                startDate = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate),
                endDate = if (selectedEndDate.isNotEmpty()) SimpleDateFormat("dd/MM/yyyy").parse(
                    selectedEndDate
                ) else null,
                currentExperience = currentlyWorkHere
            )
        )
        Log.d("STATUS", "Add Experience to List Successful")
    }

    private fun validateExperience(): Boolean {
        if (validation!!.isValidExperience(
                title,
                company,
                employment_type.text.toString(),
                location,
                selectedStartDate,
                selectedEndDate,
                currentlyWorkHere
            )
        ) {
            return true
        } else {
            if (currentlyWorkHere) {
                showError(form_error, title, company, employment_type, location, start_date)
            } else {
                showError(
                    form_error,
                    title,
                    company,
                    employment_type,
                    location,
                    start_date,
                    end_date
                )
            }
            return false
        }
    }
}