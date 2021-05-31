package com.gigforce.ambassador.user_rollment.intrest_experience

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.ambassador.EnrollmentConstants
import com.gigforce.ambassador.R
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.datamodels.profile.Experience
import com.gigforce.core.extensions.*
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ambsd_add_driving_license_info.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_experience.*
import kotlinx.android.synthetic.main.fragment_ambsd_add_experience.skip_btn
import kotlinx.android.synthetic.main.fragment_ambsd_add_experience.submitBtn
import kotlinx.android.synthetic.main.fragment_ambsd_add_experience.toolbar_layout
import kotlinx.android.synthetic.main.fragment_gig_page_2_details.*
import kotlinx.android.synthetic.main.fragment_user_current_address_main.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class AddUserExperienceFragment : Fragment(), IOnBackPressedOverride {

    private val interestAndExperienceViewModel: InterestAndExperienceViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String
    private var pincode = ""
    private var mode: Int = EnrollmentConstants.MODE_ADD

    //To be used in case of edit
    private var currentInterestName: String? = null
    private var vechiclesOwn: Array<String> = emptyArray()
    private val dateFormatter = SimpleDateFormat("MM/yyyy", Locale.getDefault())

    @Inject lateinit var navigation : INavigation
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_ambsd_add_experience, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        setYearsAndStateData()
        initListeners()
        initViewModel()

        if (mode == EnrollmentConstants.MODE_EDIT) {
            skip_btn.visible()

            if (currentInterestName == null) {
                // Fresh start of experience edit, fetch first category
                interestAndExperienceViewModel.getInterestDetailsOrFetchFirstOneIfInterestNameIsNull(
                        userId,
                        null
                )
            } else {
                interestAndExperienceViewModel.getInterestDetailsOrFetchFirstOneIfInterestNameIsNull(
                        userId,
                        currentInterestName
                )
            }
        } else {
            skip_btn.gone()
            interestAndExperienceViewModel.getPendingInterestExperience(userId)
        }
    }

    private fun setYearsAndStateData() {

        val yearsArray = resources.getStringArray(R.array.years_values)
        val yearsSpinnerAdapter: ArrayAdapter<String> =
                ArrayAdapter(requireContext(), R.layout.layout_spinner_item, yearsArray)
        year_exp_spinner.adapter = yearsSpinnerAdapter

        val monthsArray = resources.getStringArray(R.array.months_values)
        val monthsAdapter: ArrayAdapter<String> =
                ArrayAdapter(requireContext(), R.layout.layout_spinner_item, monthsArray)
        months_exp_spinner.adapter = monthsAdapter
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            currentInterestName = it.getString(INTENT_EXTRA_CURRENT_INTEREST_NAME)
            vechiclesOwn = it.getStringArray(INTENT_EXTRA_VEHICLES_CAN_DRIVE) ?: emptyArray()
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }

        savedInstanceState?.let {
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            currentInterestName = it.getString(INTENT_EXTRA_CURRENT_INTEREST_NAME)
            vechiclesOwn = it.getStringArray(INTENT_EXTRA_VEHICLES_CAN_DRIVE) ?: emptyArray()

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
        outState.putStringArray(INTENT_EXTRA_VEHICLES_CAN_DRIVE, vechiclesOwn)
    }

    private fun initListeners() {

        exp_chipgroup.setOnCheckedChangeListener { group, checkedId ->
            exp_yes_layout.isVisible = checkedId == R.id.exp_yes
        }

        company_name_et.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->

            try {

                if (hasFocus) {
                    experience_root_scroll_view.post {
                        experience_root_scroll_view.smoothScrollBy(
                                0,
                                currently_work_here_checkbox.y.toInt()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        currently_work_here_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->

//            if (isChecked) {
//                end_date_tv.text = getString(R.string.current)
//            } else {
//                if (endDate != null) {
//                    end_date_tv.setText(dateFormatter.format(endDate!!.time))
//                } else
//                    end_date_tv.setText(null)
//            }
        }

        submitBtn.setOnClickListener {
            validateDataAndSubmit()
        }

//        start_date_tv.setOnClickListener {
//            showStartDateMonthYearPicker()
//        }
//
//        end_date_tv.setOnClickListener {
//            // showEndDatePicker()
//            showEndDateMonthYearPicker()
//        }


        toolbar_layout.apply {
            showTitle(getString(R.string.add_experience))
            hideActionMenu()
            setBackButtonListener(View.OnClickListener { showGoBackConfirmationDialog() })
//            setBackButtonListener {
//                showGoBackConfirmationDialog()
//            }
        }

        skip_btn.setOnClickListener {

            if (currentInterestName != null)
                interestAndExperienceViewModel.skipCurrentExperienceAndFetchNextOne(
                        userId,
                        currentInterestName!!
                )
        }
    }

    private fun validateDataAndSubmit() {

        if (currentInterestName == null) {
            showAlertDialog(getString(R.string.wait), getString(R.string.interest_not_loaded_yet))
            return
        }

        if (exp_chipgroup.checkedChipId == -1) {
            have_exp_error.visible()
            have_exp_error.text = getString(R.string.select_experience_or_not)
            return
        } else {
            have_exp_error.gone()
            have_exp_error.text = null
        }

        val doHaveAndExp = exp_chipgroup.checkedChipId == R.id.exp_yes

        if (doHaveAndExp) {
            if (role_chipgroup.isVisible &&
                    role_chipgroup.childCount != 0 &&
                    role_chipgroup.checkedChipIds.isEmpty()
            ) {

                role_error.visible()
                role_error.text = getString(R.string.please_select_role)

                return
            } else {
                role_error.gone()
                role_error.text = null
            }


            if (company_name_et.text.isBlank()) {

                company_name_error.visible()
                company_name_error.text = getString(R.string.please_select_company)

                return
            } else {

                company_name_error.gone()
                company_name_error.text = null
            }

            if (year_exp_spinner.selectedItemPosition == 0) {
                experience_error.text = "Please Select Experience Years"
            } else if (months_exp_spinner.selectedItemPosition == 0) {
                experience_error.text = "Please Select Experience Months"
            } else {
                experience_error.text = null
            }
        }

        val company = if (doHaveAndExp) {
            company_name_et.text.toString()
        } else {
            ""
        }

        val role: String = if (doHaveAndExp && role_chipgroup.checkedChipId != -1) {
            role_chipgroup.findViewById<Chip>(role_chipgroup.checkedChipId).text.toString()
        } else {
            ""
        }

        val experienceInMonths =
                ((year_exp_spinner.selectedItemPosition - 1) * 12) + (months_exp_spinner.selectedItemPosition - 1)

        val driverQuestionOwnVehicle: List<String> =
                if (driver_question_layout.isVisible && driver_own_vehicle_chipgroup.checkedChipIds.isNotEmpty()) {
                    val driverOwnedVehicles: MutableList<String> = mutableListOf()

                    driver_own_vehicle_chipgroup.checkedChipIds.forEach {
                        val vehicle =
                                driver_own_vehicle_chipgroup.findViewById<Chip>(it).text.toString()
                        driverOwnedVehicles.add(vehicle)
                    }

                    driverOwnedVehicles
                } else {
                    emptyList()
                }


        val deliveryExecQuestionOwnVehicles: List<String> =
                if (delivery_exec_question_layout.isVisible && delivery_exec_own_vehicle_chipgroup.checkedChipIds.isNotEmpty()) {
                    val deliveryVehiclesOwn: MutableList<String> = mutableListOf()

                    delivery_exec_own_vehicle_chipgroup.checkedChipIds.forEach {
                        val vehicle =
                                delivery_exec_own_vehicle_chipgroup.findViewById<Chip>(it).text.toString()
                        deliveryVehiclesOwn.add(vehicle)
                    }

                    deliveryVehiclesOwn
                } else {
                    emptyList()
                }


        val experience = Experience(
                haveExperience = doHaveAndExp,
                title = currentInterestName!!,
                employmentType = "",
                company = company,
                location = "",
                currentExperience = currently_work_here_checkbox.isChecked,
                role = role,

                driverQuestionOwnVehicle = "",
                driverQuestionVehiclesOwn = driverQuestionOwnVehicle,

                deliveryExecQuestionOwnVehicle = "",
                deliveryQuestionVehiclesOwn = deliveryExecQuestionOwnVehicles,
                experienceInMonths = experienceInMonths
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
                .setPositiveButton(getString(R.string.okay).capitalize()) { _, _ -> }
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
                            showExpDetailsOnScreen(
                                    content.interestName,
                                    content.experience,
                                    content.roles
                            )
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
                            it ?: return@Observer


                            when (it) {
                                Lce.Loading -> {
                                }
                                is Lce.Content -> {
                                    showToast(currentInterestName.toString() + " " + getString(R.string.experience_submitted))

                                    if (it.content == null) {
                                        //All Exps filled
                                        navigation.navigateTo("userinfo/addUserCurrentAddressFragment",bundleOf(
                                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                                                EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                                                EnrollmentConstants.INTENT_EXTRA_MODE to mode
                                        ))
//                                navigate(
//                                    R.id.addUserCurrentAddressFragment, bundleOf(
//                                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                                        EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
//                                        EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
//                                        EnrollmentConstants.INTENT_EXTRA_MODE to mode
//                                    )
//                                )
                                    } else {

                                        val driverQuestionOwnVehicle: List<String> =
                                                if (driver_question_layout.isVisible && driver_own_vehicle_chipgroup.checkedChipIds.isNotEmpty()) {
                                                    val driverOwnedVehicles: MutableList<String> =
                                                            mutableListOf()

                                                    driver_own_vehicle_chipgroup.checkedChipIds.forEach {
                                                        val vehicle =
                                                                driver_own_vehicle_chipgroup.findViewById<Chip>(it).text.toString()
                                                        driverOwnedVehicles.add(vehicle)
                                                    }

                                                    driverOwnedVehicles
                                                } else {
                                                    emptyList()
                                                }


//                                val deliveryExecQuestionOwnVehicles: List<String> =
//                                        if (delivery_exec_question_layout.isVisible && delivery_exec_own_vehicle_chipgroup.checkedChipIds.isNotEmpty()) {
//                                            val deliveryVehiclesOwn: MutableList<String> = mutableListOf()
//
//                                            delivery_exec_own_vehicle_chipgroup.checkedChipIds.forEach {
//                                                val vehicle =
//                                                        delivery_exec_own_vehicle_chipgroup.findViewById<Chip>(it).text.toString()
//                                                deliveryVehiclesOwn.add(vehicle)
//                                            }
//
//                                            deliveryVehiclesOwn
//                                        } else {
//                                            emptyList()
//                                        }
                                        navigation.navigateTo("userinfo/addUserExperienceFragment",bundleOf(
                                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                                                EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                                                INTENT_EXTRA_CURRENT_INTEREST_NAME to it.content,
                                                EnrollmentConstants.INTENT_EXTRA_MODE to mode,
                                                INTENT_EXTRA_VEHICLES_CAN_DRIVE to driverQuestionOwnVehicle.toTypedArray()
                                        ))
//                                navigate(
//                                    R.id.addUserExperienceFragment, bundleOf(
//                                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                                        EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
//                                        EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
//                                        INTENT_EXTRA_CURRENT_INTEREST_NAME to it.content,
//                                        EnrollmentConstants.INTENT_EXTRA_MODE to mode,
//                                        INTENT_EXTRA_VEHICLES_CAN_DRIVE to driverQuestionOwnVehicle.toTypedArray()
//                                    )
//                                )
                                    }
                                }
                                is Lce.Error -> {
                                }
                            }
                        })


    }

    private fun populateRoleSpinner(items: List<String>) {
        if(items.isEmpty()){
            role_chipgroup.gone()
            what_was_your_role_label.gone()
        } else {
            role_chipgroup.visible()
            what_was_your_role_label.visible()

            role_chipgroup.removeAllViews()
            items.forEach {

                var chip: Chip
                chip = layoutInflater.inflate(
                        R.layout.fragment_ambassador_role_chip,
                        gig_chip_group,
                        false
                ) as Chip
                chip.text = it
                chip.id = ViewCompat.generateViewId()
                role_chipgroup.addView(chip)
            }

            role_chipgroup.isSingleSelection = true
        }
    }


    private fun showExpDetailsOnScreen(
            content: String?,
            experienceData: Experience?,
            roles : List<String>
    ) {
        currentInterestName = content
        do_you_have_exp_label.text = buildSpannedString {
            append(getString(R.string.do_u_have_experience_in))
            color(ResourcesCompat.getColor(resources, R.color.colorPrimary, null)) {
                append(" $content ?")
            }
        }

        if (content == "Delivery Executive") {
            delivery_exec_own_vehicle_chipgroup.selectChipsWithText(vechiclesOwn.toList())
        }

        experienceData?.let {
            if (it.haveExperience) {
                exp_chipgroup.check(R.id.exp_yes)
            } else {
                exp_chipgroup.check(R.id.exp_no)
            }

            if (it.haveExperience) {
                role_chipgroup.selectChipWithText(it.role)
                company_name_et.setText(it.company)

                if (it.startDate != null) {

                    val monthsDiff = if (it.endDate != null) {
                        ChronoUnit.MONTHS.between(
                                it.startDate!!.toLocalDate(),
                                it.endDate!!.toLocalDate()
                        )
                    } else {
                        ChronoUnit.MONTHS.between(
                                it.startDate!!.toLocalDate(),
                                LocalDate.now()
                        )
                    }

                    if (monthsDiff > 12) {
                        val years = monthsDiff / 12
                        year_exp_spinner.setSelection((years + 1).toInt())

                        val months = monthsDiff % 12
                        months_exp_spinner.setSelection((months + 1).toInt())
                    } else {
                        year_exp_spinner.setSelection(1) // 0 Years
                        months_exp_spinner.setSelection((monthsDiff + 1).toInt())
                    }
                } else if (it.experienceInMonths != 0) {

                    if (it.experienceInMonths > 12) {
                        val years = it.experienceInMonths / 12
                        year_exp_spinner.setSelection((years + 1).toInt())

                        val months = it.experienceInMonths % 12
                        months_exp_spinner.setSelection((months + 1).toInt())
                    } else {
                        year_exp_spinner.setSelection(1) // 0 Years
                        months_exp_spinner.setSelection((it.experienceInMonths + 1).toInt())
                    }
                }

                currently_work_here_checkbox.isChecked = it.currentExperience
            }
        }

        //Driving Data

        if (content == "Driving") {
            driver_question_layout.visible()
            delivery_exec_question_layout.gone()
            role_chipgroup.visible()
            what_was_your_role_label.visible()

            populateRoleSpinner(roles)

            experienceData?.let {
                if (it.title == "Driving") {

                    if (it.haveExperience) {
                        role_chipgroup.selectChipWithText(it.role)
                    }

                    if (it.driverQuestionOwnVehicle.isNotBlank()) {

                        val driverOwnVehicleList = it.driverQuestionVehiclesOwn.toMutableList()
                        driverOwnVehicleList.add(it.driverQuestionOwnVehicle)
                        driver_own_vehicle_chipgroup.selectChipsWithText(driverOwnVehicleList)

                    } else {

                        driver_own_vehicle_chipgroup.selectChipsWithText(it.driverQuestionVehiclesOwn)
                    }
                }
            }
        } else if (content == "Delivery Executive") {
            driver_question_layout.gone()
            delivery_exec_question_layout.visible()
            role_chipgroup.visible()
            what_was_your_role_label.visible()

            populateRoleSpinner(roles)
            experienceData?.let {

                if (it.title == "Delivery Executive") {

                    if (it.haveExperience) {
                        role_chipgroup.selectChipWithText(it.role)
                    }

                    val deliveryExecOwnVehicleList = it.deliveryQuestionVehiclesOwn.toMutableList()
                    deliveryExecOwnVehicleList.add(it.deliveryExecQuestionOwnVehicle)
                    delivery_exec_own_vehicle_chipgroup.selectChipsWithText(
                            deliveryExecOwnVehicleList
                    )
                }
            }
        } else if (content == "Warehouse Helper") {
            driver_question_layout.gone()
            delivery_exec_question_layout.gone()
            role_chipgroup.visible()
            what_was_your_role_label.visible()

            populateRoleSpinner(roles)

            experienceData?.let {

                if (it.title == "Warehouse Helper") {

                    if (it.haveExperience) {
                        role_chipgroup.selectChipWithText(it.role)
                    }
                }
            }
        } else if (content == "Sales") {
            driver_question_layout.gone()
            delivery_exec_question_layout.gone()
            role_chipgroup.visible()
            what_was_your_role_label.visible()

            populateRoleSpinner(roles)

            experienceData?.let {

                if (it.title == "Sales") {

                    if (it.haveExperience) {
                        role_chipgroup.selectChipWithText(it.role)
                    }
                }
            }
        } else {
            driver_question_layout.gone()
            delivery_exec_question_layout.gone()
            role_chipgroup.gone()
            what_was_your_role_label.gone()
        }

    }

    override fun onBackPressed(): Boolean {
        showGoBackConfirmationDialog()
        return true
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.alert))
                .setMessage(getString(R.string.are_u_sure_u_want_to_go_back))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> goBackToUsersList() }
                .setNegativeButton(getString(R.string.no)) { _, _ -> }
                .show()
    }

    private fun goBackToUsersList() {

        findNavController().navigateUp()
//        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }

    companion object {
        const val INTENT_EXTRA_CURRENT_INTEREST_NAME = "currentInterestName"
        const val INTENT_EXTRA_VEHICLES_CAN_DRIVE = "vechiles_can_drive"
    }

}