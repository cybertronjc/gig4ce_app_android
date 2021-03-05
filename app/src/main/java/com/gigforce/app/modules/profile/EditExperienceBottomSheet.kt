package com.gigforce.app.modules.profile

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.core.datamodels.profile.Experience
import com.gigforce.app.utils.DropdownAdapter
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import kotlinx.android.synthetic.main.edit_experience.*
import kotlinx.android.synthetic.main.edit_experience.cancel
import kotlinx.android.synthetic.main.edit_experience.title
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditExperienceBottomSheet : ProfileBaseBottomSheetFragment() {
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
        employments.addAll(
            listOf(
                getString(R.string.full_time),
                getString(R.string.internship),
                getString(R.string.part_time)
            )
        )

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
                employment_type.setText(experience.employmentType, false)
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
            DropdownAdapter(this.requireContext(), employments)
        val employmentSpinner = employment_type
        employmentSpinner.setAdapter(employmentAdapter)

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
                    selectedStartDate = "$i2/${i1 + 1}/$i"
                    start_date.setText(selectedStartDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        currently_work_here.setOnCheckedChangeListener { currently_work_here, isChecked ->
            Toast.makeText(
                context, getString(R.string.checked), Toast.LENGTH_LONG
            ).show()
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
            val dialog = getDeleteConfirmationDialog(requireContext())
            dialog.yes.setOnClickListener {
                profileViewModel.removeProfileExperience(experience)
                findNavController().navigate(R.id.experienceExpandedFragment)
                dialog .dismiss()
            }
            dialog.show()
        }

        save_button.setOnClickListener {
            if (validateExperience()) {
                Log.d("EditExperience", "Editing Experience")
                profileViewModel.removeProfileExperience(experience)
                profileViewModel.setProfileExperience(
                    Experience(
                        title = title.text.toString(),
                        company = company.text.toString(),
                        employmentType = employment_type.text.toString(),
                        location = location.text.toString(),
                        startDate = SimpleDateFormat("dd/MM/yyyy").parse(selectedStartDate),
                        endDate = if (selectedEndDate.isNotEmpty()) SimpleDateFormat("dd/MM/yyyy").parse(selectedEndDate) else null,
                        currentExperience = currentlyWorkHere
                    )
                )
                findNavController().navigate(R.id.experienceExpandedFragment)
            }
        }

        cancel.setOnClickListener {
            findNavController().navigate(R.id.experienceExpandedFragment)
        }

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
                showError(form_error, title, company, location, start_date)
            }
            else {
                showError(form_error, title, company, location, start_date, end_date)
            }
            return false
        }
    }
}