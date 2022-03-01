package com.gigforce.wallet.payouts.payout_details

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewmodels.payouts.Payout
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.wallet.PayoutConstants
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.PayoutDetailsFragmentBinding
import com.toastfix.toastcompatwrapper.ToastHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PayoutDetailsFragment : BaseBottomSheetDialogFragment<PayoutDetailsFragmentBinding>(
    fragmentName = TAG,
    layoutId = R.layout.payout_details_fragment
) {
    companion object {
        const val TAG = "PayoutDetailsFragment"
    }

    private val viewModel: PayoutDetailsViewModel by viewModels()

    private lateinit var payoutId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            payoutId = it.getString(PayoutConstants.INTENT_EXTRA_PAYOUT_ID) ?: return@let
        }

        savedInstanceState?.let {
            payoutId = it.getString(PayoutConstants.INTENT_EXTRA_PAYOUT_ID) ?: return@let
        }

        viewModel.setPayoutReceivedFromPreviousScreen(
            payoutId = payoutId
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PayoutConstants.INTENT_EXTRA_PAYOUT_ID, payoutId)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: PayoutDetailsFragmentBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {

            initView()
            initViewModel()
        }
    }

    private fun initView() = viewBinding.apply {
        this.mainLayout.callHelpLineButton.setOnClickListener {
            viewModel.handleEvent(PayoutDetailsContract.UiEvent.CallHelpLineClicked)
        }

        this.mainLayout.downloadPayoutSlipButton.setOnClickListener {
            viewModel.handleEvent(PayoutDetailsContract.UiEvent.DownloadPayoutPDFClicked)
        }
    }

    private fun initViewModel() {

        lifecycleScope.launch {

            viewModel.viewState.collect {
                when (it) {
                    is PayoutDetailsContract.State.ErrorInLoadingPayoutDetails -> errorInLoadingPayoutDetails(
                        it.error
                    )
                    is PayoutDetailsContract.State.LoadingPayoutDetails -> showPayoutLoading()
                    is PayoutDetailsContract.State.ShowPayoutDetails -> showPayoutDetailsOnView(it.payout)
                }
            }
        }

        lifecycleScope.launch {

            viewModel.viewEffects.collect {
                when (it) {
                    is PayoutDetailsContract.UiEffect.CallHelpLineNo -> callPhoneNumber(it.phoneNumber)
                    is PayoutDetailsContract.UiEffect.StartPayoutDocumentDownload -> startDocumentDownload(
                        it.businessName,
                        it.url
                    )
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

    private fun showPayoutLoading() = viewBinding.apply {
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

    private fun showPayoutDetailsOnView(
        payout: Payout
    ) = viewBinding.apply {

        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()
        infoLayout.root.gone()
        mainLayout.root.visible()

        showInfoOnView(payout)
    }

    private fun showInfoOnView(
        payout: Payout
    ) = viewBinding.mainLayout.apply {

        businessNameTextview.text = payout.businessName

        if (payout.businessIcon != null) {
            this.businessLogoImageview.loadImageIfUrlElseTryFirebaseStorage(
                payout.businessIcon!!
            )
        } else {
            val businessInitials: String = if (payout.businessName != null) {
                payout.businessName!![0].uppercaseChar().toString()
            } else {
                "C"
            }
            val drawable = TextDrawable.builder().buildRound(
                businessInitials,
                ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )
            this.businessLogoImageview.setImageDrawable(drawable)
        }

        this.payoutStatusView.bind(
            payout.status ?: "-",
            payout.statusColorCode ?: "#ffffff"
        )
        this.infoLayout.bind(payout)

        //Bank and ifsc info
        this.accountNoLayout.titleTextView.text = "Account No."
        this.accountNoLayout.valueTextView.text = ": ${payout.accountNo ?: "-"}"

        this.ifscLayout.titleTextView.text = "IFSC Code."
        this.ifscLayout.valueTextView.text = ": ${payout.isfc ?: "-"}"

        this.remarksTextview.text = payout.remarks
        this.downloadPayoutSlipButton.isVisible = !payout.payoutDocumentUrl.isNullOrBlank()
    }

    private fun errorInLoadingPayoutDetails(
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

    private fun callPhoneNumber(
        phoneNumber: String
    ) {

        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}