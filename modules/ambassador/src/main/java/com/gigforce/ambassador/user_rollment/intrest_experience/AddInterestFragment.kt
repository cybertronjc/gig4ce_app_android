package com.gigforce.ambassador.user_rollment.intrest_experience

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gigforce.ambassador.EnrollmentConstants
import com.gigforce.ambassador.R
import com.gigforce.ambassador.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.datamodels.profile.Skill2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.selectChipsWithText
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ambsd_add_experience.*
import kotlinx.android.synthetic.main.fragment_ambsd_user_interest.*
import kotlinx.android.synthetic.main.fragment_ambsd_user_interest_main.*
import kotlinx.android.synthetic.main.fragment_ambsd_user_interest_main.skip_btn
import kotlinx.android.synthetic.main.fragment_ambsd_user_interest_main.submitBtn
import kotlinx.android.synthetic.main.fragment_ambsd_user_interest_main.toolbar_layout
import kotlinx.android.synthetic.main.fragment_gig_page_2_details.*
import javax.inject.Inject
@AndroidEntryPoint
class AddUserInterestFragment : Fragment(),IOnBackPressedOverride {

    private val interestAndExperienceViewModel: InterestAndExperienceViewModel by viewModels()
    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String
    private var pincode = ""
    private var mode: Int = EnrollmentConstants.MODE_ADD

    @Inject lateinit var navigation : INavigation

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_ambsd_user_interest, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
        getUserInterest()
    }

    private fun getUserInterest() {

        if(mode == EnrollmentConstants.MODE_EDIT) {
            interestAndExperienceViewModel.getInterestForUser(userId,shouldFetchProfileDataToo = true)
        } else {
            interestAndExperienceViewModel.getInterestForUser(userId,shouldFetchProfileDataToo = false)
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
                        .setMessage(getString(R.string.please_select_atleast_one_chip_amb))
                        .setPositiveButton(
                                getString(R.string.okay_amb).capitalize()
                        ) { _, _ -> }.show()

                return@setOnClickListener
            }

            if (interest_chipgroup.checkedChipIds.size > 3) {

                MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.select_only_3_chips_amb))
                        .setPositiveButton(
                                getString(R.string.okay_amb).capitalize()
                        ) { _, _ -> }.show()

                return@setOnClickListener
            }


            submitInterests()
        }

        skip_btn.setOnClickListener {
            navigation.navigateTo("userinfo/addUserExperienceFragment",bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
            ))
//            navigate(
//                R.id.addUserExperienceFragment, bundleOf(
//                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
//                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
//                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
//                )
//            )
        }

        toolbar_layout.apply {
            showTitle(getString(R.string.add_interest_amb))
            hideActionMenu()
            setBackButtonListener(View.OnClickListener {
                showGoBackConfirmationDialog()
            })
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

        interestAndExperienceViewModel.fetchUserInterestDataState
                .observe(viewLifecycleOwner, Observer {
                    when (it) {
                        Lce.Loading -> {
                            user_interest_main_layout.gone()
                            user_interest_error.gone()
                            user_interest_progressbar.visible()
                        }
                        is Lce.Content -> {
                            showMainLayout(
                                    shouldShowEditAction = it.content.profileData != null,
                                    interest = it.content.interest
                            )

                            if(it.content.profileData != null)
                                it.content.profileData?.let {
                                    setDataOnView(it)
                                }
                        }
                        is Lce.Error -> {
                            user_interest_progressbar.gone()
                            user_interest_main_layout.gone()
                            user_interest_error.visible()

                            user_interest_error.text =
                                getString(R.string.unable_to_fetch_interest_details_amb) + it.error
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
                            navigation.navigateTo("userinfo/addUserExperienceFragment",bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
                            ))
//                        navigate(
//                            R.id.addUserExperienceFragment, bundleOf(
//                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
//                                EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
//                                EnrollmentConstants.INTENT_EXTRA_MODE to mode
//                            )
//                        )
                        }
                        is Lse.Error -> {

                            MaterialAlertDialogBuilder(requireContext())
                                    .setMessage(getString(R.string.unable_to_submit_interest_amb))
                                    .setMessage(it.error)
                                    .setPositiveButton(getString(R.string.okay_amb).capitalize()) { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    private fun showMainLayout(
        shouldShowEditAction : Boolean,
        interest : List<Skill2>,
    ) {
        user_interest_progressbar.gone()
        user_interest_error.gone()
        user_interest_main_layout.visible()

        populateSkillsSpinner(
                interest.map { it.skill }
        )
        if(shouldShowEditAction){
            skip_btn.visible()
        } else {
            skip_btn.gone()
        }
    }

    private fun populateSkillsSpinner(
            skills: List<String>
    ) {
        interest_chipgroup.removeAllViews()
        skills.forEach {

            val chip: Chip = layoutInflater.inflate(
                    R.layout.fragment_ambassador_role_chip,
                    interest_chipgroup,
                    false
            ) as Chip
            chip.text = it
            chip.id = ViewCompat.generateViewId()
            interest_chipgroup.addView(chip)
        }

        interest_chipgroup.isSingleSelection = false
    }

    private fun setDataOnView(
            content: ProfileData
    ) = content.skills?.let {

        if (it.isNotEmpty()) {
            interest_chipgroup.selectChipsWithText(
                    it.map { interest ->
                        interest.id
                    })

            submitBtn.text = "Update"
            skip_btn.visible()
        } else {
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
                .setTitle(getString(R.string.alert_amb))
                .setMessage(getString(R.string.sure_to_go_back_amb))
                .setPositiveButton(getString(R.string.yes_amb)) { _, _ -> goBackToUsersList() }
                .setNegativeButton(getString(R.string.no_amb)) { _, _ -> }
                .show()
    }

    private fun goBackToUsersList() {
        findNavController().navigateUp()
//        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }
}