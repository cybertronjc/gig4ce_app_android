package com.gigforce.giger_gigs.attendance_tl.attendance_details

import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.databinding.FragmentGigerAttendanceDetailsBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GigerAttendanceDetailsFragment : BaseBottomSheetDialogFragment<FragmentGigerAttendanceDetailsBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_giger_attendance_details
) {
    companion object {
        const val TAG = "GigerAttendanceDetailsFragment"
    }

    private val viewModel: GigerAttendanceDetailsViewModel by viewModels()
    private lateinit var gigId: String
    private lateinit var gigAttendanceDetails : AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
            gigAttendanceDetails = it.getParcelable(GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID) ?: return@let
            gigAttendanceDetails = it.getParcelable(GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS) ?: return@let
        }

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        viewModel.setGigerAttendanceReceivedFromPreviousScreen(
            gigId = gigId,
            gigAttendanceInfo = gigAttendanceDetails
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(GigAttendanceConstants.INTENT_EXTRA_GIG_ID, gigId)
//        outState.putParcelable(GigAttendanceConstants.INTENT_EXTRA_GIG_ATTENDANCE_DETAILS, gigAttendanceDetails)
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
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    private fun initView() = viewBinding.apply {

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
                    is GigerAttendanceDetailsViewContract.State.ShowAttendanceDetails -> showAttendanceDetailsOnView(it.attendanceDetails)
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

    private fun startDocumentDownload(
        businessName: String,
        url: String
    ) {
        try {
            val filePathName = FirebaseUtils.extractFilePath(url)

            val downloadRequest = DownloadManager.Request(Uri.parse(url)).run {
                setTitle(filePathName)
                setDescription(businessName)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    filePathName
                )
                setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )
            }

            val downloadManager = requireContext()
                .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(downloadRequest)

            ToastHandler.showToast(
                requireContext(),
                "Saving file in Downloads,check notification...",
                Toast.LENGTH_LONG
            )
        } catch (e: Exception) {
            e.printStackTrace()
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
        attendanceDetails: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
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
        attendanceDetails:  AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewBinding.mainLayout.apply {

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