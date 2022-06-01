package com.gigforce.common_ui.components.cells

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.gigforce.app.navigation.gigs.GigNavigation
import com.gigforce.common_ui.R
import com.gigforce.common_ui.utils.TextDrawable
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.core.utils.DateHelper
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingGigCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {

    @Inject
    lateinit var gigNavigation: GigNavigation

    @Inject
    lateinit var navigation: INavigation

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    @Inject
    lateinit var eventTracker: IEventTracker

    private val timeFormatter = SimpleDateFormat("hh.mm aa")

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.upcoming_gig_card_component, this, true)

    }

    fun setTitle(title: String) {
        this.findViewById<TextView>(R.id.textView41).text = title
    }

    fun callManager(number: String?) {
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

    val cardView = this.findViewById<View>(R.id.card_view)
    val ivContact = this.findViewById<ImageView>(R.id.iv_call)
    val iv_message = this.findViewById<ImageView>(R.id.iv_message)
    val messageCardView = this.findViewById<View>(R.id.messageCardView)

    val textView41 = this.findViewById<TextView>(R.id.textView41)
    val contactPersonTV = this.findViewById<TextView>(R.id.contactPersonTV)
    val checkInTV = this.findViewById<Button>(R.id.checkInTV)

    val callView = this.findViewById<View>(R.id.callCardView)

    val navigateTV = this.findViewById<View>(R.id.navigateTV)

    val companyLogoIV = this.findViewById<GigforceImageView>(R.id.companyLogoIV)

    val textView67 = this.findViewById<TextView>(R.id.textView67)
    var checkInClickListener: AdapterClickListener<Any>? = null

    override fun bind(obj: Any?) {
        if (obj is Gig) {
            cardView.setOnClickListener {
                gigNavigation.openGigPage(obj.gigId)
            }
            ivContact.setImageResource(R.drawable.ic_phone_white_24dp)
            ivContact.setColorFilter(
                ContextCompat.getColor(
                    this.context,
                    R.color.lipstick
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )

            iv_message.setImageResource(R.drawable.ic_chat)

            if(obj.agencyContact != null ) {
                if (obj.agencyContact?.uid != null) {

                    messageCardView.visible()
                    messageCardView.setOnClickListener {
                        navigation.navigateTo(
                            "chats/chatPage", bundleOf(
                                AppConstants.INTENT_EXTRA_CHAT_TYPE to AppConstants.CHAT_TYPE_USER,
                                AppConstants.INTENT_EXTRA_OTHER_USER_ID to obj.agencyContact?.uid,
                                AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to obj.agencyContact?.profilePicture,
                                AppConstants.INTENT_EXTRA_OTHER_USER_NAME to obj.agencyContact?.name
                            )
                        )
                        navigation.navigateTo("chat")
                    }
                } else {
                    messageCardView.gone()
                }


                if (!obj.agencyContact?.contactNumber.isNullOrEmpty()) {

                    callView.visible()
                    callView.setOnClickListener {
                        callManager(obj?.agencyContact?.contactNumber)
                        if (obj.gigContactDetails?.contactNumber != null &&
                            obj.gigContactDetails?.contactNumber != 0L
                        ) {
                            callManager(obj.gigContactDetails?.contactNumber.toString())

                        } else if (!obj.agencyContact?.contactNumber.isNullOrEmpty()) {
                            callManager(obj.agencyContact?.contactNumber)
                        }
                    }
                } else {
                    callView.gone()
                }
            } else{
                messageCardView.gone()
                callView.gone()
            }

            textView41.text = obj.getGigTitle()
            contactPersonTV.text = obj.agencyContact?.name

            val gigStatus = GigStatus.fromGig(obj as Gig)
            when (gigStatus) {
                GigStatus.UPCOMING,
                GigStatus.DECLINED,
                GigStatus.CANCELLED,
                GigStatus.COMPLETED,
                GigStatus.MISSED -> {

                    checkInTV.isEnabled = false
                    checkInTV.text = context.getString(R.string.check_in_common_ui)
                }
                GigStatus.ONGOING,
                GigStatus.PENDING,
                GigStatus.NO_SHOW -> {

                    checkInTV.setOnClickListener {
                        navigation.navigateTo(
                            "gig/attendance", bundleOf(
                                AppConstants.INTENT_EXTRA_GIG_ID to obj.gigId
                            )
                        )
                    }

                    if (obj.isCheckInAndCheckOutMarked()) {
                        checkInTV.isEnabled = false
                        checkInTV.text =
                            context.getString(R.string.checked_out)
                    } else if (obj.isCheckInMarked()) {
                        checkInTV.isEnabled = true
                        checkInTV.text =
                            context.getString(R.string.check_out_common_ui)
                    } else {
                        checkInTV.isEnabled = true
                        checkInTV.text =
                            context.getString(R.string.check_in_common_ui)
                    }
                }
            }

            if (obj.isGigOfToday()) {

                textView67.text = buildString {
                    append(timeFormatter.format(obj.startDateTime.toDate()))
                    append(" - ")
                    append(timeFormatter.format(obj.endDateTime.toDate()))
                }
            } else {
                val date = DateHelper.getDateInDDMMYYYY(obj.startDateTime.toDate())
                textView67.text = date
            }


            if (!obj.getFullCompanyLogo().isNullOrBlank()) {

                companyLogoIV.loadImageIfUrlElseTryFirebaseStorage(
                    obj.getFullCompanyLogo()!!
                )
            } else {
                val companyInitials = if (obj.getFullCompanyName().isNullOrBlank())
                    "C"
                else
                    obj.getFullCompanyName()!![0].toString().toUpperCase()
                val drawable = TextDrawable.builder().buildRound(
                    companyInitials,
                    ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                )

                companyLogoIV.setImageDrawable(drawable)
            }
        }
    }
}


