package com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.vertical_calendar_item.view.*

public class VerticalCalendarAdapter(var baseFragment:BaseFragment,var arrCalendarData: ArrayList<VerticalCalendarDataItemModel>) :RecyclerView.Adapter<VerticalCalendarAdapter.VerticalCalendarViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = VerticalCalendarViewHolder(baseFragment,
        LayoutInflater.from(parent.context).inflate(R.layout.vertical_calendar_item, parent, false)
    )

    override fun onBindViewHolder(holder: VerticalCalendarViewHolder, position: Int) {
    holder.bind(arrCalendarData.get(position))

    }

    class VerticalCalendarViewHolder(baseFragment: BaseFragment,view: View) : RecyclerView.ViewHolder(view) {

        private val monthlyCalendar = view.calendar_month_cl
        private val calendarDetailItem = view.calendar_detail_item_cl
        private val title = view.title
        private val subTitle = view.subtitle
        private val day = view.day
        private val date = view.date
        private val daydatecard = view.daydatecard
        private val baseFragment = baseFragment
        fun bind(verticalData: VerticalCalendarDataItemModel) {
            if (verticalData!!.isMonth) {
                monthlyCalendar.visibility = View.VISIBLE
                calendarDetailItem.visibility = View.GONE
            } else if (verticalData!!.isPreviousDate) {
                monthlyCalendar.visibility = View.GONE
                calendarDetailItem.visibility = View.VISIBLE
                title.text = verticalData?.title
                subTitle.visibility = View.GONE
                day.text = verticalData?.day
                date.text = verticalData?.date.toString()
                baseFragment.setTextViewColor(
                    title,
                    R.color.gray_color_calendar
                )
                baseFragment.setTextViewColor(
                    day,
                    R.color.gray_color_calendar
                )
                baseFragment.setTextViewColor(
                    date,
                    R.color.gray_color_calendar
                )

                baseFragment.setViewBackgroundColor(
                    daydatecard,
                    R.color.gray_color_calendar_previous_date_50
                )
                daydatecard.alpha = 0.5F
                baseFragment.setTextViewSize(title, 12F)
                baseFragment.setTextViewSize(day, 12F)
                baseFragment.setTextViewSize(date, 12F)
            } else if (verticalData!!.isToday) {
                monthlyCalendar.visibility = View.GONE
                calendarDetailItem.visibility = View.VISIBLE
                title.text = verticalData?.title
                subTitle.text = verticalData?.subTitle
                day.text = verticalData?.day
                date.text = verticalData?.date.toString()
            } else if (verticalData!!.isGigAssign) {
                monthlyCalendar.visibility = View.GONE
                calendarDetailItem.visibility = View.VISIBLE
                title.text = verticalData?.title
                subTitle.text = verticalData?.subTitle
                day.text = verticalData?.day
                date.text = verticalData?.date.toString()
                baseFragment.setTextViewColor(
                    title,
                    R.color.black
                )
                baseFragment.setTextViewColor(
                    subTitle,
                    R.color.black
                )
                baseFragment.setTextViewColor(
                    day,
                    R.color.black
                )
                baseFragment.setTextViewColor(
                    date,
                    R.color.black
                )
                baseFragment.setViewBackgroundColor(
                    daydatecard,
                    R.color.vertical_calendar_today_50
                )
                baseFragment.setTextViewSize(title, 12F)
                baseFragment.setTextViewSize(day, 12F)
                baseFragment.setTextViewSize(date, 12F)
            } else if (!verticalData!!.isGigAssign) {
                monthlyCalendar.visibility = View.GONE
                calendarDetailItem.visibility = View.VISIBLE
                title.text = verticalData?.title
                subTitle.visibility = View.GONE
                day.text = verticalData?.day
                date.text = verticalData?.date.toString()
                baseFragment.setTextViewColor(
                    title,
                    R.color.gray_color_calendar
                )
                baseFragment.setTextViewColor(
                    day,
                    R.color.gray_color_calendar
                )
                baseFragment.setTextViewColor(
                    date,
                    R.color.gray_color_calendar
                )
                baseFragment.setViewBackgroundColor(
                    daydatecard,
                    R.color.vertical_calendar_today_20
                )
                baseFragment.setTextViewSize(title, 12F)
                baseFragment.setTextViewSize(day, 12F)
                baseFragment.setTextViewSize(date, 12F)
            }else {
                monthlyCalendar.visibility = View.GONE
                calendarDetailItem.visibility = View.VISIBLE
                title.text = verticalData?.title
                subTitle.text = verticalData?.subTitle
                day.text = verticalData?.day
                date.text = verticalData?.date.toString()
            }
        }
    }

    override fun getItemCount(): Int = arrCalendarData.size
}

