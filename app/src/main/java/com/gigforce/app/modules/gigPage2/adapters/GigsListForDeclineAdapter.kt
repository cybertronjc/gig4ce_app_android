package com.gigforce.app.modules.gigPage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage2.models.Gig
import com.gigforce.app.utils.TextDrawable
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.recycler_row_gig_list_for_decline.view.*
import java.text.SimpleDateFormat

interface GigsListForDeclineAdapterListener {

    fun onCallClicked(gig: Gig)

    fun onMessageClicked(gig: Gig)
}

class GigsListForDeclineAdapter constructor(
    private val context: Context
) : RecyclerView.Adapter<GigsListForDeclineAdapter.TodaysGigViewHolder>() {

    private var gigSelectionListener: ((Gig) -> Unit)? = null
    private lateinit var mLayoutInflater: LayoutInflater
    private var mGigs: List<Gig> = emptyList()
    private var mSelectedGigIndexes: MutableList<Int> = mutableListOf()
    private val timeFormatter = SimpleDateFormat("hh.mm aa")
    private lateinit var mGigsListForDeclineAdapterListener: GigsListForDeclineAdapterListener

    fun setGigsListForDeclineAdapterListener(gigsListForDeclineAdapterListener: GigsListForDeclineAdapterListener) {
        this.mGigsListForDeclineAdapterListener = gigsListForDeclineAdapterListener
    }

    fun setOnLearningVideoActionListener(listener: (Gig) -> Unit) {
        this.gigSelectionListener = listener
    }

    val selectedGigCount: Int get() = mSelectedGigIndexes.size

    fun getSelectedGig(): List<Gig> {
        return if (mSelectedGigIndexes.isEmpty())
            emptyList()
        else {
            val selectedGigs = mutableListOf<Gig>()
            mSelectedGigIndexes.forEach {
                selectedGigs.add(mGigs[it])
            }

            selectedGigs
        }
    }

    fun updateGig(gigs: List<Gig>) {
        this.mGigs = gigs
        this.mSelectedGigIndexes = mutableListOf()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodaysGigViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return TodaysGigViewHolder(
            mLayoutInflater.inflate(
                R.layout.recycler_row_gig_list_for_decline,
                parent,
                false
            ), viewType
        )
    }

    override fun getItemCount() = mGigs.size

    override fun onBindViewHolder(holder: TodaysGigViewHolder, position: Int) {
        holder.bindTo(mGigs[position])
        holder.selectionRadioButton.isChecked = mSelectedGigIndexes.contains(position)
    }

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    inner class TodaysGigViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView),
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {

        val selectionRadioButton = itemView.selectRadioButton
        private val gigTitleTv = itemView.textView41
        private val gigTimingTV = itemView.textView67
        private val contactPersonNameTV = itemView.contactPersonTV
        private val companyLogoIV = itemView.companyLogoIV
        private val callCard = itemView.callCardView
        private val messageCardView = itemView.messageCardView

        init {
            itemView.setOnClickListener(this)
            selectionRadioButton.setOnCheckedChangeListener(this)
            callCard.setOnClickListener(this)
            messageCardView.setOnClickListener(this)
        }

        fun bindTo(gig: Gig) {

            if (!gig.getFullCompanyLogo().isNullOrBlank()) {

                if (gig.getFullCompanyLogo()!!.startsWith("http", true)) {

                    Glide.with(context)
                        .load(gig.getFullCompanyLogo())
                        .into(companyLogoIV)
                } else {
                    FirebaseStorage.getInstance()
                        .reference
                        .child(gig.getFullCompanyLogo()!!)
                        .downloadUrl
                        .addOnSuccessListener {

                            Glide.with(context)
                                .load(it)
                                .into(companyLogoIV)
                        }
                }
            } else {
                val companyInitials = if (gig.getFullCompanyName().isNullOrBlank())
                    "C"
                else
                    gig.getFullCompanyName()!![0].toString().toUpperCase()
                val drawable = TextDrawable.builder().buildRound(
                    companyInitials,
                    ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
                )
                companyLogoIV.setImageDrawable(drawable)
            }

            gigTitleTv.text = gig.getGigTitle()

            gigTimingTV.text = "${timeFormatter.format(gig.startDateTime.toDate())} - ${
                timeFormatter.format(gig.endDateTime.toDate())
            }"

            contactPersonNameTV.text = if (gig.openNewGig()) {
                gig.agencyContact?.name
            } else {
                gig.gigContactDetails?.contactName
            }

            callCard.isVisible = if (gig.openNewGig()) {
                !gig.agencyContact?.contactNumber.isNullOrEmpty()
            } else {
                !gig.gigContactDetails?.contactNumberString.isNullOrEmpty()
            }


            messageCardView.isVisible = gig.agencyContact?.uid != null
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

            if (isChecked) {
                mSelectedGigIndexes.add(adapterPosition)
            } else {
                mSelectedGigIndexes.remove(adapterPosition)
            }

            gigSelectionListener?.invoke(mGigs[adapterPosition])
        }

        override fun onClick(v: View?) {
            v ?: return
            val position = adapterPosition

            val gig = mGigs[position]

            when (v.id) {
                R.id.messageCardView -> mGigsListForDeclineAdapterListener.onMessageClicked(gig)
                R.id.callCardView -> mGigsListForDeclineAdapterListener.onCallClicked(gig)
                else -> {
                    selectionRadioButton.toggle()
                }
            }

        }
    }
}
