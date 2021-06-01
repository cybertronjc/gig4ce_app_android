package com.gigforce.common_ui.cells

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.IViewHolder
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigContactDetails
import com.gigforce.core.date.DateHelper.getHourMinutes
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.upcoming_gig_card_component.view.*
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingGigCardComponent(context: Context, attrs: AttributeSet?) :  FrameLayout(context, attrs),
        IViewHolder{

@Inject
    lateinit var navigation: INavigation
    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }


    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.upcoming_gig_card_component, this, true)

    }

    fun setTitle(title: String){
       textView41.text = title
    }

    private fun setGigDate(data: Gig) {

        if (data.isGigOfToday()) {

            val gigTiming = if (data.endDateTime != null)
                "${getHourMinutes(data.startDateTime!!.toDate())} - ${
                    getHourMinutes(
                        data.endDateTime!!.toDate()
                    )
                }"
            else
                "${getHourMinutes(data.startDateTime!!.toDate())}"
            textView67.text = gigTiming

        } else {
            val date = DateHelper.getDateInDDMMYYYY(data.startDateTime.toDate())
            textView67.text = date
        }

        val gigStatus = GigStatus.fromGig(data)
        when (gigStatus) {
            GigStatus.UPCOMING,
            GigStatus.DECLINED,
            GigStatus.CANCELLED,
            GigStatus.COMPLETED,
            GigStatus.MISSED -> {

                checkInTV.isEnabled = false
                checkInTV.text = "Check In"
            }
            GigStatus.ONGOING,
            GigStatus.PENDING,
            GigStatus.NO_SHOW -> {

            checkInTV.setOnClickListener {}

            if (!data.isPresentGig()) {
                checkInTV.isEnabled = false
            }
            else if (data.isCheckInAndCheckOutMarked()) {
                checkInTV.isEnabled = false
                checkInTV.text =
                    "Checked Out"
            } else if (data.isCheckInMarked()) {
                checkInTV.isEnabled = true
                checkInTV.text =
                    context.getString(R.string.check_out)
            } else {
                checkInTV.isEnabled = true
                checkInTV.text =
                    context.getString(R.string.check_in)
            }
            }
        }
    }

    fun callManager(number: String) {
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", number, null)
        )
        context.startActivity(intent)
    }

    private fun setContactPerson(gigContactDetails: GigContactDetails?) {
        gigContactDetails?.let {

            if (it.contactNumberString.isNullOrBlank()) {
                callCardView.visibility = View.GONE
            } else {
                callCardView.visible()
                callCardView.setOnClickListener { _ ->
                    callManager(it.contactNumberString)
                }
                contactPersonTV.text = it.contactNumberString
            }
        } ?: let { callCardView.visibility = View.GONE }
    }

    private fun setMessagePerson(obj: Gig){

        contactPersonTV.text = if (obj.openNewGig())
            obj.agencyContact?.name
        else
            obj.gigContactDetails?.contactName

        if (obj.openNewGig() && obj.agencyContact?.uid != null) {

            messageCardView.visible()
            messageCardView.setOnClickListener {
            }

        } else if (obj.gigContactDetails != null && obj.gigContactDetails?.contactNumber != null) {
            if (obj.chatInfo?.isNullOrEmpty() == false) {
                messageCardView.visible()
            } else {
                messageCardView.gone()
            }
        } else {
            messageCardView.gone()
        }
    }

    private fun setCompanyLogo(data: Gig) {
        if (!data.companyLogo.isNullOrBlank()) {

            if (data.companyLogo!!.startsWith("http", true)) {

                Glide.with(context)
                    .load(data.companyLogo)
                    .into(companyLogoIV)

            } else {
                FirebaseStorage.getInstance()
                    .getReference("companies_gigs_images")
                    .child(data.companyLogo!!)
                    .downloadUrl
                    .addOnSuccessListener {

                        Glide.with(context)
                            .load(it)
                            .into(companyLogoIV)
                    }
            }
        } else {
            val companyInitials = if (data.companyName.isNullOrBlank())
                "C"
            else
                data.companyName!![0].toString().toUpperCase()
            val drawable = com.gigforce.common_ui.utils.TextDrawable.builder().buildRound(
                companyInitials,
                ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )

            companyLogoIV.setImageDrawable(drawable)
        }
    }

    override fun bind(obj: Any?){
        if (obj is Gig){

            //set title
            setTitle(obj.title)

            //set contact
            setContactPerson(obj.gigContactDetails)

            //set message contact
            setMessagePerson(obj)

            //set gig date
            setGigDate(obj)

            //set company logo
            setCompanyLogo(obj)
        }
        }
    }
