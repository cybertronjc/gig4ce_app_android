package com.gigforce.common_ui.components.cells

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
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.utils.TextDrawable
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.AppConstants
import com.gigforce.core.IViewHolder
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
class UpcomingGigCardComponent(context: Context, attrs: AttributeSet?) :  FrameLayout(context, attrs),
        IViewHolder{

@Inject
    lateinit var navigation: INavigation
    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

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

    val companyLogoIV = this.findViewById<ImageView>(R.id.companyLogoIV)

    val textView67 = this.findViewById<TextView>(R.id.textView67)
    var checkInClickListener: AdapterClickListener<Any>? = null

    override fun bind(obj: Any?) {
        if (obj is Gig) {
            ivContact.setImageResource(R.drawable.ic_phone_white_24dp)
            ivContact.setColorFilter(
                ContextCompat.getColor(
                    this.context,
                    R.color.lipstick
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )

            iv_message.setImageResource(R.drawable.ic_chat)

            if (obj.openNewGig() && obj.agencyContact?.uid != null) {

                messageCardView.visible()
                messageCardView.setOnClickListener {
                       navigation.navigateTo("chats/chatPage", bundleOf(
                            AppConstants.INTENT_EXTRA_CHAT_TYPE to AppConstants.CHAT_TYPE_USER,
                            AppConstants.INTENT_EXTRA_OTHER_USER_ID to obj.agencyContact?.uid,
                            AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE to obj.agencyContact?.profilePicture,
                            AppConstants.INTENT_EXTRA_OTHER_USER_NAME to obj.agencyContact?.name))
                    navigation.navigateTo("chat")
                }

            } else if (obj.gigContactDetails != null && obj.gigContactDetails?.contactNumber != null) {
                if (obj.chatInfo?.isNullOrEmpty() == false) {
                    messageCardView.visible()
                    messageCardView.setOnClickListener {
                        val bundle = Bundle()
                        val map = obj.chatInfo
                        bundle.putString(
                                AppConstants.INTENT_EXTRA_OTHER_USER_IMAGE,
                            AppConstants.IMAGE_URL
                        )
                        bundle.putString(
                                AppConstants.INTENT_EXTRA_OTHER_USER_NAME,
                            AppConstants.CONTACT_NAME
                        )
                        bundle.putString(
                                AppConstants.INTENT_EXTRA_CHAT_TYPE,
                            ChatConstants.CHAT_TYPE_USER
                        )

                        bundle.putString(
                                AppConstants.INTENT_EXTRA_CHAT_HEADER_ID,
                            map?.get("chatHeaderId") as String
                        )
                        bundle.putString(
                                AppConstants.INTENT_EXTRA_OTHER_USER_ID,
                            map?.get("otherUserId") as String
                        )
                        bundle.putString(
                            StringConstants.MOBILE_NUMBER.value,
                            map?.get(StringConstants.MOBILE_NUMBER.value) as String
                        )
                        bundle.putBoolean(
                            StringConstants.FROM_CLIENT_ACTIVATON.value,
                            map?.get(StringConstants.FROM_CLIENT_ACTIVATON.value) as Boolean
                        )
                        navigation.navigateTo("chats/chatPage", bundle)
                    }

                } else {

                    messageCardView.gone()
                }
            } else {
                messageCardView.gone()
            }


            textView41.text = obj.getGigTitle()
            contactPersonTV.text = if (obj.openNewGig())
                obj.agencyContact?.name
            else
                obj.gigContactDetails?.contactName

            val gigStatus = GigStatus.fromGig(obj as Gig)
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

                    checkInTV.setOnClickListener {
                        navigation.navigateTo("gig/attendance", bundleOf(
                                AppConstants.INTENT_EXTRA_GIG_ID to obj.gigId
                        ))
                    }

                    if (obj.isCheckInAndCheckOutMarked()) {
                        checkInTV.isEnabled = false
                        checkInTV.text =
                            "Checked Out"
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

                val gigTiming = if (obj.endDateTime != null)
                    "${timeFormatter.format(obj.startDateTime.toDate())} - ${
                    timeFormatter.format(
                        obj.endDateTime.toDate()
                    )
                    }"
                else
                    "${timeFormatter.format(obj.startDateTime.toDate())} - "
                textView67.text = gigTiming

            } else {
                val date = DateHelper.getDateInDDMMYYYY(obj.startDateTime.toDate())
                textView67.text = date
            }

//            navigateTV.setOnClickListener {
//
//                data?.get(adapterPosition)?.let {it1->
//                    navigationClickListener?.onItemClick(it,it1,adapterPosition)
//                }
//
//            }

            if (obj.gigContactDetails?.contactNumber != null) {

                callView.visible()
                callView.setOnClickListener{

            }
            } else if (!obj.agencyContact?.contactNumber.isNullOrEmpty()) {

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


            if (!obj.getFullCompanyLogo().isNullOrBlank()) {

                if (obj.getFullCompanyLogo()!!.startsWith("http", true)) {

                    Glide.with(context)
                        .load(obj.getFullCompanyLogo())
                        .into(companyLogoIV)

                } else {
                    FirebaseStorage.getInstance()
                        .reference
                        .child(obj.getFullCompanyLogo()!!)
                        .downloadUrl
                        .addOnSuccessListener {

                            Glide.with(context)
                                .load(it)
                                .into(companyLogoIV)
                        }
                }
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


