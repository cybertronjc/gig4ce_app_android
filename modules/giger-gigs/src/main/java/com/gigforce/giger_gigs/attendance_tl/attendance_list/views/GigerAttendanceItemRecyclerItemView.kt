package com.gigforce.giger_gigs.attendance_tl.attendance_list.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.giger_gigs.attendance_tl.attendance_list.GigerAttendanceUnderManagerViewContract
import com.gigforce.giger_gigs.databinding.RecyclerRowGigerAttendanceBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import dagger.hilt.android.AndroidEntryPoint
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class GigerAttendanceItemRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    private lateinit var viewBinding: RecyclerRowGigerAttendanceBinding
    private var viewData: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData? = null

    lateinit var viewForeground : View
    lateinit var viewBackground : View

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD
    private val paidOnDateFormatter = DateTimeFormatter.ofPattern("dd/LLL/yyyy") //YYYY-MM-DD

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowGigerAttendanceBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        viewForeground = viewBinding.viewForeground
        viewBackground = viewBinding.viewBackground
    }

    override fun bind(data: Any?) {
        viewData = null

        (data as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData?)?.let {
            viewData = it

            viewBinding.userImageIv.loadProfilePicture(
                it.gigerImage,
                it.gigerImage
            )
            viewBinding.gigerNameTextview.text = it.gigerName.capitalize()
            viewBinding.gigerDesignationTextview.text = it.gigerDesignation
            viewBinding.gigerLastActiveDateTextview.text = it.lastActiveText
            viewBinding.markedByTextview.text = it.markedByText
            viewBinding.overallStatusTextview.bind(
                it.status,
                it.statusBackgroundColorCode,
                it.statusTextColorCode
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

    override fun onClick(v: View?) {

        val currentViewData = viewData ?: return
        currentViewData.viewModel.handleEvent(
            GigerAttendanceUnderManagerViewContract.UiEvent.AttendanceItemClicked(
                currentViewData
            )
        )
    }

    fun getGigDataOrThrow() : AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData{
        return  viewData ?: throw IllegalStateException("view data is null")
    }

    fun getViewBinding() : RecyclerRowGigerAttendanceBinding{
        return viewBinding
    }
}
