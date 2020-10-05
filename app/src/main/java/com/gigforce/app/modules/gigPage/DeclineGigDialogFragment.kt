package com.gigforce.app.modules.gigPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.visible
import com.gigforce.app.utils.Lse
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
        const val TAG = "DeclineGigDialogFragment"

        fun launch(
            gigId: String,
            fragmentManager: FragmentManager,
            declineGigDialogFragmentResultListener: DeclineGigDialogFragmentResultListener
        ) {
            val frag = DeclineGigDialogFragment()
            frag.arguments = bundleOf(INTENT_EXTRA_GIG_ID to gigId)
            frag.mDeclineGigDialogFragmentResultListener = declineGigDialogFragmentResultListener
            frag.show(fragmentManager, TAG)
        }

        fun launch(
            gigIds: List<String>,
            fragmentManager: FragmentManager,
            declineGigDialogFragmentResultListener: DeclineGigDialogFragmentResultListener
        ) {
            val frag = DeclineGigDialogFragment()
            frag.arguments = bundleOf(INTENT_EXTRA_GIG_IDS to ArrayList(gigIds))
            frag.mDeclineGigDialogFragmentResultListener = declineGigDialogFragmentResultListener
            frag.show(fragmentManager, TAG)
        }
    }

    private val viewModel: GigViewModel by viewModels()

    private var gigId: String? = null
    private var gigIds: ArrayList<String>? = null

    private lateinit var mDeclineGigDialogFragmentResultListener: DeclineGigDialogFragmentResultListener

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
        }

        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID)
            gigIds = it.getStringArrayList(INTENT_EXTRA_GIG_IDS)
        }
        initView()
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GIG_ID, gigId)
        outState.putStringArrayList(INTENT_EXTRA_GIG_IDS, gigIds)
    }



    private fun initViewModel() {
        viewModel.declineGig.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lse.Loading -> {
                    declineGigMainLayout.invisible()
                    progressBar.visible()
                }
                Lse.Success -> {
                    Toast.makeText(requireContext(), "Gig Declined", Toast.LENGTH_LONG)
                        .show()
                    mDeclineGigDialogFragmentResultListener.gigDeclined()
                    dismiss()
                }
                is Lse.Error -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Unable to decline gig, ${it.error}")
                        .setPositiveButton("Okay") { _, _ -> }
                        .show()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    private fun initView() {

        reason_radio_group.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.reason_others) {
                reason_label.visible()
                reason_et.visible()
            } else {
                reason_label.gone()
                reason_et.gone()
            }
        }

        confirm_decline_cb.setOnCheckedChangeListener { _, isChecked ->
            submitBtn.isEnabled = isChecked
        }

        submitBtn.setOnClickListener {

            val checkedRadioButtonId = reason_radio_group.checkedRadioButtonId
            if (checkedRadioButtonId == -1) {

                confirm_decline_cb.isChecked = false
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Please select the reason")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()

                return@setOnClickListener
            } else if (checkedRadioButtonId == R.id.reason_others
                && reason_et.text.isNullOrBlank()
            ) {

                confirm_decline_cb.isChecked = false
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
                viewModel.declineGig(gigId!!, reason)
            else if (!gigIds.isNullOrEmpty())
                viewModel.declineGigs(gigIds!!, reason)
        }
    }
}