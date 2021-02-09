package com.gigforce.app.modules.gigPage

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.PushDownAnim
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gigs_list_for_decline.*
import java.time.LocalDate


class GigsListForDeclineBottomSheet : BottomSheetDialogFragment(),
        DeclineGigDialogFragmentResultListener, GigsListForDeclineAdapterListener {

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

        decline_slider_btn.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {
                    override fun onSlideComplete(view: SlideToActView) {

                        val selectedGig = mAdapter.getSelectedGig().map {
                            it.gigId
                        }
                        if (selectedGig.isNotEmpty()) {
                            DeclineGigDialogFragment.launch(
                                    selectedGig,
                                    childFragmentManager,
                                    this@GigsListForDeclineBottomSheet
                            )
                        } else {
                            view.resetSlider()
                            Toast.makeText(requireContext(), "Please Select Gig", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                }
        PushDownAnim.setPushDownAnimTo(tv_okay_no_gigs_present).setOnClickListener(View.OnClickListener {
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
                                is Lce.Content -> showTodaysGig(it.content)
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

        if (decline_slider_btn.isCompleted()) {
            decline_slider_btn.resetSlider()
        }
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
                "Alright, No new Gigs will be asssigned to you this day. However, you have 1 gig assigned to you"
            }
            else -> {
                gig_error.gone()
                decline_slider_btn.visible()
                "Alright, No new Gigs will be asssigned to you this day. However, you have ${content.size} gigs assigned to you"
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
        Toast.makeText(requireContext(), "Gig Declined", Toast.LENGTH_SHORT).show()
    }

    private fun disableSubmitButton() {
        decline_slider_btn.isEnabled = false

        decline_slider_btn.textColor = ResourcesCompat.getColor(resources, R.color.warm_grey, null)
        decline_slider_btn.outerColor =
                ResourcesCompat.getColor(resources, R.color.light_grey, null)
        decline_slider_btn.innerColor =
                ResourcesCompat.getColor(resources, R.color.warm_grey, null)
    }

    private fun enableSubmitButton() {
        decline_slider_btn.isEnabled = true

        decline_slider_btn.textColor = ResourcesCompat.getColor(resources, R.color.lipstick, null)
        decline_slider_btn.outerColor =
                ResourcesCompat.getColor(resources, R.color.light_pink, null)
        decline_slider_btn.innerColor =
                ResourcesCompat.getColor(resources, R.color.lipstick, null)
    }

    companion object {

        const val INTEN_EXTRA_DATE = "date"
    }

    override fun onCallClicked(gig: Gig) {
        if (gig.gigContactDetails?.contactNumberString.isNullOrEmpty()) return
        val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.fromParts("tel", gig.gigContactDetails?.contactNumber?.toString(), null)
        )
        startActivity(intent)
    }

    override fun onMessageClicked(gig: Gig) {
        findNavController().navigate(R.id.fakeGigContactScreenFragment)
    }
}