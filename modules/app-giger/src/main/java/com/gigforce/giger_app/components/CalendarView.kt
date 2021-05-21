package com.gigforce.giger_app.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.gigforce.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.giger_app.R
import com.gigforce.giger_app.calendarscreen.maincalendarscreen.verticalcalendar.AllotedGigDataModel
import kotlinx.android.synthetic.main.calendar_view_layout.view.*
import java.util.*
import kotlin.collections.ArrayList


class CalendarView : LinearLayout {
    var visibleOnce = false
    val TOTAL_YEAR = 3
    private var currentVisibleDate = Date()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        initControl(context)
    }

    companion object {
        var arrlist = ArrayList<MonthModel>()
    }

    lateinit var changedMonthModelListener: MonthChangeAndDateClickedListener

    lateinit var calendarData: Calendar

    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<MonthModel>

    private fun initControl(context: Context?) {
        val inflater =
                context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.calendar_view_layout, this)
        calendarData = Calendar.getInstance()
        calendarData.set(Calendar.DATE, 1)
        calendarData.set(Calendar.YEAR, 2019)
        initializeGridView()
    }


    fun setMonthChangeListener(changedMonthModelListener1: MonthChangeAndDateClickedListener) {
        changedMonthModelListener = changedMonthModelListener1
    }

    open interface MonthChangeAndDateClickedListener {
        fun onMonthChange(monthModel: MonthModel)
    }

    private fun initializeGridView() {
        recyclerGenericAdapter =
                RecyclerGenericAdapter<MonthModel>(
                        context,
                        PFRecyclerViewAdapter.OnViewHolderClick<MonthModel?> { view, position, item -> },
                        RecyclerGenericAdapter.ItemInterface<MonthModel?> { obj, viewHolder, position ->
                            var calendarDataAdapter = CalendarBaseAdapter(context)
                            calendarDataAdapter.list = obj?.days!!
                            (viewHolder.getView(R.id.calendar_grid_view) as GridView).adapter =
                                    calendarDataAdapter
                            if (this::dateClickListener.isInitialized)
                                calendarDataAdapter.setDateClickedListener(dateClickListener)
                        })
//        recyclerGenericAdapter.list = getDefaultItems()
        recyclerGenericAdapter.setLayout(R.layout.calendar_moth_data_item)
        calendar_rv.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        calendar_rv.adapter = recyclerGenericAdapter
        var linearSnapHelper: SnapHelper = PagerSnapHelper()
        linearSnapHelper.attachToRecyclerView(calendar_rv)
        var scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {

//                if(newState == RecyclerView.SCROLL_STATE_IDLE)
//                Toast.makeText(context,"working scrolled state changed "+currentVisiblePosition,Toast.LENGTH_LONG).show()

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                if(!visibleOnce) {
//                    visibleOnce = true
//                    Toast.makeText(context, "working scrolled", Toast.LENGTH_LONG).show()
//                }

                val currentVisiblePosition =
                        (calendar_rv.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                try {
                    if (changedMonthModelListener != null)
                        changedMonthModelListener.onMonthChange(
                                recyclerGenericAdapter.list.get(
                                        currentVisiblePosition
                                )
                        )
                } catch (e: Exception) {
                }

            }
        }
        calendar_rv.addOnScrollListener(scrollListener)
    }

    class MonthModel {
        var year: Int = -1
        var currentMonth: Int = -1
        var days = ArrayList<DayModel>()

        constructor()
        constructor(currentMonth: Int) {
            this.currentMonth = currentMonth
        }

        override fun toString(): String {
            var daySelected = ""
            if (days.size > 0) {
                daySelected =
                        "  " + days.get(0).date + " " + days.get(0).month + " " + days.get(0).year
            }
            return "" + year + currentMonth + daySelected
        }
    }

    class DayModel {
        var date: Int = -1
        var month: Int = -1
        var year: Int = -1
        var currentMonth: Int = -1
        var gigData: ArrayList<AllotedGigDataModel>? = ArrayList<AllotedGigDataModel>()

        constructor()
        constructor(
                date: Int,
                month: Int,
                year: Int
        ) {
            this.date = date
            this.month = month
            this.year = year
        }
    }

    private fun getDefaultItems(): ArrayList<MonthModel>? {
        arrlist = ArrayList<MonthModel>()
        for (i in 0..TOTAL_YEAR * 12) {
            arrlist.add(getMonthData())
        }
        return arrlist
    }

    private fun getMonthData(): MonthModel {
        var model = MonthModel()
        model.currentMonth = calendarData.get(Calendar.MONTH)
        model.year = calendarData.get(Calendar.YEAR)

        model.days = getDays()

        setNextMonthCalendar()
        return model
    }

    private fun setNextMonthCalendar() {
        calendarData.add(Calendar.MONTH, 1)
    }

    private fun getDays(): ArrayList<DayModel> {
        var arrListDays = ArrayList<DayModel>()
        var dayOfWeek = calendarData.get(Calendar.DAY_OF_WEEK)
        var startingDayOfWeek = getStartingDayOfWeek(dayOfWeek)
        var lastMonth = getLastMonth()
        for (x in lastMonth.getActualMaximum(Calendar.DATE) downTo lastMonth.getActualMaximum(
                Calendar.DATE
        ) - startingDayOfWeek + 1) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = lastMonth.get(Calendar.MONTH)
            dayModel.year = lastMonth.get(Calendar.YEAR)
            dayModel.currentMonth = calendarData.get(Calendar.MONTH)
            arrListDays.add(0, dayModel)
        }
        arrListDays.addAll(getCalendarCurrentMonthData())
//        for (x in 1..(7 - (arrListDays.size % 7))) {
        var nextMonth = getNextMonth()
        for (x in 1..(42 - arrListDays.size)) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = nextMonth.get(Calendar.MONTH)
            dayModel.year = nextMonth.get(Calendar.YEAR)
            dayModel.currentMonth = calendarData.get(Calendar.MONTH)
            arrListDays.add(dayModel)
        }
        return arrListDays
    }

    fun getCalendarCurrentMonthData(): ArrayList<DayModel> {
        var arrListDays = ArrayList<DayModel>()
        for (x in 1..calendarData.getActualMaximum(Calendar.DATE)) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = calendarData.get(Calendar.MONTH)
            dayModel.year = calendarData.get(Calendar.YEAR)
            dayModel.currentMonth = calendarData.get(Calendar.MONTH)
            if (arrMainHomeDataModel != null)
                for (allotedGig in arrMainHomeDataModel) {
                    if (allotedGig.date == dayModel.date && allotedGig.month == dayModel.month && allotedGig.year == dayModel.year) {
                        dayModel.gigData?.add(allotedGig)
                    }
                }
            arrListDays.add(dayModel)
        }
        return arrListDays
    }

    private fun getNextMonth(): Calendar {
        var calendar1: Calendar = Calendar.getInstance()
        calendar1.set(Calendar.MONTH, calendarData.get(Calendar.MONTH) + 1)
        return calendar1
    }

    private fun getLastMonth(): Calendar {
        var calendar1: Calendar = Calendar.getInstance()
        calendar1.set(Calendar.MONTH, calendarData.get(Calendar.MONTH) - 1)
        return calendar1
    }

    fun getStartingDayOfWeek(dayofweek: Int): Int {
        when (dayofweek) {
            1 -> return 6
            2 -> return 0
            3 -> return 1
            4 -> return 2
            5 -> return 3
            6 -> return 4
            7 -> return 5
        }
        return 0
    }

    fun setVerticalMonthChanged(calendar: Calendar) {
        var layoutManager: LinearLayoutManager? = null
        if (layoutManager == null) {
            layoutManager = calendar_rv.layoutManager as LinearLayoutManager
        }
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        if (firstVisibleItem != -1 && arrlist.size > 0) {
            if (calendar.get(Calendar.YEAR) > arrlist.get(firstVisibleItem).year || (calendar.get(
                            Calendar.YEAR
                    ) == arrlist.get(firstVisibleItem).year && calendar.get(Calendar.MONTH) > arrlist.get(
                            firstVisibleItem
                    ).currentMonth)
            ) {
                for (index in firstVisibleItem..arrlist.size - 1) {
                    if (arrlist.get(index).currentMonth == calendar.get(Calendar.MONTH) && arrlist.get(
                                    index
                            ).year == calendar.get(Calendar.YEAR)
                    ) {
                        layoutManager.scrollToPositionWithOffset(
                                index,
                                0
                        )
                    }
                }
            } else {
                for (index in 0..firstVisibleItem) {
                    if (arrlist.get(index).currentMonth == calendar.get(Calendar.MONTH) && arrlist.get(
                                    index
                            ).year == calendar.get(Calendar.YEAR)
                    ) {
                        layoutManager.scrollToPositionWithOffset(
                                index,
                                0
                        )
                    }
                }
            }
        }
    }

    lateinit var dateClickListener: MonthChangeAndDateClickedListener
    fun setOnDateClickListner(dateClickListener: MonthChangeAndDateClickedListener) {
        this.dateClickListener = dateClickListener
    }

    var arrMainHomeDataModel: ArrayList<AllotedGigDataModel> = ArrayList<AllotedGigDataModel>()
    fun setGigData(arrMainHomeDataModel: ArrayList<AllotedGigDataModel>) {
        this.arrMainHomeDataModel = arrMainHomeDataModel
        calendarData = Calendar.getInstance()
        calendarData.set(Calendar.DATE, 1)
//        calendarData.set(Calendar.YEAR, 2019)
        recyclerGenericAdapter.list = getDefaultItems()
        recyclerGenericAdapter.notifyDataSetChanged()
    }

    fun setCurrentVisibleDate(currentVisibleDate: Date) {
        this.currentVisibleDate = currentVisibleDate

    }
}