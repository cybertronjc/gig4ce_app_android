package com.gigforce.app.modules.homescreen.mainhome

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.homescreen.mainhome.bottomsheet.FeatureModel
import com.gigforce.app.modules.homescreen.mainhome.bottomsheet.UpcomingGigModel
import com.gigforce.app.modules.homescreen.mainhome.verticalcalendar.VerticalCalendarDataItemModel
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.StorageReference
import com.riningan.widget.ExtendedBottomSheetBehavior
import com.riningan.widget.ExtendedBottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.homescreen_1nsvbs.*
import java.text.SimpleDateFormat
import java.util.*


class MainHomeScreen : BaseFragment() {

    companion object {
        fun newInstance() =
            MainHomeScreen()
    }

    private var mExtendedBottomSheetBehavior: ExtendedBottomSheetBehavior<*>? = null
    private lateinit var viewModel: MainHomeScreenViewModel
    lateinit var viewModelProfile: ProfileViewModel
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
        viewModelProfile = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
    }

    private fun initializeViews() {
        initialiseMonthTV()
        initializeVerticalCalendarRV()
    }

    private fun listener() {
        iv2HS1.setOnClickListener(View.OnClickListener { navigate(R.id.profileFragment) })
//        tv_hs1bs_alert.setOnClickListener(View.OnClickListener { navigate(R.id.verification) })
    }

    private fun observePreferenceData() {
        viewModel.mainHomeLiveDataModel.observe(viewLifecycleOwner, Observer { homeDataModel ->
            if(homeDataModel!=null) {
                viewModel.setDataModel(homeDataModel.all_gigs)
                initializeViews()
            }
        })


        // load user data
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile.profileAvatarName)
            if(profile.name!=null && !profile.name.equals(""))
            tv1HS1.text = profile.name
        })
    }

    private fun displayImage(profileImg:String) {
        if(profileImg!=null && !profileImg.equals("")) {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.context!!)
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }


    private fun initialiseMonthTV() {
        val pattern = "MMM YYYY"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(Date())
        tv2HS1.text = date
    }
    private val visibleThreshold = 10
    var isLoading:Boolean = false
    private fun initializeVerticalCalendarRV() {
        val recyclerGenericAdapter: RecyclerGenericAdapter<VerticalCalendarDataItemModel> =
            RecyclerGenericAdapter<VerticalCalendarDataItemModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<VerticalCalendarDataItemModel?> { view, position, item ->
//                    Toast.makeText(this.context, "CLICKED ON DAY DEBUG", Toast.LENGTH_LONG).show()
                    navigate(R.id.rosterDayFragment)
                },
                RecyclerGenericAdapter.ItemInterface<VerticalCalendarDataItemModel?> { obj, viewHolder, position ->
                    if (obj!!.isMonth) {
                        showMonthLayout(true,viewHolder)
                        getTextView(viewHolder,R.id.month_year).text = obj.monthStr+" "+obj.year
                    }
                    else{
                        getView(viewHolder,R.id.coloredsideline).visibility = View.GONE
                        getView(viewHolder,R.id.graysideline).visibility = View.VISIBLE
                        showMonthLayout(false,viewHolder)
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        if (obj?.subTitle != null && !obj?.subTitle.equals(""))
                        {
                            getTextView(viewHolder, R.id.subtitle).visibility = View.VISIBLE
                            getTextView(viewHolder, R.id.subtitle).text = obj?.subTitle
                        }
                        else
                        {
                            getTextView(viewHolder, R.id.subtitle).visibility = View.GONE
                        }
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date.toString()
                        if(obj!!.isToday)
                        {
                            getView(viewHolder,R.id.coloredsideline).visibility = View.VISIBLE
                            getView(viewHolder,R.id.graysideline).visibility = View.GONE

                            setViewBackgroundColor(
                                getView(viewHolder, R.id.daydatecard),
                                R.color.vertical_calendar_today
                            )
                            setTextViewColor(
                                getTextView(viewHolder, R.id.title),
                                R.color.vertical_calendar_today1
                            )
                            setTextViewColor(
                                getTextView(viewHolder, R.id.subtitle),
                                R.color.vertical_calendar_today1
                            )
                            setTextViewColor(
                                getTextView(viewHolder, R.id.day),
                                R.color.white
                            )
                            setTextViewColor(
                                getTextView(viewHolder, R.id.date),
                                R.color.white
                            )
                            getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                            setTextViewSize(getTextView(viewHolder, R.id.title), 14F)
                            setTextViewSize(getTextView(viewHolder, R.id.subtitle), 10F)
                            setTextViewSize(getTextView(viewHolder, R.id.day), 12F)
                            setTextViewSize(getTextView(viewHolder, R.id.date), 14F)
                        }
                        else if (obj!!.isPreviousDate)
                        {
                            setTextViewColor(
                                getTextView(viewHolder, R.id.title),
                                R.color.gray_color_calendar
                            )
                            setTextViewColor(
                                getTextView(viewHolder, R.id.subtitle),
                                R.color.gray_color_calendar
                            )
                            setTextViewColor(
                                getTextView(viewHolder, R.id.day),
                                R.color.gray_color
                            )
                            setTextViewColor(
                                getTextView(viewHolder, R.id.date),
                                R.color.gray_color
                            )
                            setViewBackgroundColor(
                                getView(viewHolder, R.id.daydatecard),
                                R.color.gray_color_calendar_previous_date
                            )
                            if (obj!!.isGigAssign) {
                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                            }
                            else{
                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                                getView(viewHolder, R.id.daydatecard).alpha = 0.5F
                            }
                        }
                        else
                        {
                            if (obj!!.isGigAssign){
                                setTextViewColor(
                                    getTextView(viewHolder, R.id.title),
                                    R.color.black_color_future_date
                                )
                                setTextViewColor(
                                    getTextView(viewHolder, R.id.subtitle),
                                    R.color.black_color_future_date
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
                                    R.color.vertical_calendar_today1
                                )
                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                                getView(viewHolder, R.id.daydatecard).alpha = 0.7F
                            }
                            else{
                                setTextViewColor(
                                    getTextView(viewHolder, R.id.title),
                                    R.color.gray_color_day_date_calendar
                                )
                                setTextViewColor(
                                    getTextView(viewHolder, R.id.day),
                                    R.color.gray_color_day_date_calendar
                                )
                                setTextViewColor(
                                    getTextView(viewHolder, R.id.date),
                                    R.color.gray_color_day_date_calendar
                                )
                                setViewBackgroundColor(
                                    getView(viewHolder, R.id.daydatecard),
                                    R.color.vertical_calendar_today1
                                )
                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                                getView(viewHolder, R.id.daydatecard).alpha = 0.4F
                            }
                        }
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
                // below commented code will require later
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

    }

    private fun showMonthLayout(show: Boolean, viewHolder: PFRecyclerViewAdapter<Any?>.ViewHolder) {
        if(show) {
            getView(viewHolder, R.id.calendar_month_cl).visibility = View.VISIBLE
            getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.GONE
        }
        else{
            getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
            getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
        }
    }

}