package com.gigforce.app.tl_work_space.payout.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.RecyclerViewGigerPayoutGigerItemViewBinding
import com.gigforce.app.tl_work_space.payout.GigerPayoutFragmentViewEvents
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutScreenData
import com.gigforce.common_ui.ext.formatToCurrency
import com.gigforce.core.IViewHolder
import java.time.format.DateTimeFormatter

class GigerPayoutGigerItemView (
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: RecyclerViewGigerPayoutGigerItemViewBinding
    private var viewData: GigerPayoutScreenData.GigerItemData? = null

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD
    private val paidOnDateFormatter = DateTimeFormatter.ofPattern("dd/LLL/yyyy") //YYYY-MM-DD

    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)

        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerViewGigerPayoutGigerItemViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
        //viewBinding.callGigerBtn.setOnClickListener(this)
    }

    override fun bind(data: Any?) {
        (data as GigerPayoutScreenData.GigerItemData?)?.let {
            viewData = it

            viewBinding.gigerImage.loadProfilePicture(
                it.profilePictureThumbnail,
                it.profilePicture
            )
            viewBinding.gigerName.text = it.gigerName
            viewBinding.amount.text = it.amount.formatToCurrency()
            viewBinding.payoutStatusView.bind(
                it.statusString,
                it.statusColorCode
            )
            viewBinding.paidOnTextview.text = formatPaymentDate(it.paymentDate)
//            viewBinding.gigerDesignationTextview.text = getCompanyDesignationString(
//                it.business,
//                it.jobProfile
//            )
        }
    }


    override fun onClick(v: View?) {
        if (v?.id == R.id.call_giger_btn) {

            viewData?.viewModel?.setEvent(
                GigerPayoutFragmentViewEvents.CallGigerClicked(
                    viewData!!
                )
            )
        } else {

            viewData?.viewModel?.setEvent(
                GigerPayoutFragmentViewEvents.GigerClicked(
                    viewData!!
                )
            )
        }
    }

    private fun formatPaymentDate(
        paymentDate: String?
    ): String {
        return if (paymentDate != null) {
            "Paid on : ${paidOnDateFormatter.format(isoDateFormatter.parse(paymentDate))}"
        } else {
            "Paid on : -"
        }
    }

}