package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.selectChipWithText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.auth.ui.main.LoginSuccessfulFragment
import com.gigforce.app.modules.gigPage.GigPageFragment
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_ambsd_user_details.*
import kotlinx.android.synthetic.main.fragment_ambsd_user_details_main.*
import java.text.SimpleDateFormat
import java.util.*

class AddUserDetailsFragment : BaseFragment() {

    private val viewModel: UserDetailsViewModel by viewModels()

    private lateinit var userId: String
    private lateinit var phoneNumber: String
    private var dateOfBirth: Date? = null
    private var mode: Int = EnrollmentConstants.MODE_ADD

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val dateOfBirthPicker: DatePickerDialog by lazy {

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

                    dateOfBirth = newCal.time
                    date_of_birth_et.text = dateFormatter.format(newCal.time)

                    dob_okay_iv.visible()
                    dob_error_tv.gone()
                    dob_error_tv.text = null
                },
                1995,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_user_details, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
        getProfileDetailsForUser()
    }

    private fun getProfileDetailsForUser() {

        if (mode == EnrollmentConstants.MODE_EDIT) {
            viewModel.getProfileForUser(userId)
        } else {
            showUserDetailsMainLayout(showEditActions = false)
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            phoneNumber = it.getString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
        }

        savedInstanceState?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            phoneNumber = it.getString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER, phoneNumber)
        outState.putInt(EnrollmentConstants.INTENT_EXTRA_MODE, mode)
    }

    private fun initListeners() {
        user_name_et.textChanged {
            user_name_okay_iv.isVisible = it.length > 2

            if (it.length > 2) {
                full_name_error_tv.gone()
                full_name_error_tv.text = null
            }
        }

        date_of_birth_et.setOnClickListener {

            dateOfBirthPicker.datePicker.maxDate = Date().time
            dateOfBirthPicker.show()
        }

        pin_code_et.textChanged {
            pin_okay_iv.isVisible = it.length == 6 && it.toString().toInt() > 10_00_00

            if (it.isNotBlank()) {
                pincode_error_tv.isVisible = it.length != 6 || it.toString().toInt() < 10_00_00
                pincode_error_tv.text = "Please fill Pincode"
            } else {
                pincode_error_tv.gone()
                pincode_error_tv.text = null
            }
        }

        submitBtn.setOnClickListener {
            validateDataAndsubmit()
        }

        ic_back_iv.setOnClickListener {
            showGoBackConfirmationDialog()
        }

        skip_btn.setOnClickListener {
            navigate(
                    R.id.addProfilePictureFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to user_name_et.text.toString(),
                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pin_code_et.text.toString(),
                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
            )
            )
        }

        gender_chip_group.setOnCheckedChangeListener { group, checkedId ->

            if (checkedId != View.NO_ID) {
                gender_error_tv.gone()
                gender_error_tv.text = null
            }
        }

        highest_qual_chipgroup.setOnCheckedChangeListener { group, checkedId ->

            if (checkedId != View.NO_ID) {
                highest_qual_error_tv.gone()
                highest_qual_error_tv.text = null
            }
        }
    }

    private fun validateDataAndsubmit() {
        if (user_name_et.text.length <= 2) {
            full_name_error_tv.visible()
            full_name_error_tv.text = getString(R.string.name_should_be_more_than_2_chars)

            return
        } else {
            full_name_error_tv.gone()
            full_name_error_tv.text = null
        }

        if (dateOfBirth == null) {
            dob_error_tv.visible()
            dob_error_tv.text = getString(R.string.select_ur_dob)
            return
        } else {
            dob_error_tv.gone()
            dob_error_tv.text = null
        }

        if (gender_chip_group.checkedChipId == -1) {
            gender_error_tv.visible()
            gender_error_tv.text = getString(R.string.select_ur_gender)
            return
        } else {
            gender_error_tv.gone()
            gender_error_tv.text = null
        }

        if (pin_code_et.text.isNotBlank()) {
            val pinCode = pin_code_et.text.toString().toInt()

            if (pinCode < 10_00_00) {

                pincode_error_tv.visible()
                pincode_error_tv.text = "Please fill Pincode"
            } else {
                pincode_error_tv.gone()
                pincode_error_tv.text = null
            }
        } else {
            pincode_error_tv.gone()
            pincode_error_tv.text = null
        }

        if (highest_qual_chipgroup.checkedChipId == -1) {

            highest_qual_error_tv.visible()
            highest_qual_error_tv.text = getString(R.string.please_fill_highest_qual)
            return
        } else {
            highest_qual_error_tv.gone()
            highest_qual_error_tv.text = null
        }

        viewModel.updateUserDetails(
                uid = userId,
                phoneNumber = phoneNumber,
                name = user_name_et.text.toString(),
                dateOfBirth = dateOfBirth!!,
                pinCode = pin_code_et.text.toString(),
                gender = gender_chip_group.findViewById<Chip>(gender_chip_group.checkedChipId).text.toString(),
                highestQualification = highest_qual_chipgroup.findViewById<Chip>(highest_qual_chipgroup.checkedChipId).text.toString()
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(getString(R.string.okay).capitalize()) { _, _ -> }
                .show()
    }

    private fun initViewModel() {

        viewModel.profile
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    when (it) {
                        Lce.Loading -> {

                            user_details_main_layout.gone()
                            user_details_error.gone()
                            user_details_progressbar.visible()
                        }
                        is Lce.Content -> {
                            showUserDetailsMainLayout(
                                    showEditActions = true
                            )
                            showUserDetailsOnView(it.content)
                        }
                        is Lce.Error -> {
                            user_details_progressbar.gone()
                            user_details_main_layout.gone()
                            user_details_error.visible()

                            user_details_error.text = it.error
                        }
                    }
                })

        viewModel.submitUserDetailsState
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lse.Loading -> {

//                            submitBtn.showProgress {
//                                this.progressColor = Color.WHITE
//                            }
                        }
                        Lse.Success -> {
//                            submitBtn.hideProgress()

                            showToast(getString(R.string.user_details_submitted))
                            navigate(
                                    R.id.addProfilePictureFragment, bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to user_name_et.text.toString(),
                                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pin_code_et.text.toString(),
                                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
                            )
                            )
                        }
                        is Lse.Error -> {
//                            submitBtn.hideProgress()

                            showAlertDialog(getString(R.string.cannot_submit_info), it.error)
                        }
                    }
                })
    }

    private fun showUserDetailsMainLayout(showEditActions: Boolean) {
        user_details_error.gone()
        user_details_progressbar.gone()
        user_details_main_layout.visible()

        if (showEditActions) {
            skip_btn.visible()
            submitBtn.text = "Update"
        } else {
            skip_btn.gone()
            submitBtn.text = "Submit"
        }
    }

    private fun showUserDetailsOnView(content: ProfileData) = content.let {
        user_name_et.setText(it.name)

        if(it.dateOfBirth != null) {
            val dob = dateFormatter.format(it.dateOfBirth!!.toDate())
            this.dateOfBirth = it.dateOfBirth!!.toDate()
            date_of_birth_et.text = dob
        }

        gender_chip_group.selectChipWithText(it.gender)
        highest_qual_chipgroup.selectChipWithText(it.highestEducation)

        pin_code_et.setText(it.address.current.pincode)
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
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }
}