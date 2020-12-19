package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.intrest_experience

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.utils.Lce
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_add_experience.*
import java.text.SimpleDateFormat
import java.util.*

class AddUserExperienceFragment : BaseFragment() {

    private val interestAndExperienceViewModel: InterestAndExperienceViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var currentExperienceTitle: String? = null

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val startDatePicker: DatePickerDialog by lazy {

        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                newCal.set(Calendar.HOUR_OF_DAY, 0)
                newCal.set(Calendar.MINUTE, 0)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)

                startDate = newCal.time
                start_date_tv.setText(dateFormatter.format(newCal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    private val endDatePicker: DatePickerDialog by lazy {

        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->

                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                newCal.set(Calendar.HOUR_OF_DAY, 0)
                newCal.set(Calendar.MINUTE, 0)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)

                endDate = newCal.time
                currently_work_here_checkbox.isChecked = false
                end_date_tv.setText(dateFormatter.format(newCal.time))

            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_add_experience, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
    }

    private fun initListeners() {

        exp_chipgroup.setOnCheckedChangeListener { group, checkedId ->
            exp_yes_layout.isVisible = checkedId == R.id.exp_yes
        }

        currently_work_here_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                end_date_tv.text = "Current"
            } else {
                if (endDate != null) {
                    end_date_tv.setText(dateFormatter.format(endDate!!.time))
                } else
                    end_date_tv.setText(null)
            }
        }

        submitBtn.setOnClickListener {
            validateDataAndSubmit()
        }

        start_date_tv.setOnClickListener {
            showStartDatePicker()
        }

        end_date_tv.setOnClickListener {
            showEndDatePicker()
        }
    }

    private fun showStartDatePicker() {
        if (endDate != null) {
            startDatePicker.datePicker.maxDate = endDate!!.time
        }

        startDatePicker.show()
    }

    private fun showEndDatePicker() {
        if (startDate != null) {
            endDatePicker.datePicker.minDate = startDate!!.time
        }

        endDatePicker.show()
    }

    private fun validateDataAndSubmit() {

        if (currentExperienceTitle == null) {
            showAlertDialog("Wait", "Please wait intrest not loaded yet")
            return
        }

        if (exp_chipgroup.checkedChipId == -1) {
            showAlertDialog("Select Experience", "Please Select if you have experience or not")
            return
        }

        val doHaveAndExp = exp_chipgroup.checkedChipId == R.id.exp_yes

        if (doHaveAndExp) {
            if (role_spinner.isVisible && role_spinner.selectedItemPosition == 0) {
                showAlertDialog("Select Role", "Please Select Role")
                return
            }

            if (company_name_et.text.isBlank()) {
                showAlertDialog("Select Company", "Please Select Company")
                return
            }

            if (total_exp_chipgroup.checkedChipId == -1) {
                showAlertDialog("Select Experience", "Please Select Experience")
                return
            }

            if (startDate == null) {
                showAlertDialog("Select Start Date", "Please Select Start Date")
                return
            }

            if (endDate == null && !currently_work_here_checkbox.isChecked) {
                showAlertDialog(
                    "Select End date",
                    "Please Select end date or Check Currently working here"
                )
                return
            }

            if (helper_exec_question_layout.isVisible && helper_weight_chipgroup.checkedChipId == -1) {
                showAlertDialog(
                    "Select Weight question answer",
                    "Please Select answer to last question"
                )
                return
            }

        }

        val company = if (doHaveAndExp) {
            company_name_et.text.toString()
        } else {
            ""
        }

        val role: String = if (doHaveAndExp && role_spinner.isVisible) {
            role_spinner.selectedItem.toString()
        } else {
            ""
        }

        val earning: Double = if (doHaveAndExp && earning_et.text.isNotBlank()) {
            earning_et.text.toString().toDouble()
        } else {
            0.0
        }
        val totalExperience = if (doHaveAndExp && total_exp_chipgroup.checkedChipId != -1) {
            total_exp_chipgroup.findViewById<Chip>(total_exp_chipgroup.checkedChipId).text.toString()
        } else {
            ""
        }

        val driverQuestionOwnVehicle =
            if (driver_question_layout.isVisible && driver_own_vehicle_chipgroup.checkedChipId != -1) {
                driver_own_vehicle_chipgroup.findViewById<Chip>(driver_own_vehicle_chipgroup.checkedChipId).text.toString()
            } else {
                ""
            }

        val deliveryExecQuestionOwnVehicle =
            if (delivery_exec_question_layout.isVisible && delivery_exec_own_vehicle_chipgroup.checkedChipId != -1) {
                delivery_exec_own_vehicle_chipgroup.findViewById<Chip>(
                    delivery_exec_own_vehicle_chipgroup.checkedChipId
                ).text.toString()
            } else {
                ""
            }

        val helperComfortableLiftingHeavyWeights =
            if (helper_exec_question_layout.isVisible && helper_weight_chipgroup.checkedChipId != -1) {
                helper_weight_chipgroup.checkedChipId == R.id.option_weight_yes
            } else {
                false
            }

        val experience = Experience(
            haveExperience = doHaveAndExp,
            title = currentExperienceTitle!!,
            employmentType = "",
            company = company,
            location = "",
            startDate = startDate,
            endDate = if (currently_work_here_checkbox.isChecked) null else endDate,
            currentExperience = currently_work_here_checkbox.isChecked,
            role = role,
            earningPerMonth = earning,
            totalExperence = totalExperience,
            driverQuestionOwnVehicle = driverQuestionOwnVehicle,
            deliveryExecQuestionOwnVehicle = deliveryExecQuestionOwnVehicle,
            helperComfortableLiftingHeavyWeights = helperComfortableLiftingHeavyWeights
        )

        interestAndExperienceViewModel.saveExpAndReturnNewOne(userId, experience)
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        interestAndExperienceViewModel
            .experience
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lce.Loading -> {

                    }
                    is Lce.Content -> {
                        showExpDetailsOnScreen(it.content)
                    }
                    is Lce.Error -> {

                    }
                }

            })
        interestAndExperienceViewModel.getPendingInterestExperience(userId)

        interestAndExperienceViewModel
            .saveExpAndReturnNextOne
            .observe(
                viewLifecycleOwner,
                Observer {
                    when (it) {
                        Lce.Loading -> {
                        }
                        is Lce.Content -> {
                            showToast("$currentExperienceTitle Experience Submitted")
                            if (it.content == null) {
                                //All Exps filled
                                navigate(
                                    R.id.addCurrentAddressFragment, bundleOf(
                                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                        EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
                                    )
                                )
                            } else {
                                navigate(
                                    R.id.addUserExperienceFragment, bundleOf(
                                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                        EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
                                    )
                                )
                            }
                        }
                        is Lce.Error -> {
                        }
                    }
                })


    }

    private fun populateRoleSpinner(items: MutableList<String>) {
        items.add(0, "Select Role")
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        role_spinner.adapter = adapter
    }

    private fun showExpDetailsOnScreen(content: String?) {
        currentExperienceTitle = content
        do_you_have_exp_label.text = buildSpannedString {
            append("Do you have experience in")
            color(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)) {
                append(" $content ?")
            }
        }

        if (content == "Driving") {
            driver_question_layout.visible()
            delivery_exec_question_layout.gone()
            helper_exec_question_layout.gone()
            role_spinner.visible()
            what_was_your_role_label.visible()

            populateRoleSpinner(
                mutableListOf(
                    "Car", "Truck", "Bus", "Autorickshaw"
                )
            )
        } else if (content == "Delivery Executive") {
            driver_question_layout.gone()
            delivery_exec_question_layout.visible()
            helper_exec_question_layout.gone()
            role_spinner.visible()
            what_was_your_role_label.visible()

            populateRoleSpinner(
                mutableListOf(
                    "Food Parcel", "Luggage parcel", "Documents parcel"
                )
            )
        } else if (content == "Warehouse Helper") {
            driver_question_layout.gone()
            delivery_exec_question_layout.gone()
            helper_exec_question_layout.visible()
            role_spinner.visible()
            what_was_your_role_label.visible()

            populateRoleSpinner(
                mutableListOf(
                    "Warehouse Helper", "Cleaner"
                )
            )
        } else {
            driver_question_layout.gone()
            delivery_exec_question_layout.gone()
            helper_exec_question_layout.gone()
            role_spinner.gone()
            what_was_your_role_label.gone()
        }
    }

    companion object {

    }
}