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
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lse
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.viewModels.SharedGigerAttendanceUnderManagerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_decline_gig_dialog.*
import kotlinx.android.synthetic.main.fragment_decline_gig_dialog_main.*
import javax.inject.Inject

interface DeclineGigDialogFragmentResultListener {

    fun gigDeclined()
}

@AndroidEntryPoint
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

    @Inject
    lateinit var eventTracker: IEventTracker

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
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.gig_declined_giger_gigs),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    mDeclineGigDialogFragmentResultListener?.gigDeclined()
                    if (gigId != null) sharedGigViewModel.gigDeclined(gigId!!)
                    dismiss()
                }
                is Lse.Error -> {
                    progressBar.invisible()
                    declineGigMainLayout.visible()
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_giger_gigs))
                        .setMessage(getString(R.string.unable_to_deline_gig_giger_gigs) + it.error)
                        .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
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
        declineGigLabel.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.please_let_us_know_your_reason_why_giger_declining_this_gig_giger_gigs)
        else
            getString(R.string.please_let_us_know_your_reason_why_are_you_declining_this_gig_giger_gigs)

        reason_sick_leave.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.giger_on_sick_leave_today_giger_gigs)
        else
            getString(R.string.i_m_on_sick_leave_today_giger_gigs)

        reason_on_leave.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.giger_have_some_personal_work_giger_gigs)
        else
            getString(R.string.i_have_some_personal_work_giger_gigs)

        reason_cant_reach_location.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.giger_can_t_reach_the_location_today_giger_gigs)
        else
            getString(R.string.i_can_t_reach_the_location_today_giger_gigs)

        reason_gig_unsuitable.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.this_gig_is_unsuitable_for_giger_giger_gigs)
        else
            getString(R.string.this_gig_is_unsuitable_for_me_giger_gigs)

        weekly_off.text = if (isAnyUserOtherThanGigerIsDecliningTheGig)
            getString(R.string.weekly_off_for_giger_gigs)
        else
            getString(R.string.weekly_off_for_me_gigs)

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
                    .setTitle(getString(R.string.alert_giger_gigs))
                    .setMessage(getString(R.string.select_the_reason_giger_gigs))
                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                    .show()

                return@setOnClickListener
            } else if (checkedRadioButtonId == R.id.reason_others
                && reason_et.text.isNullOrBlank()
            ) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert_giger_gigs))
                    .setMessage(getString(R.string.select_the_reason_giger_gigs))
                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                    .show()

                return@setOnClickListener
            }

            val reason = if (checkedRadioButtonId == R.id.reason_others) {
                reason_et.text.toString()
            } else {

                if (checkedRadioButtonId == R.id.reason_sick_leave) {

                    if (isAnyUserOtherThanGigerIsDecliningTheGig)
                        "Giger is on sick leave"
                    else
                        "I am on sick leave"
                } else if (checkedRadioButtonId == R.id.reason_on_leave) {

                    if (isAnyUserOtherThanGigerIsDecliningTheGig)
                        "Giger have some personal work"
                    else
                        "I have some personal work"
                } else if (checkedRadioButtonId == R.id.reason_cant_reach_location) {

                    if (isAnyUserOtherThanGigerIsDecliningTheGig)
                        "Giger can't reach work location"
                    else
                        "I can't reach work location"
                } else if (checkedRadioButtonId == R.id.reason_gig_unsuitable) {

                    if (isAnyUserOtherThanGigerIsDecliningTheGig)
                        "This gig is unsuitable for giger"
                    else
                        "This gig is unsuitable for me"
                }
                else if(checkedRadioButtonId == R.id.weekly_off){
                    if (isAnyUserOtherThanGigerIsDecliningTheGig)
                        "Giger is on weekly off."
                    else
                        "I am on weekly off."
                }
                else
                    reason_radio_group.findViewById<RadioButton>(checkedRadioButtonId).text.toString()
            }

            if (gigId != null) {
                viewModel.declineGig(gigId!!, reason, isAnyUserOtherThanGigerIsDecliningTheGig)
                if (isAnyUserOtherThanGigerIsDecliningTheGig) {
                    //event
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        gigId?.let { it1 ->
                            val map = mapOf(
                                "Gig ID" to it1,
                                "TL ID" to it,
                                "Decline reason" to reason
                            )
                            eventTracker.pushEvent(TrackingEventArgs("tl_marked_decline", map))
                        }

                    }

                } else {
                    //event
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        gigId?.let { it1 ->
                            val map = mapOf(
                                "Gig ID" to it1,
                                "TL ID" to it,
                                "Decline reason" to reason
                            )
                            eventTracker.pushEvent(TrackingEventArgs("giger_marked_decline", map))
                        }

                    }
                }
            }
            else if (!gigIds.isNullOrEmpty())
                viewModel.declineGigs(gigIds!!, reason)
        }
    }
}