package com.gigforce.giger_gigs.attendance_tl.attendance_list.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.gigforce.core.IViewHolder
import com.gigforce.giger_gigs.R
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

    lateinit var viewForeground: View
    lateinit var viewBackground: View

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD
    private val paidOnDateFormatter = DateTimeFormatter.ofPattern("dd/LLL/yyyy") //YYYY-MM-DD

    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
        viewBinding.gigerAttendanceStatusView.setOnResolveButtonClickListener(this)
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


            if(it.currentlyMarkingAttendanceForThisGig){
                viewBinding.gigerAttendanceStatusView.isVisible = it.showGigerAttendanceLayout

                if(it.hasAttendanceConflict){

                    viewBinding.overallStatusTextview.bind(
                        it.status,
                        it.statusBackgroundColorCode,
                        it.statusTextColorCode,
                        true
                    )

                    viewBinding.gigerAttendanceStatusView.bind(
                        status = it.gigerAttendanceStatus ?: "Pending",
                        markingTime = null,
                        showResolveButton = it.hasAttendanceConflict,
                        true
                    )
                } else{

                    viewBinding.overallStatusTextview.bind(
                        it.status,
                        it.statusBackgroundColorCode,
                        it.statusTextColorCode,
                        true
                    )

                    viewBinding.gigerAttendanceStatusView.bind(
                        status = it.gigerAttendanceStatus ?: "Pending",
                        markingTime = null,
                        showResolveButton = it.hasAttendanceConflict,
                        true
                    )
                }

            } else {

                viewBinding.overallStatusTextview.bind(
                    it.status,
                    it.statusBackgroundColorCode,
                    it.statusTextColorCode,
                    false
                )

                viewBinding.gigerAttendanceStatusView.isVisible = it.showGigerAttendanceLayout
                viewBinding.gigerAttendanceStatusView.bind(
                    status = it.gigerAttendanceStatus ?: "Pending",
                    markingTime = null,
                    showResolveButton = it.hasAttendanceConflict,
                    false
                )
            }
        }
    }

    override fun onClick(v: View?) {

        val currentViewData = viewData ?: return
        when (v?.id) {
            R.id.resolve_btn -> currentViewData.viewModel.handleEvent(
                GigerAttendanceUnderManagerViewContract.UiEvent.AttendanceItemResolveClicked(
                    currentViewData
                )
            )
            else -> currentViewData.viewModel.handleEvent(
                GigerAttendanceUnderManagerViewContract.UiEvent.AttendanceItemClicked(
                    currentViewData
                )
            )
        }
    }

    fun getGigDataOrThrow(): AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData {
        return viewData ?: throw IllegalStateException("view data is null")
    }

    fun getViewBinding(): RecyclerRowGigerAttendanceBinding {
        return viewBinding
    }
}
