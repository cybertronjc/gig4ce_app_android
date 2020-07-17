package com.gigforce.app.modules.calendarscreen.maincalendarscreen

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.components.CalendarView
import com.gigforce.app.core.base.dialog.ConfirmationDialogOnClickListener
import com.gigforce.app.core.base.dialog.OptionSelected
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.CalendarRecyclerItemTouchHelper
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.VerticalCalendarDataItemModel
import com.gigforce.app.modules.custom_gig_preferences.CustomPreferencesViewModel
import com.gigforce.app.modules.custom_gig_preferences.ParamCustPreferViewModel
import com.gigforce.app.modules.custom_gig_preferences.UnavailableDataModel
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.StorageReference
import com.riningan.widget.ExtendedBottomSheetBehavior
import com.riningan.widget.ExtendedBottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.calendar_home_screen.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class CalendarHomeScreen : BaseFragment(),
    CalendarRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    companion object {
        fun newInstance() =
            CalendarHomeScreen()

        lateinit var temporaryData: VerticalCalendarDataItemModel
    }

    lateinit var selectedMonthModel: CalendarView.MonthModel

    lateinit var arrCalendarDependent: Array<View>
    private var mExtendedBottomSheetBehavior: ExtendedBottomSheetBehavior<*>? = null
    private lateinit var viewModel: CalendarHomeScreenViewModel
    lateinit var viewModelProfile: ProfileViewModel
    lateinit var viewModelCustomPreference: CustomPreferencesViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.calendar_home_screen, inflater, container)
    }

    override fun isConfigRequired(): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CalendarHomeScreenViewModel::class.java)
        viewModelProfile = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        viewModelCustomPreference =
            ViewModelProvider(this, ParamCustPreferViewModel(viewLifecycleOwner)).get(
                CustomPreferencesViewModel::class.java
            )

        arrCalendarDependent =
            arrayOf(calendar_dependent, margin_40, below_oval, calendar_cv, bottom_sheet_top_shadow)
        selectedMonthModel = CalendarView.MonthModel(Calendar.getInstance().get(Calendar.MONTH))
        initializeViews()
        listener()
        observePreferenceData()
    }



    private fun initializeViews() {
        initializeExtendedBottomSheet()
        initializeMonthTV(Calendar.getInstance(), true)
        initializeVerticalCalendarRV()
    }

    private fun initializeExtendedBottomSheet() {
        mExtendedBottomSheetBehavior = ExtendedBottomSheetBehavior.from(nsv)
        mExtendedBottomSheetBehavior?.state = STATE_COLLAPSED
        mExtendedBottomSheetBehavior?.isAllowUserDragging = true;
    }

    private fun listener() {
        cardView.setOnClickListener(View.OnClickListener { navigate(R.id.profileFragment) })
//        tv_hs1bs_alert.setOnClickListener(View.OnClickListener { navigate(R.id.verification) })
        chat_icon_iv.setOnClickListener {
            navigate(R.id.contactScreenFragment)
        }
        month_year.setOnClickListener(View.OnClickListener {
            changeVisibilityCalendarView()
        })
        bottom_sheet_shadow_view.setOnClickListener {
            changeVisibilityCalendarView()
        }
        calendar_dependent.setOnClickListener {
            changeVisibilityCalendarView()
        }
        month_selector_arrow.setOnClickListener{
            changeVisibilityCalendarView()
        }
        oval_gradient_iv.setOnClickListener{
            changeVisibilityCalendarView()
        }
        calendarView.setMonthChangeListener(object : CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
                selectedMonthModel = monthModel
                var calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, monthModel.currentMonth)
                calendar.set(Calendar.YEAR, monthModel.year)
                calendar.set(Calendar.DATE, 1)
                initializeMonthTV(calendar, false)
                if (!isLoading) {
                    recyclerGenericAdapter.list.addAll(
                        viewModel.getVerticalCalendarData(
                            recyclerGenericAdapter.list.get(recyclerGenericAdapter.list.size - 1),
                            false
                        )
                    )
                }
            }

        })
        calendarView.setOnDateClickListner(object : CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
                selectedMonthModel = monthModel
                println(" date data1 "+selectedMonthModel.toString())
                changeVisibilityCalendarView()
            }

        })

    }

    private fun changeVisibilityCalendarView() {
        var extendedBottomSheetBehavior: ExtendedBottomSheetBehavior<NestedScrollView> =
            ExtendedBottomSheetBehavior.from(nsv);
        if (extendedBottomSheetBehavior.isAllowUserDragging) {
            hideDependentViews(false)
            extendedBottomSheetBehavior.state = ExtendedBottomSheetBehavior.STATE_COLLAPSED
            extendedBottomSheetBehavior.isAllowUserDragging = false
        } else {
            if(selectedMonthModel.days!=null  && selectedMonthModel.days.size==1){
                scrollVerticalCalendarToSelectedDate()
            }
            else {
                scrollVerticalCalendarToSelectedMonth()
            }
            hideDependentViews(true)
            extendedBottomSheetBehavior.isAllowUserDragging = true
        }
    }

    private fun scrollVerticalCalendarToSelectedDate() {
        var layoutManager: LinearLayoutManager? = null
        if (layoutManager == null) {
            layoutManager = rv_.layoutManager as LinearLayoutManager
        }
        val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
        var dayModel = selectedMonthModel.days.get(0)
        if (recyclerGenericAdapter.list.get(firstVisibleItem).year < dayModel.year || (recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).year == dayModel.year && recyclerGenericAdapter.list.get(firstVisibleItem).month < dayModel.month)||(recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).year == dayModel.year && recyclerGenericAdapter.list.get(firstVisibleItem).month == dayModel.month&&recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).date < dayModel.date)
        ) {
            for (index in firstVisibleItem..recyclerGenericAdapter.list.size) {
                if (recyclerGenericAdapter.list.get(index).year == dayModel.year && recyclerGenericAdapter.list.get(
                        index
                    ).month == dayModel.month && recyclerGenericAdapter.list.get(
                        index
                    ).date == dayModel.date
                ) {
                    (rv_.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(
                        index,
                        0
                    )
                    break
                }
            }
        } else {
            for (index in 0..firstVisibleItem) {
                if (recyclerGenericAdapter.list.get(index).year == dayModel.year && recyclerGenericAdapter.list.get(index).month == dayModel.month && recyclerGenericAdapter.list.get(index).date == dayModel.date) {
                    (rv_.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(
                        index,
                        0
                    )
                    break
                }
            }
        }
    }


    private fun hideDependentViews(hide: Boolean) {
        for (view in arrCalendarDependent) {
            if (hide)
                view.gone()
            else view.visible()
        }

    }

    private fun observePreferenceData() {
        viewModel.mainHomeLiveDataModel.observe(viewLifecycleOwner, Observer { homeDataModel ->
            if (homeDataModel != null) {
//                viewModel.setDataModel(homeDataModel.all_gigs)
                initializeViews()
                calendarView.setGigData(viewModel.arrMainHomeDataModel!!)
            }
        })


        // load user data
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile.profileAvatarName)
            if (profile.name != null && !profile.name.equals(""))
                tv1HS1.text = profile.name
        })
        viewModelCustomPreference.customPreferencesLiveDataModel.observe(
            viewLifecycleOwner,
            Observer { data ->
                viewModel.setCustomPreferenceData(viewModelCustomPreference.getCustomPreferenceData())
                initializeViews()
            })
    }

    private fun displayImage(profileImg: String) {
        if (profileImg != null && !profileImg.equals("")) {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }


    private fun initializeMonthTV(calendar: Calendar, needaction: Boolean) {
        val pattern = "MMMM YYYY"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(calendar.time)
        month_year.text = date
        if (needaction)
            calendarView.setVerticalMonthChanged(calendar)
    }

    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<VerticalCalendarDataItemModel>
    private val visibleThreshold = 20
    var isLoading: Boolean = false
    private fun initializeVerticalCalendarRV() {
        recyclerGenericAdapter =
            RecyclerGenericAdapter<VerticalCalendarDataItemModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<VerticalCalendarDataItemModel?> { view, position, item ->
                    //item?.year, item?.month, item?.date

                    val activeDateTime =
                        LocalDateTime.of(item?.year!!, item.month + 1, item.date, 0, 0, 0)

                    val bundle = Bundle()
                    bundle.putSerializable("active_date", activeDateTime)
                    findNavController().navigate(R.id.rosterDayFragment, bundle)
                },
                RecyclerGenericAdapter.ItemInterface<VerticalCalendarDataItemModel?> { obj, viewHolder, position ->
                    if (obj!!.isMonth) {
                        showMonthLayout(true, viewHolder)
                        getTextView(viewHolder, R.id.month_year).text =
                            obj.monthStr + " " + obj.year
                    } else {
                        setTextViewSize(getTextView(viewHolder, R.id.title), 14F)
                        setTextViewSize(getTextView(viewHolder, R.id.subtitle), 12F)
                        getView(viewHolder, R.id.coloredsideline).visibility = View.GONE
                        getView(viewHolder, R.id.graysideline).visibility = View.VISIBLE
                        showMonthLayout(false, viewHolder)
                        getTextView(viewHolder, R.id.title).text = obj?.title
                        if (obj?.subTitle != null && !obj?.subTitle.equals("")) {
                            getTextView(viewHolder, R.id.subtitle).visibility = View.VISIBLE
                            getTextView(viewHolder, R.id.subtitle).text = obj?.subTitle
                        } else {
                            getTextView(viewHolder, R.id.subtitle).visibility = View.GONE
                        }
                        getTextView(viewHolder, R.id.day).text = obj?.day
                        getTextView(viewHolder, R.id.date).text = obj?.date.toString()
                        if (obj!!.isToday) {
                            getView(viewHolder, R.id.coloredsideline).visibility = View.VISIBLE
                            getView(viewHolder, R.id.graysideline).visibility = View.GONE

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
//                            setTextViewSize(getTextView(viewHolder, R.id.title), 14F)
//                            setTextViewSize(getTextView(viewHolder, R.id.subtitle), 12F)
                            setTextViewSize(getTextView(viewHolder, R.id.day), 12F)
                            setTextViewSize(getTextView(viewHolder, R.id.date), 14F)
                        } else if (obj!!.isPreviousDate) {
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
                            } else {
                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                                getView(viewHolder, R.id.daydatecard).alpha = 0.5F
                            }
                        } else {
                            if (obj!!.isUnavailable) {
                                getTextView(viewHolder, R.id.title).text = "Not working"
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
                                    R.color.date_day_unavailable_color
                                )
                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                                setBackgroundStateAvailable(viewHolder)
                            } else if (obj!!.isGigAssign) {
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
                            } else {
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
        rv_.scrollToPosition((recyclerGenericAdapter.list.size / 2) - 2)

        var scrollListener = object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                var layoutManager: LinearLayoutManager? = null
                if (layoutManager == null) {
                    layoutManager = recyclerView.layoutManager as LinearLayoutManager
                }
//                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
//                var calendar = Calendar.getInstance()
//                calendar.set(
//                    Calendar.MONTH,
//                    recyclerGenericAdapter.list.get(firstVisibleItem).month
//                )
//                calendar.set(Calendar.YEAR, recyclerGenericAdapter.list.get(firstVisibleItem).year)
//                initializeMonthTV(calendar)
                if (!isLoading) {
                    val totalItemCount = recyclerView!!.layoutManager?.itemCount


                    val lastVisibleItem = layoutManager!!.findLastVisibleItemPosition()
                    if (totalItemCount!! <= (lastVisibleItem + visibleThreshold)) {
                        isLoading = true;
                        recyclerGenericAdapter.list.addAll(
                            viewModel.getVerticalCalendarData(
                                recyclerGenericAdapter.list.get(recyclerGenericAdapter.list.size - 1),
                                false
                            )
                        )
                        recyclerGenericAdapter.notifyDataSetChanged()
                        isLoading = false
                    }

                    // below commented code will require later
//                    else if ((firstVisibleItem - visibleThreshold)<=0) {
//                        isLoading = true;
//                        recyclerGenericAdapter.list.addAll(0,viewModel.getVerticalCalendarData(
//                            recyclerGenericAdapter.list.get(0),true
//                        ))
//                        recyclerGenericAdapter.notifyDataSetChanged()
//                        isLoading = false
//                    }
                }


            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var layoutManager: LinearLayoutManager? = null
                if (layoutManager == null) {
                    layoutManager = recyclerView.layoutManager as LinearLayoutManager
                }
                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
                if(firstVisibleItem==0)return;
                var calendar = Calendar.getInstance()

                calendar.set(
                    Calendar.MONTH,
                    recyclerGenericAdapter.list.get(firstVisibleItem - 1).month
                )
                calendar.set(Calendar.YEAR, recyclerGenericAdapter.list.get(firstVisibleItem).year)
                initializeMonthTV(calendar, true)
            }
        }
        rv_.addOnScrollListener(scrollListener)

        var itemTouchListener =
            CalendarRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, this)
        ItemTouchHelper(itemTouchListener).attachToRecyclerView(rv_)

    }

    private fun scrollVerticalCalendarToSelectedMonth() {
        var layoutManager: LinearLayoutManager? = null
        if (layoutManager == null) {
            layoutManager = rv_.layoutManager as LinearLayoutManager
        }
        val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
        if (recyclerGenericAdapter.list.get(firstVisibleItem).year < selectedMonthModel.year || (recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).year == selectedMonthModel.year && recyclerGenericAdapter.list.get(firstVisibleItem).month < selectedMonthModel.currentMonth)
        ) {
            for (index in firstVisibleItem..recyclerGenericAdapter.list.size) {
                if (recyclerGenericAdapter.list.get(index).year == selectedMonthModel.year && recyclerGenericAdapter.list.get(
                        index
                    ).month == selectedMonthModel.currentMonth
                ) {
                    (rv_.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(
                        index + 1,
                        0
                    )
                    break
                }
            }
        } else {
            for (index in 0..firstVisibleItem) {
                if (recyclerGenericAdapter.list.get(index).year == selectedMonthModel.year && recyclerGenericAdapter.list.get(index).month == selectedMonthModel.currentMonth) {
                    (rv_.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(
                        index + 1,
                        0
                    )
                    break
                }
            }
        }
    }

    private fun setBackgroundStateAvailable(viewHolder: PFRecyclerViewAdapter<Any?>.ViewHolder) {
        setViewBackgroundColor(
            getView(viewHolder, R.id.action_layout),
            R.color.action_layout_available
        )
        setViewBackgroundColor(
            getView(viewHolder, R.id.border_top),
            R.color.action_layout_available_border
        )
        setViewBackgroundColor(
            getView(viewHolder, R.id.border_bottom),
            R.color.action_layout_available_border
        )
        setTextViewColor(
            getTextView(viewHolder, R.id.title_calendar_action_item),
            R.color.action_layout_available_title
        )
        getTextView(viewHolder, R.id.title_calendar_action_item).text = "Marked working"
        getImageView(viewHolder, R.id.flash_icon).setImageResource(R.drawable.ic_flash_green)
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        temporaryData = recyclerGenericAdapter.list.get(position)
        if (temporaryData.isGigAssign) {
            if (temporaryData.isUnavailable) {
                temporaryData.isUnavailable = false
                recyclerGenericAdapter.notifyItemChanged(position)
            } else {
                var subTitle =
                    "You have " + temporaryData.title + " Gig \non the day. These gig will get " +
                            "cancelled as well."
                showConfirmationDialogType3(
                    " Are sure you want to change availablilty ?",
                    subTitle,
                    object : ConfirmationDialogOnClickListener {
                        override fun clickedOnYes(dialog: Dialog?) {
                            showConfirmationDialogType4(
                                "Cancelling out on a gig !",
                                "Please let us know your reason, to mark 'not Working' ?",
                                object : OptionSelected {
                                    override fun optionSelected(dialog: Dialog?, option: String) {
                                        if (option.equals("")) {
                                            showToast("Please select one option.")
                                        } else {
                                            temporaryData.isUnavailable = true
                                            temporaryData.isGigAssign = false
                                            temporaryData.title = "No gigs assigned"
                                            temporaryData.reason = option
                                            viewModelCustomPreference.updateCustomPreference(
                                                UnavailableDataModel(temporaryData.getDateObj())
                                            )
                                            recyclerGenericAdapter.notifyItemChanged(position)
                                            dialog?.dismiss()
                                        }
                                    }

                                })
                            dialog?.dismiss()
                        }

                        override fun clickedOnNo(dialog: Dialog?) {
                            recyclerGenericAdapter.notifyItemChanged(position)
                            dialog?.dismiss()
                        }

                    })
            }
        } else if (temporaryData.isUnavailable) {
            temporaryData.isUnavailable = false
            viewModelCustomPreference.deleteCustomPreference(
                UnavailableDataModel(temporaryData.getDateObj())
            )
            recyclerGenericAdapter.notifyItemChanged(position)
        } else {
            temporaryData.isUnavailable = true
            viewModelCustomPreference.updateCustomPreference(
                UnavailableDataModel(temporaryData.getDateObj())
            )
            recyclerGenericAdapter.notifyItemChanged(position)
            showSnackbar(position)
        }
//        val snackbar = Snackbar
//            .make(coodinate_layout, "Item was removed from the list.", Snackbar.LENGTH_LONG)
//        snackbar.setAction("UNDO", View.OnClickListener {
//        })
//        snackbar.setActionTextColor(resources.getColor(R.color.snakbar_action_color))
//        snackbar.show()
    }

    class OnSnackBarUndoClickListener(
        var position: Int,
        var recyclerGenericAdapter: RecyclerGenericAdapter<VerticalCalendarDataItemModel>,
        var snackbar: Snackbar,
        var viewModelCustomPreference: CustomPreferencesViewModel
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            temporaryData.isUnavailable = false
            viewModelCustomPreference.deleteCustomPreference(
                UnavailableDataModel(temporaryData.getDateObj())
            )
            recyclerGenericAdapter.notifyItemChanged(position)
            snackbar.dismiss()
        }
    }

    private fun showSnackbar(position: Int) {
        nsv.visibility = View.GONE
        val snackbar = Snackbar.make(coodinate_layout, "", Snackbar.LENGTH_LONG);
        // Get the Snackbar's layout view
        var layout = snackbar.getView() as Snackbar.SnackbarLayout;
        // Hide the text
        var textView =
            layout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        // Inflate our custom view
        var snackView = layoutInflater.inflate(R.layout.snackbar_layout, null);
        snackView.setOnClickListener(
            OnSnackBarUndoClickListener(
                position,
                recyclerGenericAdapter,
                snackbar,
                viewModelCustomPreference
            )
        )
        //If the view is not covering the whole snackbar layout, add this line
        layout.setPadding(0, 0, 0, 0);
        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0);
        // Show the Snackbar
        snackbar.show();
        Handler().postDelayed({
            nsv.visibility = View.VISIBLE
        }, SNACKBAR_TIMEOUT)

    }

    private val SNACKBAR_TIMEOUT: Long = 2000 // 1 sec
    private fun showMonthLayout(show: Boolean, viewHolder: PFRecyclerViewAdapter<Any?>.ViewHolder) {
        if (show) {
            getView(viewHolder, R.id.calendar_month_cl).visibility = View.VISIBLE
            getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.GONE
        } else {
            getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
            getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
        }
    }

}