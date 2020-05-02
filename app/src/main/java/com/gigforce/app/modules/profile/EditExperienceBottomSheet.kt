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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Experience
import kotlinx.android.synthetic.main.edit_experience.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditExperienceBottomSheet: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = EditExperienceBottomSheet()
    }

    var arrayLocation: String = ""
    var employments: ArrayList<String> = ArrayList()
    var selectedEmployment: String = ""
    lateinit var experience: Experience
    var selectedStartDate: String = ""
    var selectedEndDate: String = ""
    var currentlyWorkHere: Boolean = false

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
        inflateView(R.layout.edit_experience, inflater, container)
        employments.addAll(listOf("--employment type--", "Full time", "internship", "Part time"))

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()
    }

    private fun initialize() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            profile.experiences?.let {
                val experiences = it.sortedByDescending { experience -> experience.startDate  }
                experience = experiences[arrayLocation.toInt()]
                title.setText(experience.title)
                company.setText(experience.company)
                employment_type.setSelection(employments.indexOf(experience.employmentType))
                location.setText(experience.location)
                selectedStartDate = format.format(experience.startDate!!)
                experience.endDate?.let {
                    selectedEndDate = format.format(experience.endDate!!)
                    end_date.setText(format.format(experience.endDate!!))
                }
                selectedEmployment = experience.employmentType
                start_date.setText(format.format(experience.startDate!!))
                if (experience.currentExperience) {
                    currently_work_here.isChecked = true
                    currentlyWorkHere = true
                }
            }
        })
    }

    private fun setListeners() {
        val employmentAdapter =
            ArrayAdapter(this.context!!, R.layout.simple_spinner_dropdown_item, employments)
        val employmentSpinner = employment_type
        employmentSpinner.adapter = employmentAdapter
        employmentSpinner.onItemSelectedListener = object :
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

        var calendar = Calendar.getInstance(TimeZone.getDefault())
        end_date.setOnClickListener {
            DatePickerDialog(
                this.context!!,
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
                this.context!!,
                DatePickerDialog.OnDateSetListener { datePicker: DatePicker, i: Int, i1: Int, i2: Int ->
                    selectedStartDate = "$i2/${i1 + 1}/$i"
                    start_date.setText(selectedStartDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        currently_work_here.setOnCheckedChangeListener { currently_work_here, isChecked ->
            Toast.makeText(context, "CHECKED", Toast.LENGTH_LONG).show()
            currentlyWorkHere = isChecked
            if (isChecked) {
                end_date.setText("")
                selectedEndDate = ""
                end_date.isEnabled = false
            }
            else {
                end_date.isEnabled = true
            }
        }

        delete_button.setOnClickListener {
            Log.d("EditExperience", "Deleting Experience")
            MaterialDialog(this.context!!).show {
                title(text = "Confirm Delete")
                message(text = "Are you sure to Delete this item?")
                positiveButton(R.string.delete) {
                    profileViewModel!!.removeProfileExperience(experience)
                    findNavController().navigate(R.id.experienceExpandedFragment)
                }
                negativeButton(R.string.cancel_text) {

                }
            }
        }

        save_button.setOnClickListener {
            if (validateExperience()) {
                Log.d("EditExperience", "Editing Experience")
                profileViewModel!!.removeProfileExperience(experience!!)
                val newExperience: ArrayList<Experience> = ArrayList()
                newExperience.add(
                    Experience(
                        title = title.text.toString(),
                        company = company.text.toString(),
                        employmentType = selectedEmployment,
                        location = location.text.toString(),
                        startDate = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate),
                        endDate = if (selectedEndDate.isNotEmpty()) SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate) else null,
                        currentExperience = currentlyWorkHere
                    )
                )
                profileViewModel!!.setProfileExperience(newExperience)
                findNavController().navigate(R.id.experienceExpandedFragment)
            }
        }

    }

    private fun validateExperience(): Boolean {
        if (validation!!.isValidExperience(
                title,
                company,
                selectedEmployment,
                location,
                selectedStartDate,
                selectedEndDate,
                currentlyWorkHere
            )
        ) {
            return true
        } else {
            if (currentlyWorkHere) {
                showError(form_error, title, company, location, start_date)
            }
            else {
                showError(form_error, title, company, location, start_date, end_date)
            }
            return false
        }
    }
}