package com.gigforce.giger_gigs.bottomsheets

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.adapters.GigsListForDeclineAdapter
import com.gigforce.giger_gigs.adapters.GigsListForDeclineAdapterListener
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragment
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragmentResultListener
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_gigs_list_for_decline.*
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class GigsListForDeclineBottomSheet : BottomSheetDialogFragment(),
    DeclineGigDialogFragmentResultListener, GigsListForDeclineAdapterListener {

    @Inject lateinit var navigation : INavigation

    private val mAdapter: GigsListForDeclineAdapter by lazy {
        GigsListForDeclineAdapter(requireContext()).apply {
            this.setGigsListForDeclineAdapterListener(this@GigsListForDeclineBottomSheet)
        }
    }

    private val viewModel: GigViewModel by viewModels()

    private lateinit var date: LocalDate

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gigs_list_for_decline, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {

        arguments?.let {
            date = it.getSerializable(INTEN_EXTRA_DATE) as LocalDate
        }

        savedInstanceState?.let {
            date = it.getSerializable(INTEN_EXTRA_DATE) as LocalDate
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(INTEN_EXTRA_DATE, date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            val dialog = it as BottomSheetDialog
            BottomSheetBehavior.from(dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)!!)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return bottomSheetDialog
    }

    private fun initView() {

        disableSubmitButton()
        cross_iv.setOnClickListener {
            dismiss()
        }

        gigs_recycler_view.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        mAdapter.setOnLearningVideoActionListener {

            if (mAdapter.selectedGigCount != 0)
                enableSubmitButton()
            else
                disableSubmitButton()
        }

        gigs_recycler_view.adapter = mAdapter

        decline_slider_btn.setOnClickListener {

            val selectedGig = mAdapter.getSelectedGig().map {
                it.gigId
            }
            if (selectedGig.isNotEmpty() && isAdded) {
                DeclineGigDialogFragment.launch(
                    selectedGig,
                    childFragmentManager,
                    this@GigsListForDeclineBottomSheet
                )
            } else {
                Toast.makeText(requireContext(), getString(R.string.select_gig), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        PushDownAnim.setPushDownAnimTo(tv_okay_no_gigs_present)
            .setOnClickListener(View.OnClickListener {
                dismiss()
            })
    }

    private fun initViewModel() {
        viewModel
            .todaysGigs
            .observe(viewLifecycleOwner,
                androidx.lifecycle.Observer {

                    when (it) {
                        Lce.Loading -> showTodaysGigsLoading()
                        is Lce.Content -> showTodaysGig(it.content.filter {
                            GigStatus.fromGig(it) != GigStatus.DECLINED
                        })
                        is Lce.Error -> showError(it.error)
                    }
                })

        viewModel.startWatchingTodaysOngoingAndUpcomingGig(date)
    }

    private fun showTodaysGigsLoading() {
        mAdapter.updateGig(emptyList())
        progress_bar.visible()
    }

    private fun showTodaysGig(content: List<Gig>) {
        progress_bar.gone()


        disableSubmitButton()

        gig_message_tv.text = when {
            content.isEmpty() -> {

                decline_slider_btn.gone()
                gig_error.gone()
                ll_decline_layout.visible()
                cross_iv.gone()
                rl_gigs.gone()
                gig_message_tv.gone()

//                gig_error.text = "No upcoming gigs on this day"
                ""
            }
            content.size == 1 -> {
                gig_error.gone()
                decline_slider_btn.visible()
                getString(R.string.no_new_gigs_will_e_assigned)
            }
            else -> {
                gig_error.gone()
                decline_slider_btn.visible()

                getString(R.string.no_new_gig_assigned) + content.size + getString(R.string.gigs_assigned)
            }
        }

        mAdapter.updateGig(content)
    }

    private fun showError(error: String) {
        mAdapter.updateGig(emptyList())
        progress_bar.gone()
        gig_error.visible()
        gig_error.text = error
    }

    override fun gigDeclined() {
        Toast.makeText(requireContext(), getString(R.string.gig_declined), Toast.LENGTH_SHORT).show()
    }

    private fun disableSubmitButton() {
        decline_slider_btn.isEnabled = false
    }

    private fun enableSubmitButton() {
        decline_slider_btn.isEnabled = true
    }

    companion object {

        const val INTEN_EXTRA_DATE = "date"
    }

    override fun onCallClicked(gig: Gig) {
        if (gig.openNewGig()) {

            if (gig.agencyContact?.contactNumber.isNullOrEmpty())
                return
        } else if (gig.gigContactDetails?.contactNumberString.isNullOrEmpty()) {
            return
        }

        val contactNumber = if (gig.openNewGig()) {
            gig.agencyContact?.contactNumber
        } else {
            gig.gigContactDetails?.contactNumberString
        }

        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", contactNumber, null)
        )
        startActivity(intent)
    }

    override fun onMessageClicked(gig: Gig) {


        if (gig.agencyContact != null && gig.agencyContact?.uid != null) {

            val userName = gig.agencyContact?.name ?: ""
            val profilePicture = gig.agencyContact?.profilePicture ?: ""
            navigation.navigateTo("chats/chatPage",bundleOf(
                AppConstants.INTENT_EXTRA_CHAT_TYPE to AppConstants.CHAT_TYPE_USER,
                AppConstants.INTENT_EXTRA_OTHER_USER_ID to gig.agencyContact?.uid,
                AppConstants.INTENT_EXTRA_CHAT_HEADER_ID to "",
                AppConstants.INTENT_EXTRA_OTHER_USER_NAME to userName,
                AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to profilePicture
            ))
//            findNavController().navigate(
//                R.id.chatPageFragment, bundleOf(
//                    AppConstants.INTENT_EXTRA_CHAT_TYPE to AppConstants.CHAT_TYPE_USER,
//                    AppConstants.INTENT_EXTRA_OTHER_USER_ID to gig.agencyContact?.uid,
//                    AppConstants.INTENT_EXTRA_CHAT_HEADER_ID to "",
//                    AppConstants.INTENT_EXTRA_OTHER_USER_NAME to userName,
//                    AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to profilePicture
//                )
//            )
        }
    }
}