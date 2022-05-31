package com.gigforce.app.tl_work_space.activity_tacker.mark_inactive_attendance

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.viewdatamodels.gig.DeclineReason
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import com.gigforce.app.navigation.tl_workspace.attendance.GigAttendanceConstants
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentSelectInactiveReasonBinding
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SelectMarkInactiveReasonsBottomSheetFragment :
    BaseBottomSheetDialogFragment<FragmentSelectInactiveReasonBinding>(
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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        arguments?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
        }
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
        val bottomSheet  = viewBinding.root.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        if (viewCreatedForTheFirstTime) {
            initView()
            initViewModel()
        }
    }

    private fun initViewModel() {
        lifecycleScope.launchWhenCreated {

            viewModel
                .viewState
                .collect {

                    when (it) {
                        is SelectMarkInactiveReasonsViewContract.UiState.ErrorWhileLoadingDeclineOptions -> showErrorInView(
                            it.error
                        )
                        SelectMarkInactiveReasonsViewContract.UiState.LoadingDeclineOptions -> showLoadingView()
                        is SelectMarkInactiveReasonsViewContract.UiState.ShowDeclineOptions -> showMainView(
                            it.options
                        )
                        SelectMarkInactiveReasonsViewContract.UiState.DeclineMarkedSuccessfully -> dismiss()
                        is SelectMarkInactiveReasonsViewContract.UiState.ErrorWhileMarkingDecline -> errorWhileMarkingDecline(
                            it.error
                        )
                        SelectMarkInactiveReasonsViewContract.UiState.MarkingDecline -> showMarkingDecline()
                    }
                }
        }
    }

    private fun errorWhileMarkingDecline(
        error: String
    ) = viewBinding.declineGigMainLayout.apply {

        this.noButton.isEnabled = true
        this.yesButton.isEnabled = true
        this.yesButton.hideProgress("Yes")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unable to mark absent")
            .setMessage(error)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun showMarkingDecline() = viewBinding.declineGigMainLayout.apply {

        this.noButton.isEnabled = false
        this.yesButton.isEnabled = false
        this.yesButton.showProgress {
            this.buttonText = "Marking Absent.."
            this.progressColor = Color.WHITE
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog.apply {
            setOnShowListener { dialog ->

                //Makes the Bottom Open full , without it opens half
                val d: BottomSheetDialog = dialog as BottomSheetDialog
                val bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

                bottomSheet?.let {
                    BottomSheetBehavior.from(it).apply {
                        state = BottomSheetBehavior.STATE_EXPANDED
                        skipCollapsed = true
                    }

                }
            }
        }
    }

    private fun initView() = viewBinding.declineGigMainLayout.apply {

        bindProgressButton(this.yesButton)
        yesButton.attachTextChangeAnimator()

        reasonEt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus){

                lifecycleScope.launch {
                    try {
                        delay(400L)

                        mainNestedScrollView.post {
                            mainNestedScrollView.fullScroll(View.FOCUS_DOWN)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        reasonRadioGroup.setOnCheckedChangeListener { _, checkedId ->

            if (reasonRadioGroup.checkedRadioButtonId == -1) {
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
                    .setTitle("Alert")
                    .setMessage("Please select the reason")
                    .setPositiveButton("OKAY") { _, _ -> }
                    .show()

                return@setOnClickListener
            } else {

                val checkedReason =
                    reasonRadioGroup.findViewById<RadioButton>(reasonRadioGroup.checkedRadioButtonId).text.toString()
                val checkedReasonId =
                    reasonRadioGroup.findViewById<RadioButton>(reasonRadioGroup.checkedRadioButtonId).tag.toString()

                if (checkedReasonId == REASON_ID_OTHERS &&
                    reasonEt.text.isNullOrBlank()
                ) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Please fill the reason")
                        .setPositiveButton("Okay") { _, _ -> }
                        .show()

                    return@setOnClickListener
                }

                val reason = if (checkedReasonId == REASON_ID_OTHERS) {
                    reasonEt.text.toString()
                } else {
                    reasonRadioGroup.findViewById<RadioButton>(reasonRadioGroup.checkedRadioButtonId).text.toString()
                }

                viewModel.markDecline(
                    gigId,
                    checkedReasonId,
                    reason,
                    sharedViewModel
                )
            }
        }

        this.noButton.setOnClickListener {
            dismiss()
        }
    }

    private fun showLoadingView() = viewBinding.apply {
        declineGigMainLayout.root.gone()
        errorLayout.gone()
        progressBar.visible()
    }

    private fun showErrorInView(
        error: String
    ) = viewBinding.apply {
        declineGigMainLayout.root.gone()
        progressBar.gone()
        errorLayout.visible()

        errorTV.text = error
    }

    private fun showMainView(
        options: List<DeclineReason>
    ) = viewBinding.apply {
        errorLayout.gone()
        progressBar.gone()
        declineGigMainLayout.root.visible()

        options.toMutableList().apply {
            add(
                DeclineReason(
                    reasonId = REASON_ID_OTHERS,
                    reason = "Others"
                )
            )
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


        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?.apply {
                postDelayed({
                    requestLayout()
                }, 400)
            }
    }
}