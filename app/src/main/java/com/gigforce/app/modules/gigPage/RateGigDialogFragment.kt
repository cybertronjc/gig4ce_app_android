package com.gigforce.app.modules.gigPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_rate_gig_dialog.*


class RateGigDialogFragment : DialogFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val TAG = "RateGigDialogFragment"

        fun launch(gigId: String, fragmentManager: FragmentManager) {
            val frag = RateGigDialogFragment()
            frag.arguments = bundleOf(INTENT_EXTRA_GIG_ID to gigId)
            frag.show(fragmentManager, TAG)
        }
    }

    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rate_gig_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }
        initView()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.gigDetails.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lce.Loading -> {}
                is Lce.Content -> {

                    val rating = it.content.gigRating
                    val feedback = it.content.gigUserFeedback
                    ratingBar.rating = rating
                    reviewET.setText(feedback)
                }
                is Lce.Error -> {

                }
            }
        })

        viewModel.watchGig(gigId)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GIG_ID, gigId)
    }

    private fun initView() {
        submitBtn.setOnClickListener {

            val rating = ratingBar.rating
            val feedback = reviewET.text.toString()

            if (rating == 0.0f) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Please provide a rating")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()

                return@setOnClickListener
            }

            viewModel.submitGigFeedback(gigId, rating, feedback, emptyList())
            Toast.makeText(requireContext(), "Feedback Submitted", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}