package com.gigforce.app.modules.homescreen.mainhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.homescreen.mainhome.verticalcalendar.VerticalCalendarDataItemModel
import com.riningan.widget.ExtendedBottomSheetBehavior
import com.riningan.widget.ExtendedBottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.homescreen_1nsvbs.*


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
//        initializeViews()
        observePreferenceData()
    }
    private fun observePreferenceData() {
        viewModel.mainHomeLiveDataModel.observe(viewLifecycleOwner, Observer { homeDataModel ->
            viewModel.setDataModel(homeDataModel.all_gigs)
            initializeViews()
        })
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
    private val visibleThreshold = 10
    var isLoading:Boolean = false
    private fun initializeVerticalCalendarRV() {
        val recyclerGenericAdapter: RecyclerGenericAdapter<VerticalCalendarDataItemModel> =
            RecyclerGenericAdapter<VerticalCalendarDataItemModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<VerticalCalendarDataItemModel?> { view, position, item ->
                    showToast(
                        ""
                    )
                },
                RecyclerGenericAdapter.ItemInterface<VerticalCalendarDataItemModel?> { obj, viewHolder, position ->

                    if (obj!!.isMonth) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.VISIBLE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.GONE
                        getTextView(viewHolder,R.id.month_year).text = obj.monthStr+" "+obj.year
                    } else if (obj!!.isPreviousDate) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        getTextView(viewHolder, R.id.subtitle).visibility = View.GONE
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date.toString()
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
                        getTextView(viewHolder, R.id.day).text = obj?.day.toString()
                        getTextView(viewHolder, R.id.date).text = obj?.date.toString()
                    } else if (obj!!.isGigAssign) {
                        getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
                        getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        getTextView(viewHolder, R.id.subtitle).text = obj?.subTitle
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date.toString()
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
                            R.color.vertical_calendar_today_70
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
                        getTextView(viewHolder, R.id.date).text = obj?.date.toString()
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
                            R.color.vertical_calendar_today_40
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
                        getTextView(viewHolder, R.id.date).text = obj?.date.toString()
                    }
                })!!

        recyclerGenericAdapter.list = viewModel.getAllCalendarData()
        recyclerGenericAdapter.setLayout(R.layout.vertical_calendar_item)
        rv_.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        rv_.adapter = recyclerGenericAdapter
        rv_.scrollToPosition((recyclerGenericAdapter.list.size/2)-2)

        var scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView!!.layoutManager?.itemCount
                var layoutManager: LinearLayoutManager? = null
                if (layoutManager == null) {
                    layoutManager = recyclerView.layoutManager as LinearLayoutManager
                }
                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
                val lastVisibleItem = layoutManager!!.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount!! <= (lastVisibleItem + visibleThreshold)) {
                    isLoading = true;
                    recyclerGenericAdapter.list.addAll(viewModel.getVerticalCalendarData(
                        recyclerGenericAdapter.list.get(recyclerGenericAdapter.list.size-1),false
                    ))
                    recyclerGenericAdapter.notifyDataSetChanged()
                    isLoading = false
                }
//                if (!isLoading && (firstVisibleItem - visibleThreshold)<=0) {
//                    isLoading = true;
//                    recyclerGenericAdapter.list.addAll(0,viewModel.getVerticalCalendarData(
//                        recyclerGenericAdapter.list.get(0),true
//                    ))
//                    recyclerGenericAdapter.notifyDataSetChanged()
//                    isLoading = false
//                }
                }
            }
        rv_.addOnScrollListener(scrollListener)
//            rv_.addOnScrollListener(OnScrollListener() {
//                @Override
//                public void onScrolled( recyclerView:RecyclerView,  dx:Int,  dy:Int) {
//                    if (!noMoreItem) {
//                        LinearLayoutManager linearLayoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
//                        int totalItemCount = linearLayoutManager.getItemCount();
//                        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
//                        if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
//                            VisitVlsModel mediaFilesModel = new VisitVlsModel();
//                            mediaFilesModel.setFile_type("loading");
//                            arrayListViewVls.add(mediaFilesModel);
//                            vlsViewFragmentAdapter.customNotifyItemInserted(arrayListViewVls.size() - 1);
//                            isLoading = true;
//                            isScrolledDataRequested = true;
//                            requestNextPage();
//                            if (MainActivity.Current.isNetworkAvailable())
//                                webCallForGetAllVlsData(false);
//                        }
//                    }
//                }
//            });
    }



}









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