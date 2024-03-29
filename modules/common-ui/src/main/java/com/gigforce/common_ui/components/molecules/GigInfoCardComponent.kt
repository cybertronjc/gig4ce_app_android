package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.utils.TextDrawable
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.date.DateUtil
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GigInfoCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {


    @Inject lateinit var navigation: INavigation
    @Inject
    lateinit var eventTracker: IEventTracker
    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.gig_info_card, this, true)
//        val lp = this.findViewById<CardView>(R.id.card_view).layoutParams
//        val displayMetrics = DisplayMetrics()
//        context.applicationContext?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//        val width = displayMetrics.widthPixels
//        val itemWidth = ((width / 5) * 4).toInt()
//        lp.height = lp.height
//        lp.width = itemWidth
//        this.findViewById<CardView>(R.id.card_view).layoutParams = lp
    }


    fun setTitle(title: String) {
        this.findViewById<TextView>(R.id.textView41).text = title
    }

    private fun setGigDate(data: Gig) {
        if (data.isGigOfToday()) {
            val gigTiming = if (data.endDateTime != null)
                "${DateUtil.getHourMinutes(data.startDateTime!!.toDate())} - ${
                DateUtil.getHourMinutes(
                    data.endDateTime!!.toDate()
                )
                }"
            else
                "${DateUtil.getHourMinutes(data.startDateTime!!.toDate())}"
            this.findViewById<TextView>(R.id.textView67).text = gigTiming
            this.findViewById<View>(R.id.checkInTV).setOnClickListener {
            }
            if (!data.isPresentGig()) {
                this.findViewById<View>(R.id.checkInTV).isEnabled = false
            } else if (data.isCheckInAndCheckOutMarked()) {
                this.findViewById<View>(R.id.checkInTV).isEnabled = false
                this.findViewById<Button>(R.id.checkInTV).text = context.getString(R.string.checkedout_common_ui)
            } else if (data.isCheckInMarked()) {
                this.findViewById<Button>(R.id.checkInTV).isEnabled = true
                this.findViewById<Button>(R.id.checkInTV).text = context.getString(R.string.checkedout_common_ui)
            } else {
                this.findViewById<Button>(R.id.checkInTV).isEnabled = true
                this.findViewById<Button>(R.id.checkInTV).text = context.getString(R.string.check_common_ui)
            }

        } else {
            findViewById<View>(R.id.checkInTV).isEnabled = false
            findViewById<TextView>(R.id.textView67).text =
                DateUtil.getDateInDDMMYYYY(data.startDateTime!!.toDate())
        }
    }

    fun callManager(number: String) {
        eventTracker.pushEvent(
            TrackingEventArgs(
                "giger_call_tl", null
            )
        )
        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.fromParts("tel", number, null)
        )
        context.startActivity(intent)
    }
    private fun setContactPerson(gigContactDetails: com.gigforce.core.datamodels.gigpage.GigContactDetails?) {
        gigContactDetails?.let {
            if (it.contactNumberString.isNullOrBlank()) {
                findViewById<View>(R.id.callCardView).visibility = View.GONE
            } else {
                findViewById<View>(R.id.callCardView).visible()
                findViewById<View>(R.id.callCardView).setOnClickListener { _ ->
                    callManager(it.contactNumberString)
                }
                this.findViewById<TextView>(R.id.contactPersonTV).text = it.contactNumberString
            }
        } ?: let { findViewById<View>(R.id.callCardView).visibility = View.GONE }
    }


    override fun bind(data: Any?) {
        if (data is Gig) {
            Log.d("dataHere", data.toString())
            data.profile.title?.let { setTitle(it) }
            setContactPerson(data.gigContactDetails)
            setGigDate(data)
            setCompanyLogo(data)


        }
    }

    private fun setCompanyLogo(data: Gig) {
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