package com.gigforce.app.modules.homescreen.mainhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.riningan.widget.ExtendedBottomSheetBehavior
import com.riningan.widget.ExtendedBottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.homescreen_1nsvbs.*
import kotlinx.android.synthetic.main.vertical_calendar_item.view.*


// UNused below can be removed
//class DemoBottomSheetFragment : SuperBottomSheetFragment() {
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        super.onCreateView(inflater, container, savedInstanceState)
//        return inflater.inflate(R.layout.homescreen1_bs1, container, false)
//    }
//    //override fun getCornerRadius() = "16dp" //context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)
//    override fun getStatusBarColor() = Color.RED
//}
//
//class ExtBottomSheetFragment : BaseFragment() {
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        super.onCreateView(inflater, container, savedInstanceState)
//        return inflater.inflate(R.layout.homescreen_1nsvbs, container, false)
//    }
//    //override fun getCornerRadius() = "16dp" //context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)
//    //override fun getStatusBarColor() = Color.RED
//}

class MainHomeScreen : BaseFragment() {

    companion object {
        fun newInstance() =
            MainHomeScreen()
    }

    private var mExtendedBottomSheetBehavior: ExtendedBottomSheetBehavior<*>? = null
    private lateinit var viewModel: MainHomeScreenViewModel
    private val itemList: Array<String>
        get() = arrayOf(
            "My Gig",
            "Item 2",
            "Item 3",
            "Item 4",
            "Item 5",
            "Item 6",
            "Item 7",
            "Item 8",
            "Item 9",
            "Item 10",
            "Item 11",
            "Item 12"
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.homescreen_1nsvbs, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mExtendedBottomSheetBehavior = ExtendedBottomSheetBehavior.from(nsv)
        mExtendedBottomSheetBehavior?.state = STATE_COLLAPSED
        mExtendedBottomSheetBehavior?.isAllowUserDragging = true;
        viewModel = ViewModelProviders.of(this).get(MainHomeScreenViewModel::class.java)
        initializeViews()
    }

    private fun initializeViews() {
        initializeVerticalCalendarRV()
        initializeBSGridView()
    }

    private fun initializeBSGridView() {
        val adapter = this.context?.let {
            FeaturesAdapter(
                it,
                R.layout.item_grid_features_hs1,
                itemList
            )
        }
        gridView_hs1.adapter = adapter
    }

    private fun initializeVerticalCalendarRV() {
        val recyclerGenericAdapter: RecyclerGenericAdapter<DataItem> =
            RecyclerGenericAdapter<DataItem>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<DataItem?> { view, position, item ->
                    showToast(
                        ""
                    )
                },
                RecyclerGenericAdapter.ItemInterface<DataItem?> { obj, viewHolder, position ->

                    if (obj!!.isMonth) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.VISIBLE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.GONE
                    } else if (obj!!.isPreviousDate) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        getTextView(viewHolder, R.id.subtitle).visibility = View.GONE
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date
                        setTextViewColor(
                            getTextView(viewHolder, R.id.title),
                            R.color.gray_color_calendar
                        )
                        setTextViewColor(
                            getTextView(viewHolder, R.id.day),
                            R.color.gray_color_calendar
                        )
                        setTextViewColor(
                            getTextView(viewHolder, R.id.date),
                            R.color.gray_color_calendar
                        )

                        setViewBackgroundColor(
                            getView(viewHolder, R.id.daydatecard),
                            R.color.gray_color_calendar_previous_date_50
                        )
                        getView(viewHolder, R.id.daydatecard).alpha = 0.5F
                        setTextViewSize(getTextView(viewHolder, R.id.title), 12F)
                        setTextViewSize(getTextView(viewHolder, R.id.day), 12F)
                        setTextViewSize(getTextView(viewHolder, R.id.date), 12F)
                    } else if (obj!!.isToday) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        getTextView(viewHolder, R.id.subtitle).text = obj?.subTitle
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date
                    } else if (obj!!.isGigAssign) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        getTextView(viewHolder, R.id.subtitle).text = obj?.subTitle
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date
                        setTextViewColor(
                            getTextView(viewHolder, R.id.title),
                            R.color.black
                        )
                        setTextViewColor(
                            getTextView(viewHolder, R.id.subtitle),
                            R.color.black
                        )
                        setTextViewColor(
                            getTextView(viewHolder, R.id.day),
                            R.color.black
                        )
                        setTextViewColor(
                            getTextView(viewHolder, R.id.date),
                            R.color.black
                        )
                        setViewBackgroundColor(
                            getView(viewHolder, R.id.daydatecard),
                            R.color.vertical_calendar_today_50
                        )
                        setTextViewSize(getTextView(viewHolder, R.id.title), 12F)
                        setTextViewSize(getTextView(viewHolder, R.id.day), 12F)
                        setTextViewSize(getTextView(viewHolder, R.id.date), 12F)
                    } else if (!obj!!.isGigAssign) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        getTextView(viewHolder, R.id.subtitle).visibility = View.GONE
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date
                        setTextViewColor(
                            getTextView(viewHolder, R.id.title),
                            R.color.gray_color_calendar
                        )
                        setTextViewColor(
                            getTextView(viewHolder, R.id.day),
                            R.color.gray_color_calendar
                        )
                        setTextViewColor(
                            getTextView(viewHolder, R.id.date),
                            R.color.gray_color_calendar
                        )

                        setViewBackgroundColor(
                            getView(viewHolder, R.id.daydatecard),
                            R.color.vertical_calendar_today_20
                        )

                        setTextViewSize(getTextView(viewHolder, R.id.title), 12F)
                        setTextViewSize(getTextView(viewHolder, R.id.day), 12F)
                        setTextViewSize(getTextView(viewHolder, R.id.date), 12F)
                    }else {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        getTextView(viewHolder, R.id.subtitle).text = obj?.subTitle
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date
                    }
                })!!

        recyclerGenericAdapter.list = getVerticalCalendarData()
        recyclerGenericAdapter.setLayout(R.layout.vertical_calendar_item)
        rv_.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        rv_.adapter = recyclerGenericAdapter
    }

    fun getVerticalCalendarData(): ArrayList<DataItem> {
        var datalist: ArrayList<DataItem> = ArrayList<DataItem>()
        datalist.add(DataItem("No gigs Assigned", "", "21", "Tues", false, true, false, false))
        datalist.add(
            DataItem(
                "Swiggy Deliveries",
                "+3 More",
                "22",
                "Tues",
                true,
                false,
                true,
                false
            )
        )
        datalist.add(
            DataItem(
                "Dunzo Deliveries",
                "+2 More",
                "23",
                "Wed",
                false,
                false,
                true,
                false
            )
        )
        datalist.add(
            DataItem(
                "No gigs Assigned",
                "+3 More",
                "24",
                "Thus",
                false,
                false,
                false,
                false
            )
        )
        datalist.add(
            DataItem(
                "Dunzo Deliveries",
                "+2 More",
                "25",
                "Fri",
                false,
                false,
                true,
                false
            )
        )
        datalist.add(
            DataItem(
                "Swiggy Deliveries",
                "+3 More",
                "26",
                "Sat",
                false,
                false,
                false,
                true
            )
        )
        datalist.add(
            DataItem(
                "No gigs Assigned",
                "+3 More",
                "27",
                "Sun",
                false,
                false,
                false,
                false
            )
        )
        datalist.add(
            DataItem(
                "Dunzo Deliveries",
                "+2 More",
                "28",
                "Mon",
                false,
                false,
                true,
                false
            )
        )
        datalist.add(
            DataItem(
                "Swiggy Deliveries",
                "+3 More",
                "29",
                "Tues",
                false,
                false,
                false,
                false
            )
        )
        return datalist
    }

    class DataItem(
        val title: String,
        val subTitle: String,
        val date: String,
        val day: String,
        val isToday: Boolean,
        val isPreviousDate: Boolean,
        val isGigAssign: Boolean,
        val isMonth: Boolean
    ) {
//        title:String = "Swiggy Deliveries";
//        var subTitle:String = "+3 More";
//        var date:String = "16";
//        var comment:String = "Last gig complete 20 min ago";
//        var day:String = "Sun";
//        var isToday:Boolean = true;

    }

}