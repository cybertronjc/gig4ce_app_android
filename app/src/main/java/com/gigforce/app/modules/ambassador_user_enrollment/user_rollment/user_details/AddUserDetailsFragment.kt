package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lse
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_user_details.*
import java.text.SimpleDateFormat
import java.util.*

class AddUserDetailsFragment : BaseFragment() {

    private val viewModel: UserDetailsViewModel by viewModels()

    private lateinit var userId: String
    private lateinit var phoneNumber : String
    private var dateOfBirth: Date? = null

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
                date_of_birth_et.setText(dateFormatter.format(newCal.time))
                dob_okay_iv.visible()
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
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            phoneNumber= it.getString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            phoneNumber= it.getString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER, phoneNumber)
    }

    private fun initListeners() {
        user_name_et.textChanged {
            user_name_okay_iv.isVisible = it.length > 2
        }

        date_of_birth_et.setOnClickListener {

            dateOfBirthPicker.datePicker.maxDate = Date().time
            dateOfBirthPicker.show()
        }

        pin_code_et.textChanged {
            pin_okay_iv.isVisible = it.length == 6 && it.toString().toInt() > 10_00_00
        }

        submitBtn.setOnClickListener {
            validateDataAndsubmit()
        }
    }

    private fun validateDataAndsubmit() {
        if (user_name_et.text.length <= 2) {
            showAlertDialog("Invalid name", "Name should be more than 2 characters")
            return
        }

        if (dateOfBirth == null) {
            showAlertDialog("Dob not filled", "Select your date of birth")
            return
        }

        if (gender_chip_group.checkedChipId == -1) {
            showAlertDialog("select Gender", "Select your gender")
            return
        }

//        if (pin_code_et.text.length != 6 && pin_code_et.text.toString().toInt() > 10_00_00) {
//            showAlertDialog("Invalid pincode", "Provide a valid Pin Code")
//            return
//        }

        if (highest_qual_chipgroup.checkedChipId == -1) {
            showAlertDialog("Select highest qualification", "Please fill highest qualification")
            return
        }

        viewModel.updateUserDetails(
            uid = userId,
            phoneNumber = phoneNumber,
            name = user_name_et.text.toString(),
            dateOfBirth = dateOfBirth!!,
            gender = gender_chip_group.findViewById<Chip>(gender_chip_group.checkedChipId).text.toString(),
            highestQualification = highest_qual_chipgroup.findViewById<Chip>(highest_qual_chipgroup.checkedChipId).text.toString()
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.submitUserDetailsState
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lse.Loading -> {
//                        UtilMethods.showLoading(requireContext())
                    }
                    Lse.Success -> {
  //                      UtilMethods.hideLoading()
                        showToast("User Details submitted")
                        navigate(
                            R.id.addProfilePictureFragment, bundleOf(
                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId
                            )
                        )
                    }
                    is Lse.Error -> {
 //                       UtilMethods.hideLoading()
                     showAlertDialog("Could not submit info", it.error)
                    }
                }
            })
    }
}