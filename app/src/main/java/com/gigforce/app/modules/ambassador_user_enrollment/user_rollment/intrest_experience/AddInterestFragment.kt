package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.intrest_experience

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.selectChipsWithText
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_user_interest.*
import kotlinx.android.synthetic.main.fragment_ambsd_user_interest_main.*

class AddUserInterestFragment : BaseFragment() {

    private val interestAndExperienceViewModel: InterestAndExperienceViewModel by viewModels()
    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String
    private var pincode = ""
    private var mode: Int = EnrollmentConstants.MODE_ADD

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_user_interest_main, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
        getUserInterest()
    }

    private fun getUserInterest() {

        if(mode == EnrollmentConstants.MODE_EDIT) {
            viewModel.getProfileForUser(userId)
        } else {
            showMainLayout(shouldShowEditAction = false)
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }

        savedInstanceState?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
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
    }


    private fun initUi() {
        submitBtn.setOnClickListener {

            if (interest_chipgroup.checkedChipIds.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                        .setMessage("Please Select At Least One Chip")
                        .setPositiveButton("Okay") { _, _ -> }
                        .show()

                return@setOnClickListener
            }

            submitInterests()
        }

        skip_btn.setOnClickListener {

            navigate(R.id.addUserExperienceFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
            ))
        }

        ic_back_iv.setOnClickListener {
            showGoBackConfirmationDialog()
        }
    }

    private fun submitInterests() {
        val interests: MutableList<String> = mutableListOf()

        interest_chipgroup.checkedChipIds.forEach {
            val interest = interest_chipgroup.findViewById<Chip>(it).text.toString()
            interests.add(interest)
        }

        interestAndExperienceViewModel.submitInterests(userId, interests)
    }

    private fun initViewModel() {

        viewModel.profile
                .observe(viewLifecycleOwner, Observer {
                    when (it) {
                        Lce.Loading -> {
                            user_interest_main_layout.gone()
                            user_interest_error.gone()
                            user_interest_progressbar.visible()
                        }
                        is Lce.Content -> {
                            showMainLayout(
                                    shouldShowEditAction = true
                            )

                            setDataOnView(it.content)
                        }
                        is Lce.Error -> {
                            user_interest_progressbar.gone()
                            user_interest_main_layout.gone()
                            user_interest_error.visible()

                            user_interest_error.text = "Unable to fetch interest detail, ${it.error}"
                        }
                    }
                })


        interestAndExperienceViewModel
                .submitInterestState
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lse.Loading -> {

                        }
                        Lse.Success -> {
                            navigate(R.id.addUserExperienceFragment, bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
                                    ))
                        }
                        is Lse.Error -> {

                            MaterialAlertDialogBuilder(requireContext())
                                    .setMessage("Unable to submit interest")
                                    .setMessage(it.error)
                                    .setPositiveButton("Okay") { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    private fun showMainLayout(shouldShowEditAction : Boolean) {
        user_interest_progressbar.gone()
        user_interest_error.gone()
        user_interest_main_layout.visible()

        if(shouldShowEditAction){
            skip_btn.visible()
        } else {
            skip_btn.gone()
        }
    }

    private fun setDataOnView(content: ProfileData) = content.interests?.let {

        if (it.isNotEmpty()) {
            interest_chipgroup.selectChipsWithText(
                    it.map { interest ->
                        interest.name
                    })

            submitBtn.text = "Update"
            skip_btn.visible()
        } else{
            submitBtn.text = "Submit"
            skip_btn.gone()
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
}