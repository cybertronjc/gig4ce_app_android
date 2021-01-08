package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.intrest_experience

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.*
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.utils.Lce
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_add_experience.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


class AddUserExperienceFragment : BaseFragment() {

    private val interestAndExperienceViewModel: InterestAndExperienceViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String
    private var pincode = ""

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var mode: Int = EnrollmentConstants.MODE_ADD

    //To be used in case of edit
    private var currentInterestName: String? = null
    private val dateFormatter = SimpleDateFormat("MM/yyyy", Locale.getDefault())

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_add_experience, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()

        if (mode == EnrollmentConstants.MODE_EDIT) {
            skip_btn.visible()

            if (currentInterestName == null) {
                // Fresh start of experience edit, fetch first category
                interestAndExperienceViewModel.getInterestDetailsOrFetchFirstOneIfInterestNameIsNull(userId, null)
            } else {
                interestAndExperienceViewModel.getInterestDetailsOrFetchFirstOneIfInterestNameIsNull(userId, currentInterestName)
            }
        } else {
            skip_btn.gone()
            interestAndExperienceViewModel.getPendingInterestExperience(userId)
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            currentInterestName = it.getString(INTENT_EXTRA_CURRENT_INTEREST_NAME)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }

        savedInstanceState?.let {
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            currentInterestName = it.getString(INTENT_EXTRA_CURRENT_INTEREST_NAME)

            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE, pincode)
        outState.putInt(EnrollmentConstants.INTENT_EXTRA_MODE, mode)
        outState.putString(INTENT_EXTRA_CURRENT_INTEREST_NAME, currentInterestName)
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
            showStartDateMonthYearPicker()
        }

        end_date_tv.setOnClickListener {
            // showEndDatePicker()
            showEndDateMonthYearPicker()
        }

        ic_back_iv.setOnClickListener {
            showGoBackConfirmationDialog()
        }

        skip_btn.setOnClickListener {

            if (currentInterestName != null)
                interestAndExperienceViewModel.skipCurrentExperienceAndFetchNextOne(userId, currentInterestName!!)
        }
    }

    private fun validateDataAndSubmit() {

        if (currentInterestName == null) {
            showAlertDialog("Wait", "Please wait interest not loaded yet")
            return
        }

        if (exp_chipgroup.checkedChipId == -1) {
            have_exp_error.visible()
            have_exp_error.text = "Please Select if you have experience or not"
            return
        } else {
            have_exp_error.gone()
            have_exp_error.text = null
        }

        val doHaveAndExp = exp_chipgroup.checkedChipId == R.id.exp_yes

        if (doHaveAndExp) {
            if (role_spinner.isVisible && role_spinner.selectedItemPosition == 0) {

                role_error.visible()
                role_error.text = "Please Select Role"
                return
            } else {
                role_error.gone()
                role_error.text = null
            }


            if (company_name_et.text.isBlank()) {

                company_name_error.visible()
                company_name_error.text = "Please Select Company"
                return
            } else {

                company_name_error.gone()
                company_name_error.text = null
            }

            if (startDate == null) {

                start_date_error.visible()
                start_date_error.text = "Please Select Start Date"
                return
            } else {
                start_date_error.gone()
                start_date_error.text = null
            }

            if (endDate == null && !currently_work_here_checkbox.isChecked) {
                currently_working_error.visible()
                currently_working_error.text = "Please Select end date or Check Currently working here"

                return
            } else {
                currently_working_error.gone()
                currently_working_error.text = null
            }

            if (endDate == null && !currently_work_here_checkbox.isChecked) {
                currently_working_error.visible()
                currently_working_error.text = "Please Select end date or Check Currently working here"

                return
            } else {
                currently_working_error.gone()
                currently_working_error.text = null
            }


            if (total_exp_chipgroup.checkedChipId == -1) {

                total_exp_error.visible()
                total_exp_error.text = "Please Select Experience"

                return
            } else {

                total_exp_error.gone()
                total_exp_error.text = null
            }


            if (helper_exec_question_layout.isVisible && helper_weight_chipgroup.checkedChipId == -1) {

                helper_question_error.visible()
                helper_question_error.text = "Please answer this question"
                return
            } else {

                helper_question_error.gone()
                helper_question_error.text = null
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

        val driverQuestionOwnVehicle: List<String> = if (driver_question_layout.isVisible && driver_own_vehicle_chipgroup.checkedChipIds.isNotEmpty()) {
            val driverOwnedVehicles: MutableList<String> = mutableListOf()

            driver_own_vehicle_chipgroup.checkedChipIds.forEach {
                val vehicle = driver_own_vehicle_chipgroup.findViewById<Chip>(it).text.toString()
                driverOwnedVehicles.add(vehicle)
            }

            driverOwnedVehicles
        } else {
            emptyList()
        }

        val driverQuestionCanDriveVehicles: List<String> = if (driver_question_layout.isVisible && vehicle_can_drive_chipgroup.checkedChipIds.isNotEmpty()) {
            val driverCanDriveVehicles: MutableList<String> = mutableListOf()

            vehicle_can_drive_chipgroup.checkedChipIds.forEach {
                val vehicle = vehicle_can_drive_chipgroup.findViewById<Chip>(it).text.toString()
                driverCanDriveVehicles.add(vehicle)
            }

            driverCanDriveVehicles
        } else {
            emptyList()
        }

        val deliveryExecQuestionOwnVehicles: List<String> = if (delivery_exec_question_layout.isVisible && delivery_exec_own_vehicle_chipgroup.checkedChipIds.isNotEmpty()) {
            val deliveryVehiclesOwn: MutableList<String> = mutableListOf()

            delivery_exec_own_vehicle_chipgroup.checkedChipIds.forEach {
                val vehicle = delivery_exec_own_vehicle_chipgroup.findViewById<Chip>(it).text.toString()
                deliveryVehiclesOwn.add(vehicle)
            }

            deliveryVehiclesOwn
        } else {
            emptyList()
        }

        val helperComfortableLiftingHeavyWeights =
                if (helper_exec_question_layout.isVisible && helper_weight_chipgroup.checkedChipId != -1) {
                    helper_weight_chipgroup.checkedChipId == R.id.option_weight_yes
                } else {
                    false
                }

        val experience = Experience(
                haveExperience = doHaveAndExp,
                title = currentInterestName!!,
                employmentType = "",
                company = company,
                location = "",
                startDate = startDate,
                endDate = if (currently_work_here_checkbox.isChecked) null else endDate,
                currentExperience = currently_work_here_checkbox.isChecked,
                role = role,
                earningPerMonth = earning,
                totalExperence = totalExperience,

                driverQuestionOwnVehicle = "",
                driverQuestionVehiclesOwn = driverQuestionOwnVehicle,
                driverQuestionVehiclesCanDrive = driverQuestionCanDriveVehicles,

                deliveryExecQuestionOwnVehicle = "",
                deliveryQuestionVehiclesOwn = deliveryExecQuestionOwnVehicles,

                helperComfortableLiftingHeavyWeights = helperComfortableLiftingHeavyWeights
        )

        if (mode == EnrollmentConstants.MODE_EDIT) {
            interestAndExperienceViewModel.updateExpAndReturnNewOne(userId, experience)
        } else {
            interestAndExperienceViewModel.saveExpAndReturnNewOne(userId, experience)
        }
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
                    val result = it ?: return@Observer

                    when (result) {
                        Lce.Loading -> {

                        }
                        is Lce.Content -> {
                            val content = result.content ?: return@Observer
                            showExpDetailsOnScreen(content.interestName, content.experience)
                        }
                        is Lce.Error -> {

                        }
                    }

                })

        interestAndExperienceViewModel
                .saveExpAndReturnNextOne
                .observe(
                        viewLifecycleOwner,
                        Observer {
                            when (it) {
                                Lce.Loading -> {
                                }
                                is Lce.Content -> {
                                    showToast("$currentInterestName Experience Submitted")
                                    if (it.content == null) {
                                        //All Exps filled
                                        navigate(
                                                R.id.addUserCurrentAddressFragment, bundleOf(
                                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                                                EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                                                EnrollmentConstants.INTENT_EXTRA_MODE to mode
                                        )
                                        )
                                    } else {
                                        navigate(
                                                R.id.addUserExperienceFragment, bundleOf(
                                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                                                EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                                                INTENT_EXTRA_CURRENT_INTEREST_NAME to it.content,
                                                EnrollmentConstants.INTENT_EXTRA_MODE to mode
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

    private fun showExpDetailsOnScreen(content: String?, experienceData: Experience?) {
        currentInterestName = content
        do_you_have_exp_label.text = buildSpannedString {
            append("Do you have experience in")
            color(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)) {
                append(" $content ?")
            }
        }

        experienceData?.let {
            if (it.haveExperience) {
                exp_chipgroup.check(R.id.exp_yes)
            } else {
                exp_chipgroup.check(R.id.exp_no)
            }

            if (it.haveExperience) {
                role_spinner.selectItemWithText(it.role)
                company_name_et.setText(it.company)

                startDate = it.startDate
                endDate = it.endDate

                if (startDate != null)
                    start_date_tv.setText(dateFormatter.format(it.startDate))

                if (endDate != null)
                    end_date_tv.setText(dateFormatter.format(it.endDate))

                currently_work_here_checkbox.isChecked = it.currentExperience
                earning_et.setText(it.earningPerMonth.toString())
                total_exp_chipgroup.selectChipWithText(it.totalExperence)
            }
        }

        //Driving Data

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

            experienceData?.let {
                if (it.title == "Driving") {

                    if (it.haveExperience) {
                        role_spinner.selectItemWithText(it.role)
                    }

                    if (it.driverQuestionOwnVehicle.isNotBlank()) {

                        val driverOwnVehicleList = it.driverQuestionVehiclesOwn.toMutableList()
                        driverOwnVehicleList.add(it.driverQuestionOwnVehicle)
                        driver_own_vehicle_chipgroup.selectChipsWithText(driverOwnVehicleList)

                        vehicle_can_drive_chipgroup.selectChipsWithText(it.driverQuestionVehiclesCanDrive)
                    } else {

                        driver_own_vehicle_chipgroup.selectChipsWithText(it.driverQuestionVehiclesOwn)
                        vehicle_can_drive_chipgroup.selectChipsWithText(it.driverQuestionVehiclesCanDrive)
                    }
                }
            }
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

            experienceData?.let {

                if (it.title == "Delivery Executive") {

                    if (it.haveExperience) {
                        role_spinner.selectItemWithText(it.role)
                    }

                    val deliveryExecOwnVehicleList = it.deliveryQuestionVehiclesOwn.toMutableList()
                    deliveryExecOwnVehicleList.add(it.deliveryExecQuestionOwnVehicle)
                    delivery_exec_own_vehicle_chipgroup.selectChipsWithText(deliveryExecOwnVehicleList)
                }
            }
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

            experienceData?.let {

                if (it.title == "Warehouse Helper") {

                    if (it.haveExperience) {
                        role_spinner.selectItemWithText(it.role)
                    }

                    if (it.helperComfortableLiftingHeavyWeights)
                        helper_weight_chipgroup.check(R.id.option_weight_yes)
                    else
                        helper_weight_chipgroup.check(R.id.option_weight_no)
                }
            }
        } else {
            driver_question_layout.gone()
            delivery_exec_question_layout.gone()
            helper_exec_question_layout.gone()
            role_spinner.gone()
            what_was_your_role_label.gone()
        }
    }

    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Are you sure you want to go back")
                .setPositiveButton("Yes") { _, _ -> goBackToUsersList() }
                .setNegativeButton("No") { _, _ -> }
                .show()
    }

    private fun goBackToUsersList() {
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    companion object {
        const val INTENT_EXTRA_CURRENT_INTEREST_NAME = "currentInterestName"
    }

    private fun showStartDateMonthYearPicker() {
        val selectedDate: Pair<Int, Int> =
                if (startDate != null) {
                    val startDateLocalDate = startDate!!.toLocalDate()
                    Pair(startDateLocalDate.monthValue, startDateLocalDate.year)
                } else {
                    val currentDate = LocalDate.now()
                    Pair(currentDate.monthValue, currentDate.year)
                }

        if (endDate != null) {

            val localDate = LocalDateTime.of(1980, 1, 1, 0, 0)
            val zdt: ZonedDateTime = ZonedDateTime.of(localDate, ZoneId.systemDefault())
            val minDate: Long = zdt.toInstant().toEpochMilli()
            val maxDate = endDate!!.time

            Log.d("AddExp","Start End Date not null")
            Log.d("AddExp","Start Selected Month : ${selectedDate.first}")
            Log.d("AddExp","Start Selected Year : ${selectedDate.second}")
            Log.d("AddExp","Start Min: $minDate")
            Log.d("AddExp","Start Max : $maxDate")

            MonthYearPickerDialogFragment.getInstance(
                    selectedDate.first - 1,
                    selectedDate.second,
                    minDate,
                    maxDate
            ).apply {
                setOnDateSetListener { year, monthOfYear ->

                    Log.d("AddExp","Start Values Set Month : $monthOfYear")
                    Log.d("AddExp","Start Values Set Year : $year")

                    val newCal = Calendar.getInstance()
                    newCal.set(Calendar.YEAR, year)
                    newCal.set(Calendar.MONTH, monthOfYear)
                    newCal.set(Calendar.DAY_OF_MONTH, 1)
                    newCal.set(Calendar.HOUR_OF_DAY, 0)
                    newCal.set(Calendar.MINUTE, 0)
                    newCal.set(Calendar.SECOND, 0)
                    newCal.set(Calendar.MILLISECOND, 0)

                    startDate = newCal.time
                    this@AddUserExperienceFragment.start_date_tv.setText(dateFormatter.format(newCal.time))
                }
            }.show(childFragmentManager, "MonthYearPickerDialogFragment")
        } else {

            val localDate = LocalDateTime.of(1980, 1, 1, 0, 0)
            val zdt: ZonedDateTime = ZonedDateTime.of(localDate, ZoneId.systemDefault())
            val minDate: Long = zdt.toInstant().toEpochMilli()
            val maxDate = Date().time

            Log.d("AddExp","Start End Date == null")
            Log.d("AddExp","Start Selected Month : ${selectedDate.first}")
            Log.d("AddExp","Start Selected Year : ${selectedDate.second}")
            Log.d("AddExp","Start Min: $minDate")
            Log.d("AddExp","Start Max : $maxDate")

            MonthYearPickerDialogFragment.getInstance(
                    selectedDate.first -1,
                    selectedDate.second,
                    minDate,
                    maxDate
            ).apply {
                setOnDateSetListener { year, monthOfYear ->

                    Log.d("AddExp","Start Values Set Month : $monthOfYear")
                    Log.d("AddExp","Start Values Set Year : $year")


                    val newCal = Calendar.getInstance()
                    newCal.set(Calendar.YEAR, year)
                    newCal.set(Calendar.MONTH, monthOfYear)
                    newCal.set(Calendar.DAY_OF_MONTH, 1)
                    newCal.set(Calendar.HOUR_OF_DAY, 0)
                    newCal.set(Calendar.MINUTE, 0)
                    newCal.set(Calendar.SECOND, 0)
                    newCal.set(Calendar.MILLISECOND, 0)

                    startDate = newCal.time
                    this@AddUserExperienceFragment.start_date_tv.setText(dateFormatter.format(newCal.time))
                }
            }.show(childFragmentManager, "MonthYearPickerDialogFragment")

        }
    }


    private fun showEndDateMonthYearPicker() {
        val selectedDate: Pair<Int, Int> =
                if (endDate != null) {
                    val endDateLocalDate = endDate!!.toLocalDate()
                    Pair(endDateLocalDate.monthValue, endDateLocalDate.year)
                } else {
                    val currentDate = LocalDate.now()
                    Pair(currentDate.monthValue, currentDate.year)
                }

        val maxDate = Date().time

        val localDate = LocalDateTime.of(1980, 1, 1, 0, 0)
        val zdt: ZonedDateTime = ZonedDateTime.of(localDate, ZoneId.systemDefault())
        val defaultMinTime: Long = zdt.toInstant().toEpochMilli()

        val finalMinDate = startDate?.time ?: defaultMinTime

        Log.d("AddExp","End Selected Month : ${selectedDate.first}")
        Log.d("AddExp","End Selected Year : ${selectedDate.second}")
        Log.d("AddExp","End Min: $finalMinDate")
        Log.d("AddExp","End Max : $maxDate")

        MonthYearPickerDialogFragment.getInstance(
                selectedDate.first -1 ,
                selectedDate.second,
                finalMinDate,
                maxDate
        ).apply {
            setOnDateSetListener { year, monthOfYear ->

                Log.d("AddExp","End Values Set Month : $monthOfYear")
                Log.d("AddExp","End Values Set Year : $year")

                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, monthOfYear)
                newCal.set(Calendar.DAY_OF_MONTH, 1)
                newCal.set(Calendar.HOUR_OF_DAY, 0)
                newCal.set(Calendar.MINUTE, 0)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)

                endDate = newCal.time
                this@AddUserExperienceFragment.end_date_tv.setText(dateFormatter.format(newCal.time))
            }
        }.show(childFragmentManager, "MonthYearPickerDialogFragment")
    }
}