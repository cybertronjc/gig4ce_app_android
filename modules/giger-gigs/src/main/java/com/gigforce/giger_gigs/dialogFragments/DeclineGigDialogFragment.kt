package com.gigforce.giger_gigs.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lse
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.viewModels.SharedGigerAttendanceUnderManagerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_decline_gig_dialog.*
import kotlinx.android.synthetic.main.fragment_decline_gig_dialog_main.*

interface DeclineGigDialogFragmentResultListener {

    fun gigDeclined()
}

class DeclineGigDialogFragment : DialogFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_GIG_IDS = "gig_ids"
        const val INTENT_ANY_OTHER_USER_DECLINING_GIG = "other_user_decline"
        const val TAG = "DeclineGigDialogFragment"

        fun launch(
            gigId: String,
            fragmentManager: FragmentManager,
            declineGigDialogFragmentResultListener: DeclineGigDialogFragmentResultListener? = null,
            isAnyUserOtherThanGigerIsDecliningTheGig: Boolean = false
        ) {
            val frag = DeclineGigDialogFragment()
            frag.arguments = bundleOf(
                INTENT_EXTRA_GIG_ID to gigId,
                INTENT_ANY_OTHER_USER_DECLINING_GIG to isAnyUserOtherThanGigerIsDecliningTheGig
            )
            frag.mDeclineGigDialogFragmentResultListener = declineGigDialogFragmentResultListener
            frag.show(fragmentManager, TAG)
        }

        fun launch(
            gigIds: List<String>,
            fragmentManager: FragmentManager,
            declineGigDialogFragmentResultListener: DeclineGigDialogFragmentResultListener? = null
        ) {
            val frag = DeclineGigDialogFragment()
            frag.arguments = bundleOf(INTENT_EXTRA_GIG_IDS to ArrayList(gigIds))
            frag.mDeclineGigDialogFragmentResultListener = declineGigDialogFragmentResultListener
            frag.show(fragmentManager, TAG)
        }
    }

    private val sharedGigViewModel: SharedGigerAttendanceUnderManagerViewModel by activityViewModels()
    private val viewModel: GigViewModel by viewModels()

    private var gigId: String? = null
    private var gigIds: ArrayList<String>? = null
    private var isAnyUserOtherThanGigerIsDecliningTheGig = false

    private var mDeclineGigDialogFragmentResultListener: DeclineGigDialogFragmentResultListener? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_decline_gig_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID)
            gigIds = it.getStringArrayList(INTENT_EXTRA_GIG_IDS)
            isAnyUserOtherThanGigerIsDecliningTheGig =
                it.getBoolean(INTENT_ANY_OTHER_USER_DECLINING_GIG, false)
        }

        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID)
            gigIds = it.getStringArrayList(INTENT_EXTRA_GIG_IDS)
            isAnyUserOtherThanGigerIsDecliningTheGig =
                it.getBoolean(INTENT_ANY_OTHER_USER_DECLINING_GIG, false)
        }
        initView()
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GIG_ID, gigId)
        outState.putStringArrayList(INTENT_EXTRA_GIG_IDS, gigIds)
        outState.putBoolean(
            INTENT_ANY_OTHER_USER_DECLINING_GIG,
            isAnyUserOtherThanGigerIsDecliningTheGig
        )
    }


    private fun initViewModel() {
        viewModel.declineGig.observe(viewLifecycleOwner, {

            when (it) {
                Lse.Loading -> {
                    declineGigMainLayout.invisible()
                    progressBar.visible()
                }
                Lse.Success -> {
                    Toast.makeText(requireContext(), "Gig Declined", Toast.LENGTH_LONG)
                        .show()
                    mDeclineGigDialogFragmentResultListener?.gigDeclined()
                    if (gigId != null) sharedGigViewModel.gigDeclined(gigId!!)
                    dismiss()
                }
                is Lse.Error -> {
                    progressBar.invisible()
                    declineGigMainLayout.visible()
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Unable to decline gig, ${it.error}")
                        .setPositiveButton("Okay") { _, _ -> }
                        .show()
                }
            }
        })
    }

//    override fun onStart() {
//        super.onStart()
//        dialog?.window?.apply {
//            setBackgroundDrawableResource(R.drawable.dialog_round_bg)
//            setLayout(
//                    (getScreenWidth(requireActivity()).width - resources.getDimension(R.dimen.size_32)).toInt(),
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//
//        }
//    }


    private fun initView() {
        reason_sick_leave.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.giger_on_sick_leave_today)
        else
            getString(R.string.i_m_on_sick_leave_today)

        reason_on_leave.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.giger_have_some_personal_work)
        else
            getString(R.string.i_have_some_personal_work)

        reason_cant_reach_location.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.giger_can_t_reach_the_location_today)
        else
            getString(R.string.i_can_t_reach_the_location_today)

        reason_gig_unsuitable.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.this_gig_is_unsuitable_for_giger)
        else
            getString(R.string.this_gig_is_unsuitable_for_me)

        reason_radio_group.setOnCheckedChangeListener { _, checkedId ->
            submitBtn.isEnabled = true

            if (checkedId == R.id.reason_others) {
                reason_label.visible()
                reason_et.visible()
            } else {
                reason_label.gone()
                reason_et.gone()
            }
        }


        submitBtn.setOnClickListener {

            val checkedRadioButtonId = reason_radio_group.checkedRadioButtonId
            if (checkedRadioButtonId == -1) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Please select the reason")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()

                return@setOnClickListener
            } else if (checkedRadioButtonId == R.id.reason_others
                && reason_et.text.isNullOrBlank()
            ) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Please type the reason")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()

                return@setOnClickListener
            }

            val reason = if (checkedRadioButtonId == R.id.reason_others) {
                reason_et.text.toString()
            } else {
                reason_radio_group.findViewById<RadioButton>(checkedRadioButtonId).text.toString()
            }

            if (gigId != null)
                viewModel.declineGig(gigId!!, reason,isAnyUserOtherThanGigerIsDecliningTheGig)
            else if (!gigIds.isNullOrEmpty())
                viewModel.declineGigs(gigIds!!, reason)
        }
    }
}