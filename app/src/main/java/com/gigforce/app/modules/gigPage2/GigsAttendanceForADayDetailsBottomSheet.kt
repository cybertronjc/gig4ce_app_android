package com.gigforce.app.modules.gigPage2

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.GigViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_single_day_attendance_details.*
import kotlinx.android.synthetic.main.fragment_gig_single_day_attendance_details.gig_status_iv
import kotlinx.android.synthetic.main.fragment_gig_single_day_attendance_details.gig_status_tv
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import java.util.concurrent.TimeUnit


class GigsAttendanceForADayDetailsBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String

    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_single_day_attendance_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {

        arguments?.let {
            gigId = it.getString(INTENT_GIG_ID)  ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(INTENT_GIG_ID) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_GIG_ID, gigId)
    }

    private fun initView() {

        regularisation_text.setOnClickListener {
            findNavController().navigate(R.id.gigRegulariseAttendanceFragment, bundleOf(
                GigRegulariseAttendanceFragment.INTENT_EXTRA_GIG_ID to gigId
            ))
        }
    }

    private fun initViewModel() {
        viewModel
            .gigDetails
            .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lce.Loading -> {

                        }
                        is Lce.Content -> showGigDetails(it.content)
                        is Lce.Error -> {

                        }
                    }
                })

        viewModel.getGig(gigId)
    }

    private fun showGigDetails(gig: Gig) {
        val gigStartDateTime = gig.startDateTime!!.toLocalDateTime()

        val dayName = gigStartDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        att_date_day.text = dayName +"\n" + gigStartDateTime.dayOfMonth.toString()


        if(gig.isPastGig()){
            gig_status_tv.text = "Completed"
            gig_status_iv.setImageResource(R.drawable.round_pink)
        } else if(gig.isPresentGig()){
            gig_status_tv.text = "Ongoing"
            gig_status_iv.setImageResource(R.drawable.round_green)
        } else if(gig.isUpcomingGig()){
            gig_status_tv.text = "Upcoming"
            gig_status_iv.setImageResource(R.drawable.round_yellow)
        }

        if(gig.attendance != null){

            if(gig.attendance!!.checkInTime != null){
                punch_in_time.text = "Punch In\n${ timeFormatter.format(gig.attendance!!.checkInTime)}"
            } else{
                gig_timer_tv.text = "00 : 00 mins"
                punch_in_time.text = "Punch In\n--:--"

                regularisation_text.text = "Looks like you forgot to mark your attendance. Regularise"
            }

            if(gig.attendance!!.checkOutTime != null){
                punch_out_time.text ="Punch Out\n${ timeFormatter.format(gig.attendance!!.checkOutTime)}"

                val gigStartDateTime = gig.attendance!!.checkInTime!!
                val gigEndTime = gig.attendance!!.checkOutTime!!

                val diffInMillisec: Long = gigEndTime.time - gigStartDateTime.time
                val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
                val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60

                gig_timer_tv.text = "$diffInHours : $diffInMin mins"
            } else{
                gig_timer_tv.text = "00 : 00 mins"
                punch_out_time.text = "Punch Out\n--:--"

                regularisation_text.text = "Looks like you forgot to mark your attendance. Regularise"
            }
        } else{
            gig_timer_tv.text = "00 : 00 mins"

            punch_in_time.text = "Punch In\n--:--"
            punch_out_time.text = "Punch Out\n--:--"

            regularisation_text.text = "Looks like you forgot to mark your attendance. Regularise"
        }

        if(gig.gigRating != 0.0f){
            company_rating_tv.text = "--"
        } else{
            company_rating_tv.text = gig.gigRating.toString()
        }
    }


    companion object {
        const val INTENT_GIG_ID = "gig_id"
    }
}