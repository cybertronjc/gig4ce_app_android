package com.gigforce.giger_app.calendarscreen.maincalendarscreen.bottomsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.datamodels.gigpage.ContactPerson
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.core.utils.DateHelper
import com.gigforce.giger_app.R
//import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat

class UpcomingGigBSAdapter(val context: Context, val itemWidth : Int) :
    RecyclerView.Adapter<UpcomingGigBSAdapter.CustomViewHolder>() {

    private val timeFormatter = SimpleDateFormat("hh.mm aa")

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val cardView = itemView.findViewById<View>(R.id.card_view)
        val ivContact = itemView.findViewById<ImageView>(R.id.iv_call)
        val iv_message = itemView.findViewById<ImageView>(R.id.iv_message)
        val messageCardView = itemView.findViewById<View>(R.id.messageCardView)

        val textView41 = itemView.findViewById<TextView>(R.id.textView41)
        val contactPersonTV = itemView.findViewById<TextView>(R.id.contactPersonTV)
        val checkInTV = itemView.findViewById<Button>(R.id.checkInTV)

        val callView = itemView.findViewById<View>(R.id.callCardView)

        val navigateTV = itemView.findViewById<View>(R.id.navigateTV)

        val companyLogoIV = itemView.findViewById<ImageView>(R.id.companyLogoIV)

        val textView67 = itemView.findViewById<TextView>(R.id.textView67)


        fun bindView(obj: Gig) {

//            val lp = cardView.layoutParams
//            lp.height = lp.height
//            lp.width = itemWidth
//            var ivContact = getImageView(viewHolder, R.id.iv_call)
            cardView.setOnClickListener{
                clickListener?.onItemClick(it, obj,adapterPosition)
            }
            ivContact.setImageResource(R.drawable.ic_phone_white_24dp)
            ivContact.setColorFilter(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.lipstick
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )

            iv_message.setImageResource(R.drawable.ic_chat)

            if (obj.openNewGig() && obj.agencyContact?.uid != null) {

                messageCardView.visible()
                messageCardView.setOnClickListener {
                    val bundle = Bundle()
                    data?.get(adapterPosition)?.agencyContact?.let { it1 ->

                        agencyClickListener?.onItemClick(it, it1, adapterPosition)
                    } ?: return@setOnClickListener
//                    val agencyContact =
//                        data[adapterPosition].agencyContact
//                            ?: return@setOnClickListener

//                    navigate(
//                        R.id.chatPageFragment, bundleOf(
//                            ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to agencyContact.uid,
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE to agencyContact.profilePicture,
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to agencyContact.name
//                        )
//                    )
                }

            } else if (obj.gigContactDetails != null && obj.gigContactDetails?.contactNumber != null) {
                if (obj.chatInfo?.isNullOrEmpty() == false) {
                    messageCardView.visible()
                    messageCardView.setOnClickListener {
                        data?.get(adapterPosition)?.chatInfo?.let { it1 ->

                            chatInfoClickListener?.onItemClick(it, it1, adapterPosition)
                        }
//                        val bundle = Bundle()
//                        val map = upcomingGigs[viewHolder.adapterPosition].chatInfo
//                        bundle.putString(
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE,
//                            AppConstants.IMAGE_URL
//                        )
//                        bundle.putString(
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME,
//                            AppConstants.CONTACT_NAME
//                        )
//                        bundle.putString(
//                            ChatPageFragment.INTENT_EXTRA_CHAT_TYPE,
//                            ChatConstants.CHAT_TYPE_USER
//                        )
//
//                        bundle.putString(
//                            ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID,
//                            map?.get("chatHeaderId") as String
//                        )
//                        bundle.putString(
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID,
//                            map.get("otherUserId") as String
//                        )
//                        bundle.putString(
//                            StringConstants.MOBILE_NUMBER.value,
//                            map.get(StringConstants.MOBILE_NUMBER.value) as String
//                        )
//                        bundle.putBoolean(
//                            StringConstants.FROM_CLIENT_ACTIVATON.value,
//                            map.get(StringConstants.FROM_CLIENT_ACTIVATON.value) as Boolean
//                        )
//                        navigate(R.id.chatPageFragment, bundle)
                    }

                } else {

                    messageCardView.gone()
                }
            } else {
                messageCardView.gone()
            }

//            cardView.layoutParams = lp
//            cardView.layoutParams = lp
            textView41.text = obj.getGigTitle()
            contactPersonTV.text = if (obj.openNewGig())
                obj.agencyContact?.name
            else
                obj.gigContactDetails?.contactName

            val gigStatus = GigStatus.fromGig(obj)
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
                        data?.get(adapterPosition)?.let {it1->
                            callClickListener?.onItemClick(it,it1,adapterPosition)
                        }

//                        CheckInClickListener(
//                            upcoming_gig_rv,
//                            position
//                        )
                    }

                    if (obj.isCheckInAndCheckOutMarked()) {
                        checkInTV.isEnabled = false
                        checkInTV.text =
                            "Checked Out"
                    } else if (obj.isCheckInMarked()) {
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

            navigateTV.setOnClickListener {

                data?.get(adapterPosition)?.let {it1->
                    navigationClickListener?.onItemClick(it,it1,adapterPosition)
                }

            }

//            val callView = getView(viewHolder, R.id.callCardView)
            if (obj.gigContactDetails?.contactNumber != null) {

                callView.visible()
                callView.setOnClickListener{
                    data?.get(adapterPosition)?.let {it1->
                        callClickListener?.onItemClick(it,it1,adapterPosition)
                    }
//                    CallClickListener(
//                        upcoming_gig_rv,
//                        position
//                    )
            }
            } else if (!obj.agencyContact?.contactNumber.isNullOrEmpty()) {

                callView.visible()
                callView.setOnClickListener {
                    data?.get(adapterPosition)?.let {it1->
                        callClickListener?.onItemClick(it,it1,adapterPosition)
                    }
//                    CallClickListener(
//                        upcoming_gig_rv,
//                        position
//                    )
                }
            } else {
                callView.gone()
            }


//            val companyLogoIV = getImageView(viewHolder, R.id.companyLogoIV)
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

    var data: List<Gig>? = null

    var clickListener: AdapterClickListener<Gig>? = null

    fun setOnclickListener(listener: AdapterClickListener<Gig>) {
        this.clickListener = listener
    }

    var agencyClickListener: AdapterClickListener<ContactPerson>? = null

    fun setAgencyOnclickListener(listener: AdapterClickListener<ContactPerson>) {
        this.agencyClickListener = listener
    }

    var chatInfoClickListener: AdapterClickListener<Map<String, Any>>? = null

    fun setchatInfoOnclickListener(listener: AdapterClickListener<Map<String, Any>>) {
        this.chatInfoClickListener = listener
    }


    var callClickListener: AdapterClickListener<Any>? = null

    fun setcallOnclickListener(listener: AdapterClickListener<Any>) {
        this.callClickListener = listener
    }

    var navigationClickListener: AdapterClickListener<Any>? = null

    fun setnavigationOnclickListener(listener: AdapterClickListener<Any>) {
        this.navigationClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.upcoming_gig_item, null)
        )
    }

    override fun getItemCount() = data?.size ?: 0


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        data?.let {
            holder.bindView(it.get(position))
        }
    }

    override fun onViewAttachedToWindow(holder: UpcomingGigBSAdapter.CustomViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.updateLayoutParams {
            width = itemWidth
        }
    }
}