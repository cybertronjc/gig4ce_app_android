package com.gigforce.app.core.base.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.gigforce.app.R
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import kotlinx.android.synthetic.main.calendar_view_layout.view.*
import java.util.*
import kotlin.collections.ArrayList


class CalendarView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        initControl(context);
    }

    companion object {
        lateinit var changedMonthModelListener: MonthChangeListener
        var calendarData = Calendar.getInstance()
    }

    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<MonthModel>

    private fun initControl(context: Context?) {
        val inflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.calendar_view_layout, this)
        calendarData.set(Calendar.DATE, 1)
        initializeGridView()
    }


    fun setMonthChangeListener(changedMonthModelListener1: MonthChangeListener) {
        changedMonthModelListener = changedMonthModelListener1
    }

    open interface MonthChangeListener {
        fun onMonthChange(monthModel: MonthModel)
    }

    private fun initializeGridView() {
        recyclerGenericAdapter =
            RecyclerGenericAdapter<MonthModel>(
                context,
                PFRecyclerViewAdapter.OnViewHolderClick<MonthModel?> { view, position, item -> },
                RecyclerGenericAdapter.ItemInterface<MonthModel?> { obj, viewHolder, position ->
                    var calendarDataAdapter = CalendarBaseAdapter()
                    calendarDataAdapter.list = obj?.days!!
                    (viewHolder.getView(R.id.calendar_grid_view) as GridView).adapter =
                        calendarDataAdapter
                })!!
        recyclerGenericAdapter.list = getDefaultItems()
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
                var currentVisiblePosition =
                    (calendar_rv.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                changedMonthModelListener.onMonthChange(
                    recyclerGenericAdapter.list.get(
                        currentVisiblePosition
                    )
                )

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }
        }
        calendar_rv.addOnScrollListener(scrollListener)
    }

    class MonthModel {
        var year: Int = -1
        var currentMonth: Int = -1
        var days = ArrayList<DayModel>()
    }

    class DayModel {
        var date: Int = -1
        var month: Int = -1
        var year: Int = -1
        var currentMonth: Int = -1
    }

    private fun getDefaultItems(): ArrayList<MonthModel>? {
        var arrlist = ArrayList<MonthModel>()
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
        arrlist.add(getMonthData())
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
        for (x in 1..calendarData.getActualMaximum(Calendar.DATE)) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = calendarData.get(Calendar.MONTH)
            dayModel.year = calendarData.get(Calendar.YEAR)
            dayModel.currentMonth = calendarData.get(Calendar.MONTH)
            arrListDays.add(dayModel)
        }
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

    private fun getNextMonth(): Calendar {
        var calendar1: Calendar = Calendar.getInstance();
        calendar1.set(Calendar.MONTH, calendarData.get(Calendar.MONTH) + 1)
        return calendar1
    }

    private fun getLastMonth(): Calendar {
        var calendar1: Calendar = Calendar.getInstance();
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
}