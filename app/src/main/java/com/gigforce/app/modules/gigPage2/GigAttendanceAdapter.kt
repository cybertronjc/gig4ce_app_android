package com.gigforce.app.modules.gigPage2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage2.models.OtherOption
import com.gigforce.core.utils.GlideApp
import kotlinx.android.synthetic.main.recycler_item_gig_attendance.view.*
import kotlinx.android.synthetic.main.recycler_item_other_option.view.*
import java.text.SimpleDateFormat
import java.time.format.TextStyle
import java.util.*

interface GigAttendanceAdapterClickListener {
    fun onAttendanceClicked(option: Gig)
}

class GigAttendanceAdapter(
    private val context: Context,
    private val gigs: List<Gig>
) :
    RecyclerView.Adapter<GigAttendanceAdapter.GigAttendanceViewHolder>() {

    private lateinit var mLayoutInflater: LayoutInflater
    private var otherOptionClickListener: GigAttendanceAdapterClickListener? = null
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    fun setListener(otherOptionClickListener: GigAttendanceAdapterClickListener) {
        this.otherOptionClickListener = otherOptionClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GigAttendanceViewHolder {

        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }

        return GigAttendanceViewHolder(
            mLayoutInflater.inflate(
                R.layout.recycler_item_gig_attendance,
                parent,
                false
            ), viewType
        )
    }

    override fun onBindViewHolder(holder: GigAttendanceViewHolder, position: Int) {

        val gig = gigs[position]
        val gigStartDateTime = gig.startDateTime!!.toLocalDateTime()

        val dayName = gigStartDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        holder.dayNameTV.text = dayName
        holder.dayTV.text = gigStartDateTime.dayOfMonth.toString()

        if(gig.attendance != null){

            if(gig.attendance!!.checkInTime != null){
                holder.punchInTV.text = timeFormatter.format(gig.attendance!!.checkInTime)
            } else{
                holder.punchInTV.text = "--:--"
            }

            if(gig.attendance!!.checkOutTime != null){
                holder.punchOutTV.text = timeFormatter.format(gig.attendance!!.checkOutTime)
            } else{
                holder.punchOutTV.text = "--:--"
            }
        } else{
            holder.punchInTV.text = "--:--"
            holder.punchOutTV.text = "--:--"
        }

    }

    override fun getItemCount() = gigs.size

    inner class GigAttendanceViewHolder(itemView: View, viewType: Int) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        val dayNameTV = itemView.dayNameTV
        val dayTV = itemView.dayTV
        val punchInTV = itemView.punch_in_tv
        val punchOutTV = itemView.punch_out_tv

        override fun onClick(v: View?) {
            otherOptionClickListener?.onAttendanceClicked(gigs[adapterPosition])
        }
    }

}