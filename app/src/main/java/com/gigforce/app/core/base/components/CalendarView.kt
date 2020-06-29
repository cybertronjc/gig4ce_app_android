package com.gigforce.app.core.base.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
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

    private fun initControl(context: Context?) {
        val inflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.calendar_view_layout, this)
        // layout is inflated, assign local variables to components
        initializeGridView()
    }

    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<MonthModel>

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
    }

    class MonthModel {
        var year: Int = -1
        var currentMonth: Int = -1
        lateinit var days: ArrayList<DayModel>
    }

    class DayModel {
        var date: Int = -1
        var month: Int = -1
        var year: Int = -1
    }

    private fun getDefaultItems(): ArrayList<MonthModel>? {
        var arrlist = ArrayList<MonthModel>()
        arrlist.add(getCurrentMonthData())
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))
        arrlist.add(getNextMonthData(arrlist.get(arrlist.size - 1)))

        return arrlist
    }

    private fun getNextMonthData(lastMonthModel: MonthModel): MonthModel {
        var model = MonthModel()
        var calendar: Calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, lastMonthModel.currentMonth + 1)
        model.currentMonth = calendar.get(Calendar.MONTH)
        model.year = calendar.get(Calendar.YEAR)
        model.days = getDays(lastMonthModel)
        return model
    }

    private fun getDays(lastMonthModel: MonthModel): ArrayList<DayModel> {
        var arrListDays = ArrayList<DayModel>()
        arrListDays.addAll(
            lastMonthModel.days.subList(
                lastMonthModel.days.size - 7,
                lastMonthModel.days.size
            )
        )
        for (x in arrListDays.get(arrListDays.size - 1).date + 1..getLastDateOfCurrentMonth(
            lastMonthModel.currentMonth + 1
        )) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = lastMonthModel.currentMonth + 1
            dayModel.year = lastMonthModel.year
            arrListDays.add(dayModel)
        }
        for (x in 1..(35 - arrListDays.size)) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = lastMonthModel.currentMonth + 1
            dayModel.year = lastMonthModel.year
            arrListDays.add(dayModel)
        }
        return arrListDays
    }

    private fun getCurrentMonthData(): MonthModel {
        var model = MonthModel()
        var calendar: Calendar = Calendar.getInstance();
        model.currentMonth = calendar.get(Calendar.MONTH)
        model.year = calendar.get(Calendar.YEAR)
        model.days = getDays(model.currentMonth, model.year)
        return model
    }

    private fun getDays(currentMonth: Int, year: Int): ArrayList<DayModel> {
        var arrListDays = ArrayList<DayModel>()
        var calendar: Calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth)
        calendar.set(Calendar.DATE, 1)
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        var startingDayOfWeek = getStartingDayOfWeek(dayOfWeek)
        for (x in getLastDateOfLastMonth(calendar) downTo getLastDateOfLastMonth(calendar) - startingDayOfWeek + 1) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = currentMonth
            dayModel.year = year
            arrListDays.add(0, dayModel)
        }
        for (x in 1..calendar.getActualMaximum(Calendar.DATE)) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = currentMonth
            dayModel.year = year
            arrListDays.add(dayModel)
        }
//        for (x in 1..(7 - (arrListDays.size % 7))) {
        for (x in 1..(35 - arrListDays.size)) {
            var dayModel = DayModel()
            dayModel.date = x
            dayModel.month = currentMonth + 1
            dayModel.year = year
            arrListDays.add(dayModel)
        }
        return arrListDays
    }

    private fun getLastDateOfLastMonth(calendar: Calendar): Int {
        var calendar1: Calendar = Calendar.getInstance();
        calendar1.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1)
        return calendar1.getActualMaximum(Calendar.DATE)
    }

    private fun getLastDateOfCurrentMonth(month: Int): Int {
        var calendar1: Calendar = Calendar.getInstance();
        calendar1.set(Calendar.MONTH, month)
        return calendar1.getActualMaximum(Calendar.DATE)
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