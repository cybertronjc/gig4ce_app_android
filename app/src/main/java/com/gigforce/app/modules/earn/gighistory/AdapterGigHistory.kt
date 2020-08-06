package com.gigforce.app.modules.earn.gighistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.PushDownAnim
import kotlinx.android.synthetic.main.layout_rv_gig_details_gig_history.view.*
import kotlinx.android.synthetic.main.layout_rv_gig_events_gig_hist.view.*
import kotlinx.android.synthetic.main.layout_rv_ongoing_gigs_gig_hist.view.*
import java.text.SimpleDateFormat

@Suppress("IMPLICIT_CAST_TO_ANY")
class AdapterGigHistory : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var callbacks: AdapterGigHistoryCallbacks? = null
    private var onGoingGigs: List<Gig>? = null

    private var scheduledGigs: MutableList<Gig>? = ArrayList<Gig>()
    private val timeFormatter = SimpleDateFormat("hh.mm aa")
    private val dateFormatter = SimpleDateFormat("dd MMM yyyy")
    private val headerDateFormatter = SimpleDateFormat("MMM yyyy")

    private var horizontalItemDecoration: HorizontaltemDecoration? = null


    inner class ViewHolderOnGoingGigs(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class ViewHolderGigEvents(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class ViewHolderGigDetails(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ONGOING -> ViewHolderOnGoingGigs(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_ongoing_gigs_gig_hist, parent, false)
            )
            TYPE_EVENTS -> ViewHolderGigEvents(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_gig_events_gig_hist, parent, false)
            )
            else -> ViewHolderGigDetails(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_gig_details_gig_history, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return if (scheduledGigs != null) scheduledGigs?.size!! + 2 else 2;
    }

    fun addScheduledGigs(scheduledGigs: List<Gig>?) {
        val itemSizeBefore = this.scheduledGigs?.size;
        this.scheduledGigs?.addAll(scheduledGigs!!)
        notifyItemRangeInserted(itemSizeBefore?.plus(2)!!, scheduledGigs?.size!!)
        this.scheduledGigs?.let {
            callbacks?.showNoGigExists(if (it.isEmpty()) View.VISIBLE else View.GONE)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (getItemViewType(position)) {
            TYPE_ONGOING -> {
                val viewHolderOnGoings = holder as ViewHolderOnGoingGigs
                onGoingGigs?.isEmpty()?.let {
                    viewHolderOnGoings.itemView.tv_no_on_going_gigs_gig_hist.visibility =
                        if (it) View.VISIBLE else View.GONE
                    viewHolderOnGoings.itemView.tv_on_going_gigs_gig_hist.visibility =
                        if (it) View.GONE else View.VISIBLE
                }
                val adapter = AdapterOnGoingGigs()
                viewHolderOnGoings.itemView.rv_on_going_gigs_gig_hist.adapter = adapter
                if (horizontalItemDecoration == null) {
                    horizontalItemDecoration =
                        HorizontaltemDecoration(holder.itemView.resources.getDimensionPixelOffset(R.dimen.size_8))
                } else {
                    viewHolderOnGoings.itemView.rv_on_going_gigs_gig_hist.removeItemDecoration(
                        horizontalItemDecoration!!
                    )
                }
                viewHolderOnGoings.itemView.rv_on_going_gigs_gig_hist.addItemDecoration(
                    horizontalItemDecoration!!
                )
                viewHolderOnGoings.itemView.rv_on_going_gigs_gig_hist.layoutManager =
                    LinearLayoutManager(
                        holder.itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                adapter.addData(onGoingGigs)
                adapter.setCallbacks(object : AdapterOnGoingGigs.AdapterOnGoingGigCallbacks {
                    override fun openGigDetails(gig: Gig) {
                        callbacks?.openGigDetails(gig)
                    }

                })
            }
            TYPE_EVENTS -> {

                val viewHolderGigEvents = holder as ViewHolderGigEvents

                pushDown(viewHolderGigEvents)

            }
            else -> {
                val viewHolderGigDetails = holder as ViewHolderGigDetails
                val gig = scheduledGigs?.get(position - 2)
                holder.itemView.tv_completed_gig_hist.text =
                    holder.itemView.context.getString(R.string.pending)
                holder.itemView.tv_completed_gig_hist.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.bg_circle_yellow,
                    0,
                    0,
                    0
                )
                gig?.attendance?.checkInMarked?.let { checkInMarked ->
                    {
                        gig.attendance?.checkOutMarked?.let {
                            if (checkInMarked && it) {
                                holder.itemView.tv_completed_gig_hist.text =
                                    holder.itemView.context.getString(R.string.completed)
                                holder.itemView.tv_completed_gig_hist.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.bg_circle_468800,
                                    0,
                                    0,
                                    0
                                )
                            }
                        }
                    }
                }
                val currentDate = headerDateFormatter.format(gig?.startDateTime?.toDate()!!)
                viewHolderGigDetails.itemView.tv_gig_day_rv_gig_his.text = currentDate
                viewHolderGigDetails.itemView.v_car_left_bg_rv_gig_hist.setBackgroundColor(
                    viewHolderGigDetails.itemView.resources.getColor(R.color.lipstick)
                )
                if (position == 2) {
                    viewHolderGigDetails.itemView.tv_gig_day_rv_gig_his.visibility = View.VISIBLE
                } else {

                    viewHolderGigDetails.itemView.tv_gig_day_rv_gig_his.visibility =
                        if (currentDate == headerDateFormatter.format(scheduledGigs!![position - 3].startDateTime?.toDate()!!)) View.GONE else View.VISIBLE
                }
                viewHolderGigDetails.itemView.tv_date_gig_hist.isSelected = true
                viewHolderGigDetails.itemView.rl_on_going_gig_hist.visibility = View.GONE
                viewHolderGigDetails.itemView.rl_scheduled_gig_hist.visibility = View.VISIBLE
                holder.itemView.tv_designation_rv_gig_hist.text = gig.title
                holder.itemView.tv_gig_venue_rv_gig_his.text = gig.address
                holder.itemView.tv_gig_venue_rv_gig_his.isSelected = true
                holder.itemView.tv_rating_rv_gig_hist.text = gig.gigRating.toString()
                holder.itemView.tv_time_rv_gig_hist.text = ""
                gig.startDateTime?.toDate()?.let {
                    holder.itemView.tv_date_gig_hist.text = dateFormatter.format(it)
                }
                gig.endDateTime?.let { endDateTimeStamp ->
                    gig.startDateTime?.let {
                        val durationCalculated = endDateTimeStamp.toDate().time - it.toDate().time
                        val hours = (durationCalculated / (1000 * 60 * 60))
                        val mins = (durationCalculated / (1000 * 60)).toInt() % 60
                        holder.itemView.tv_time_rv_gig_hist.text =
                            "${hours}${viewHolderGigDetails.itemView.context.getString(R.string.hours)} : ${mins}${viewHolderGigDetails.itemView.context.getString(
                                R.string.mins
                            )}"
                    }
                }
                Glide.with(viewHolderGigDetails.itemView).load(gig.companyLogo)
                    .placeholder(R.drawable.profile)
                    .into(viewHolderGigDetails.itemView.iv_brand_rv_gig_hist)
                PushDownAnim.setPushDownAnimTo(holder.itemView)
                    .setOnClickListener(View.OnClickListener {
                        callbacks?.openGigDetails(scheduledGigs!![holder.adapterPosition - 2])
                    })
                holder.itemView.tv_timing_rv_gig_hist.text = if (gig.endDateTime != null)
                    "${timeFormatter.format(gig.startDateTime!!.toDate())} - ${timeFormatter.format(
                        gig.endDateTime!!.toDate()
                    )}"
                else
                    "${timeFormatter.format(gig.startDateTime!!.toDate())} - "
            }
        }

    private fun pushDown(viewHolderGigEvents: ViewHolderGigEvents) {
        PushDownAnim.setPushDownAnimTo(viewHolderGigEvents.itemView.tv_past_events_rv_gig_hist)
            .setOnClickListener(View.OnClickListener {
                callbacks?.getPastGigs()
                callbacks?.setEventState(EVENT_PAST);
                viewHolderGigEvents.itemView.tv_past_events_rv_gig_hist.setBackgroundResource(
                    R.drawable.bg_selected_event_rv_gig_hist
                )
                viewHolderGigEvents.itemView.tv_upcoming_events_rv_gig_hist.setBackgroundResource(
                    R.drawable.bg_unselected_event_rv_gig_hist
                )
                viewHolderGigEvents.itemView.tv_past_events_rv_gig_hist.setTextColor(
                    viewHolderGigEvents.itemView.context.getColor(R.color.vertical_calendar_today)
                )
                viewHolderGigEvents.itemView.tv_upcoming_events_rv_gig_hist.setTextColor(
                    viewHolderGigEvents.itemView.context.getColor(R.color.black_2222)
                )
                val size27 =
                    viewHolderGigEvents.itemView.context.resources.getDimensionPixelSize(R.dimen.size_27)
                val size6 =
                    viewHolderGigEvents.itemView.context.resources.getDimensionPixelSize(R.dimen.size_6)

                viewHolderGigEvents.itemView.tv_past_events_rv_gig_hist.setPadding(
                    size27,
                    size6,
                    size27,
                    size6
                )
                viewHolderGigEvents.itemView.tv_upcoming_events_rv_gig_hist.setPadding(
                    size27,
                    size6,
                    size27,
                    size6
                )

            })
        PushDownAnim.setPushDownAnimTo(viewHolderGigEvents.itemView.tv_upcoming_events_rv_gig_hist)
            .setOnClickListener(View.OnClickListener {
                clickUpComing(viewHolderGigEvents, true)

            })
        if (callbacks?.getEventState() == EVENT_UPCOMING) {
            clickUpComing(viewHolderGigEvents, false)
        }
    }

    private fun clickUpComing(viewHolderGigEvents: ViewHolderGigEvents, refreshList: Boolean) {
        if (refreshList) {
            callbacks?.getUpcomingGigs()
        }
        callbacks?.setEventState(EVENT_UPCOMING)
        viewHolderGigEvents.itemView.tv_past_events_rv_gig_hist.setBackgroundResource(
            R.drawable.bg_unselected_event_rv_gig_hist
        )
        viewHolderGigEvents.itemView.tv_upcoming_events_rv_gig_hist.setBackgroundResource(
            R.drawable.bg_selected_event_rv_gig_hist
        )
        viewHolderGigEvents.itemView.tv_past_events_rv_gig_hist.setTextColor(
            viewHolderGigEvents.itemView.context.getColor(R.color.black_2222)
        )
        viewHolderGigEvents.itemView.tv_upcoming_events_rv_gig_hist.setTextColor(
            viewHolderGigEvents.itemView.context.getColor(R.color.vertical_calendar_today)
        )
        val size27 =
            viewHolderGigEvents.itemView.context.resources.getDimensionPixelSize(R.dimen.size_27)
        val size6 =
            viewHolderGigEvents.itemView.context.resources.getDimensionPixelSize(R.dimen.size_6)

        viewHolderGigEvents.itemView.tv_past_events_rv_gig_hist.setPadding(
            size27,
            size6,
            size27,
            size6
        )
        viewHolderGigEvents.itemView.tv_upcoming_events_rv_gig_hist.setPadding(
            size27,
            size6,
            size27,
            size6
        )
    }

    companion object {
        const val TYPE_ONGOING = 1
        const val TYPE_EVENTS = 2
        const val TYPE_GIG_DETAILS = 3
        const val EVENT_PAST = 4;
        const val EVENT_UPCOMING = 5;
    }

    fun addOnGoingGigs(onGoingGigs: List<Gig>?) {
        this.onGoingGigs = onGoingGigs;
        notifyItemChanged(0)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_ONGOING
            1 -> TYPE_EVENTS
            else -> TYPE_GIG_DETAILS
        }
    }

    fun setCallbacks(adapterGigHistoryCallbacks: AdapterGigHistoryCallbacks) {
        this.callbacks = adapterGigHistoryCallbacks;
    }

    fun clearData() {
        scheduledGigs?.size?.let { notifyItemRangeRemoved(2, it) }
        scheduledGigs?.clear()

    }


    public interface AdapterGigHistoryCallbacks {
        fun showNoGigExists(int: Int)
        fun getPastGigs()
        fun getUpcomingGigs()
        fun openGigDetails(gig: Gig)
        fun getEventState(): Int
        fun setEventState(state: Int)
    }
}