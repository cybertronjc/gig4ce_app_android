package com.gigforce.common_ui.molecules

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.util.TextDrawable
import com.gigforce.common_ui.viewdatamodels.GigContactDetails
import com.gigforce.common_ui.viewdatamodels.GigInfoCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class GigInfoCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {
    val itemWidth = ((width / 5) * 4).toInt()

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.gig_info_card, this, true)
        val lp = this.findViewById<CardView>(R.id.card_view).layoutParams
        lp.height = lp.height
        lp.width = itemWidth
        this.findViewById<CardView>(R.id.card_view).layoutParams = lp
    }


    fun setTitle(title: String) {
        this.findViewById<TextView>(R.id.textView41).text = title
    }

//    fun setContactPerson() {
////        getTextView(viewHolder, R.id.contactPersonTV).text =
////            data?.gigContactDetails?.contactName
//    }

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    private fun setGigDate(data: GigInfoCardDVM) {
        if (data.isGigOfToday()) {
            val gigTiming = if (data.endDateTime != null)
                "${DateHelper.getHourMinutes(data.startDateTime!!.toDate())} - ${
                DateHelper.getHourMinutes(
                    data.endDateTime!!.toDate()
                )
                }"
            else
                "${DateHelper.getHourMinutes(data.startDateTime!!.toDate())}"
            this.findViewById<TextView>(R.id.textView67).text = gigTiming
            this.findViewById<View>(R.id.checkInTV).setOnClickListener {
//                navigation.navigateTo(
//                    "attendance",
//                    Bundle().apply {
//                        this.putString(INTENT_EXTRA_GIG_ID, data.gigId)
//                    }
//                )
            }
            if (!data.isPresentGig()) {
                this.findViewById<View>(R.id.checkInTV).isEnabled = false
            } else if (data.isCheckInAndCheckOutMarked()) {
                this.findViewById<View>(R.id.checkInTV).isEnabled = false
                this.findViewById<Button>(R.id.checkInTV).text = "Checked Out"
            } else if (data.isCheckInMarked()) {
                this.findViewById<Button>(R.id.checkInTV).isEnabled = true
                this.findViewById<Button>(R.id.checkInTV).text = "Checked Out"
            } else {
                this.findViewById<Button>(R.id.checkInTV).isEnabled = true
                this.findViewById<Button>(R.id.checkInTV).text = "Checked In"
            }

        } else {
            findViewById<View>(R.id.checkInTV).isEnabled = false
            findViewById<TextView>(R.id.textView67).text =
                DateHelper.getDateInDDMMYYYY(data.startDateTime!!.toDate())
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
                findViewById<View>(R.id.callCardView).visibility = View.GONE
            } else {
                findViewById<View>(R.id.callCardView).visible()
                findViewById<View>(R.id.callCardView).setOnClickListener { _ ->
                    callManager(it.contactNumberString)
                }
                this.findViewById<TextView>(R.id.contactPersonTV).text = it.toString()
            }
        } ?: let { findViewById<View>(R.id.callCardView).visibility = View.GONE }
    }
//    @Inject
//    lateinit var navigation: INavigation

    override fun bind(data: Any?) {
        if (data is GigInfoCardDVM) {
            setTitle(data.title)
            setContactPerson(data.gigContactDetails)
            setGigDate(data)
            setCompanyLogo(data)


        }
    }

    private fun setCompanyLogo(data: GigInfoCardDVM) {
        val companyLogoIV = this.findViewById<ImageView>(R.id.companyLogoIV)

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
            val drawable = TextDrawable.builder().buildRound(
                companyInitials,
                ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )

            companyLogoIV.setImageDrawable(drawable)
        }
    }


}