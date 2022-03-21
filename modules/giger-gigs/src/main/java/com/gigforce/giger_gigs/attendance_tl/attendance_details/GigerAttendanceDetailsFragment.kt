package com.gigforce.giger_gigs.attendance_tl.attendance_details

import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.databinding.FragmentGigerAttendanceDetailsBinding
import com.gigforce.giger_gigs.models.GigAttendanceData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GigerAttendanceDetailsFragment :
    BaseBottomSheetDialogFragment<FragmentGigerAttendanceDetailsBinding>(
        fragmentName = TAG,
        layoutId = R.layout.fragment_giger_attendance_details
    ) {
    companion object {
        const val TAG = "GigerAttendanceDetailsFragment"
    }

    private val viewModel: GigerAttendanceDetailsViewModel by viewModels()
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
        viewModel.setGigerAttendanceReceivedFromPreviousScreen(
            gigId = gigId
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID, gigId)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentGigerAttendanceDetailsBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {

            initView()
            initViewModel()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun initView() = viewBinding.apply {

        this.mainLayout.apply {
            this.callLayout.floatingActionButton.setImageResource(R.drawable.ic_call_phone_pink)
            this.callLayout.textView.text = "Call"

            this.changeTlLayout.floatingActionButton.setImageResource(R.drawable.ic_change_pink)
            this.changeTlLayout.textView.text = "Change TL"

            this.attendanceHistoryLayout.floatingActionButton.setImageResource(R.drawable.ic_calendar_pink)
            this.attendanceHistoryLayout.textView.text = "Att. History"

            this.dropGigerLayout.floatingActionButton.setImageResource(R.drawable.ic_block_pink)
            this.dropGigerLayout.textView.text = "Drop Giger"
        }

        this.mainLayout.activeButton.setOnClickListener {
            viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.ActiveButtonClicked)
        }

        this.mainLayout.inactiveButton.setOnClickListener {
            viewModel.handleEvent(GigerAttendanceDetailsViewContract.UiEvent.InactiveButtonClicked)
        }
    }

    private fun initViewModel() {

        lifecycleScope.launch {

            viewModel.viewState.collect {
                when (it) {
                    is GigerAttendanceDetailsViewContract.State.ErrorInLoadingAttendanceDetails -> errorInLoadingAttendanceDetails(
                        it.error
                    )
                    is GigerAttendanceDetailsViewContract.State.LoadingAttendanceDetails -> showAttendanceLoading()
                    is GigerAttendanceDetailsViewContract.State.ShowAttendanceDetails -> showAttendanceDetailsOnView(
                        it.attendanceDetails
                    )
                }
            }
        }

        lifecycleScope.launch {

            viewModel.viewEffects.collect {
                when (it) {
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenMarkGigerActiveScreen -> TODO()
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenMarkGigerInActiveScreen -> TODO()
                    is GigerAttendanceDetailsViewContract.UiEffect.OpenResolveAttendanceScreen -> TODO()
                }
            }
        }
    }

    private fun showAttendanceLoading() = viewBinding.apply {
        mainLayout.root.gone()
        infoLayout.root.gone()

        shimmerContainer.visible()
        startShimmer(
            this.shimmerContainer,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }

    private fun showAttendanceDetailsOnView(
        attendanceDetails: GigAttendanceData
    ) = viewBinding.apply {

        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()
        infoLayout.root.gone()
        mainLayout.root.visible()

        showInfoOnView(attendanceDetails)
    }

    private fun showInfoOnView(
        attendanceDetails: GigAttendanceData
    ) = viewBinding.mainLayout.apply {

        this.statusView.bind(
            attendanceDetails.status,
            attendanceDetails.statusBackgroundColorCode,
            attendanceDetails.statusTextColorCode,
            false
        )
        this.userImageImageview.loadProfilePicture(
            attendanceDetails.gigerImage,
            attendanceDetails.gigerImage
        )
        this.nameTextview.text = attendanceDetails.gigerName
        this.lastActiveTextview.text = attendanceDetails.lastActiveText

        this.gigerMarkedAttendanceStatus.isVisible = attendanceDetails.showGigerAttendanceLayout
        this.gigerMarkedAttendanceStatus.bind(
            attendanceDetails.status,
            null ,//TODO
            attendanceDetails.hasAttendanceConflict,
            false
        )

        this.infoLayout.bind(
            attendanceDetails
        )

        this.activeButton.isVisible = attendanceDetails.canTLMarkPresent
        this.inactiveButton.isVisible = attendanceDetails.canTLMarkAbsent
    }

    private fun errorInLoadingAttendanceDetails(
        error: String
    ) = viewBinding.apply {
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        infoLayout.root.visible()
        infoLayout.infoMessageTv.text = error
    }

}