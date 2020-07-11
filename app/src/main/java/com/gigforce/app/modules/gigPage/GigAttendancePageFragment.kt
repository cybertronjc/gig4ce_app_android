package com.gigforce.app.modules.gigPage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_gig_page_attendance.*
import java.text.SimpleDateFormat


class GigAttendancePageFragment : BaseFragment() {

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    private val viewModel: GigViewModel by viewModels()

    private lateinit var gigId: String
    private var gig: Gig? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_attendance, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initView()
        initViewModel(savedInstanceState)
    }

    private fun initView() {
        cross_btn.setOnClickListener { activity?.onBackPressed() }

        callCardView.setOnClickListener {

            if (gig?.contactNo != null) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", gig!!.contactNo, null))
                startActivity(intent)
            }
        }
    }

    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {
        gigId = if (savedInstanceState != null) {
            savedInstanceState.getString(INTENT_EXTRA_GIG_ID)!!
        } else {
            arguments?.getString(INTENT_EXTRA_GIG_ID)!!
        }
    }

    private fun initViewModel(savedInstanceState: Bundle?) {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                    }
                    is Lce.Content -> setGigDetailsOnView(it.content)
                    is Lce.Error -> {
                    }
                }
            })

        viewModel.watchGig(gigId)
    }

    private fun setGigDetailsOnView(gig: Gig) {
        this.gig = gig
        roleNameTV.text = gig.title
        companyNameTV.text = "@ ${gig.companyName}"
        gigTypeTV.text = gig.gigType
        gigIdTV.text = gig.gigId

        if (gig.companyLogo != null) {
            FirebaseStorage.getInstance()
                .getReference("folder")
                .child(gig.companyLogo!!)
                .downloadUrl
                .addOnSuccessListener {
                    Glide.with(requireContext())
                        .load(it)
                        .into(companyLogoIV)
                }
        }

        if (gig.endDateTime != null)
            durationTextTV.text =
                "${dateFormatter.format(gig.startDateTime!!.toDate())} - ${dateFormatter.format(gig.endDateTime!!.toDate())}"
        else
            durationTextTV.text = "${dateFormatter.format(gig.startDateTime!!.toDate())} - "

        shiftTV.text = "${gig.duration} per Day "
        addressTV.text = gig.address
        wageTV.text = "${gig.gigAmount} per Day "

        contactPersonTV.text = gig.gigContactDetails?.contactName

        if (gig.gigLocationDetails?.latitude != null) {
            addressTV.text = prepareAddress(gig.address)
        } else {
            addressTV.text = gig.address
        }

        if (gig.attendance != null) {

            if (gig.attendance!!.checkOutMarked) {

            } else if (gig.attendance!!.checkInMarked) {

            }
        }
    }

    private fun prepareAddress(address: String): SpannableString {
        if (address.isBlank())
            return SpannableString("")

        val string = SpannableString(address + PresentGigPageFragment.TEXT_VIEW_ON_MAP)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                //Launch Map
                val lat = gig?.gigLocationDetails?.latitude
                val long = gig?.gigLocationDetails?.longitude

                val uri = "http://maps.google.com/maps?q=loc:$lat,$long (Gig Location)"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                requireContext().startActivity(intent)
            }
        }

        string.setSpan(clickableSpan, address.length + 1, string.length - 1, 0)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.lipstick, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), address.length + 1, string.length - 1, 0)
        return string
    }


    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")


}