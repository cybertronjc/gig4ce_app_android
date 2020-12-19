package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.intrest_experience

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.utils.Lse
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_user_enoll_interest.*

class AddUserInterestFragment : BaseFragment() {

    private val interestAndExperienceViewModel: InterestAndExperienceViewModel by viewModels()
    private lateinit var userId: String
    private lateinit var userName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_user_enoll_interest, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initUi()
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
        interestAndExperienceViewModel
            .submitInterestState
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lse.Loading -> {

                    }
                    Lse.Success -> {
                        navigate(R.id.addUserExperienceFragment, bundleOf(
                            EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                            EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
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
}