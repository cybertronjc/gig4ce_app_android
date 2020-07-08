package com.gigforce.app.modules.gigPage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigDetails
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.*
import java.text.SimpleDateFormat


class GigAttendancePageFragment : BaseFragment() {

    private val viewModel: GigViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =  inflateView(R.layout.fragment_gig_page_attendance,inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel(savedInstanceState)
    }

    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                setGigDetailsOnView(it)
            })

        val gigId = if (savedInstanceState != null) {
            savedInstanceState.getString(PresentGigPageFragment.INTENT_EXTRA_GIG_ID)
        } else {
            arguments?.getString(PresentGigPageFragment.INTENT_EXTRA_GIG_ID)
        }

        viewModel.getPresentGig("some")
    }

    private fun setGigDetailsOnView(gig: Gig) {
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = gig.gigId

        setGigDetails(gig.gigDetails)




        if (gig.gigLocationDetails != null) {
  //          fullMapAddresTV.text = gig.gigLocationDetails?.fullAddress
//            addMarkerOnMap(
//                latitude = gig.gigLocationDetails!!.latitude!!,
//                longitude = gig.gigLocationDetails!!.longitude!!
//            )
        } else {
            //make location layout invisivle
        }
    }




    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")

    @SuppressLint("SetTextI18n")
    private fun setGigDetails(gigDetails: GigDetails) {
        durationTextTV.text =
            "${dateFormatter.format(gigDetails.startTime)} - ${dateFormatter.format(gigDetails.endTime)}"
        shiftTV.text = gigDetails.shiftDuration
        addressTV.text = gigDetails.address
        wageTV.text = gigDetails.wage
    }


}