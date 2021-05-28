package com.gigforce.giger_gigs.dialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.gigforce.core.extensions.roundTo
import com.gigforce.giger_gigs.R
import kotlinx.android.synthetic.main.fragment_user_not_in_gig_range_dialog.*

class NotInGigRangeDialogFragment : DialogFragment() {

    companion object {
        const val INTENT_EXTRA_DISTANCE_FROM_GIG = "distance_from_gig"
        const val TAG = "NotInGigRangeDialogFragment"

        fun launch(
                distanceFromGigInMeters: Float,
                fragmentManager: FragmentManager
        ) {
            val frag = NotInGigRangeDialogFragment()
            frag.arguments = bundleOf(INTENT_EXTRA_DISTANCE_FROM_GIG to distanceFromGigInMeters)
            frag.isCancelable = true
            frag.show(fragmentManager, TAG)
        }
    }

    private var distanceFromGig: Float = 0.0f
    private val gigSharedViewModel : SharedGigViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_not_in_gig_range_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            distanceFromGig = it.getFloat(INTENT_EXTRA_DISTANCE_FROM_GIG)
        }

        arguments?.let {
            distanceFromGig = it.getFloat(INTENT_EXTRA_DISTANCE_FROM_GIG)
        }
        initView()
        initViewModel()
    }

    private fun initViewModel() {
    }

    override fun isCancelable(): Boolean {
        return false
    }


//    override fun onStart() {
//        super.onStart()
//        dialog?.window?.apply {
//
//            setBackgroundDrawableResource(R.drawable.dialog_round_bg)
//            setLayout(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//        }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat(INTENT_EXTRA_DISTANCE_FROM_GIG, distanceFromGig)
    }

    private fun initView() {
        try_later_btn.setOnClickListener { dismiss() }

        submitBtn.setOnClickListener {
            gigSharedViewModel.userOkayWithNotBeingInLocation(distanceFromGig)
            dismiss()
        }

        val distanceFromgig = if (distanceFromGig > 1000L) {
            "You are ${(distanceFromGig / 1000).roundTo(2)} Km(s) away from gig location"
        } else {
            "You are ${distanceFromGig.roundTo(2)} Mtr(s) away from gig location"
        }
        location_from_gig_tv.text = distanceFromgig
    }
}