package com.gigforce.giger_gigs.attendance_tl.select_decline_reasons

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.viewdatamodels.gig.DeclineReason
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.AttendanceTLSharedViewModel
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.databinding.FragmentMarkActiveConfirmationBinding
import com.gigforce.giger_gigs.databinding.FragmentMarkInactiveConfirmationBinding
import com.gigforce.giger_gigs.databinding.FragmentSelectInactiveReasonBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectMarkInactiveReasonsBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentSelectInactiveReasonBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_inactive_reason
) {
    companion object {
        const val TAG = "MarkActiveConfirmationBottomSheetFragment"

        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val REASON_ID_OTHERS = "others"
    }

    private val viewModel: SelectMarkInactiveReasonsBottomSheetViewModel by viewModels()
    private val sharedViewModel: AttendanceTLSharedViewModel by activityViewModels()
    private lateinit var gigId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID, gigId)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentSelectInactiveReasonBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initView()
            initViewModel()
        }
    }

    private fun initViewModel() {
        viewModel.viewState.observe(viewLifecycleOwner) {

            when (it) {
                is SelectMarkInactiveReasonsViewContract.UiState.ErrorWhileLoadingDeclineOptions -> showErrorInView(it.error)
                SelectMarkInactiveReasonsViewContract.UiState.LoadingDeclineOptions -> showLoadingView()
                is SelectMarkInactiveReasonsViewContract.UiState.ShowDeclineOptions -> showMainView(it.options)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun initView() = viewBinding.declineGigMainLayout.apply {

        reasonRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            if(reasonRadioGroup.checkedRadioButtonId == -1){
                return@setOnCheckedChangeListener
            }

            yesButton.isEnabled = true
            val checkedReasonId = reasonRadioGroup.findViewById<RadioButton>(
                reasonRadioGroup.checkedRadioButtonId
            ).tag.toString()

            if (checkedReasonId == REASON_ID_OTHERS) {
                reasonLabel.visible()
                reasonEt.visible()
            } else {
                reasonLabel.gone()
                reasonEt.gone()
            }
        }

        this.yesButton.setOnClickListener {
          //todo

            val checkedRadioButtonId = reasonRadioGroup.checkedRadioButtonId
            if (checkedRadioButtonId == -1) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert_giger_gigs))
                    .setMessage(getString(R.string.select_the_reason_giger_gigs))
                    .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                    .show()

                return@setOnClickListener
            } else {

                val checkedReason = reasonRadioGroup.findViewById<RadioButton>(reasonRadioGroup.checkedRadioButtonId).text.toString()
                val checkedReasonId = reasonRadioGroup.findViewById<RadioButton>(reasonRadioGroup.checkedRadioButtonId).tag.toString()

                if (checkedReasonId == REASON_ID_OTHERS &&
                    reasonEt.text.isNullOrBlank()
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_giger_gigs))
                        .setMessage(getString(R.string.select_the_reason_giger_gigs))
                        .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
                        .show()

                    return@setOnClickListener
                }

                val reason = if (checkedReasonId == REASON_ID_OTHERS) {
                    reasonEt.text.toString()
                } else {
                    reasonRadioGroup.findViewById<RadioButton>(reasonRadioGroup.checkedRadioButtonId).text.toString()
                }

                sharedViewModel.tlSelectedInactiveReasonConfirmationDialog(
                    gigId,
                    checkedReasonId,
                    reason
                )
                dismiss()
            }

        }

        this.noButton.setOnClickListener {
            dismiss()
        }
    }

    private fun showLoadingView() = viewBinding.apply{
        declineGigMainLayout.root.invisible()
        progressBar.visible()
        errorLayout.gone()
    }

    private fun showErrorInView(
        error: String
    ) = viewBinding.apply{
        declineGigMainLayout.root.invisible()
        progressBar.gone()
        errorLayout.visible()

        errorTV.text = error
    }

    private fun showMainView(
        options: List<DeclineReason>
    ) = viewBinding.apply{
        errorLayout.gone()
        progressBar.gone()
        declineGigMainLayout.root.visible()

        options.toMutableList().apply {
            add(DeclineReason(
                reasonId = REASON_ID_OTHERS,
                reason = getString(R.string.others_camel_case_giger_gigs)
            ))
        }.run {

            for (option in this) {
                val radioButton = layoutInflater.inflate(
                    R.layout.layout_decline_radio_button,
                    null,
                    false
                ) as RadioButton

                radioButton.id = View.generateViewId()
                radioButton.text = option.reason
                radioButton.tag = option.reasonId
                viewBinding.declineGigMainLayout.reasonRadioGroup.addView(radioButton)
            }
        }
    }
}