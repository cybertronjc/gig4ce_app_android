package com.gigforce.app.modules.onboardingmain

import android.content.Context
import android.graphics.PointF
import android.graphics.Typeface
import android.os.Bundle
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.*
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.AppConstants
import kotlinx.android.synthetic.main.onboarding_main_fragment.*


class OnboardingMainFragment : BaseFragment() {

    companion object {
        fun newInstance() = OnboardingMainFragment()
    }
    private lateinit var profileData : ProfileData
    private lateinit var viewModel: OnboardingMainViewModel
    private var firstTimeLoad : Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.onboarding_main_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(OnboardingMainViewModel::class.java)
        initializeViews()
    }

    private fun initializeViews() {
        initializePager()
        initializeTitleAsName()
        listeners()
        observer()
    }

    private fun observer() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            profileData = profile
            if (profile != null) {
                if(profile.status) {
                    if(firstTimeLoad){
                        checkForAlreadyCompletedData()
                        setLiveDataListItems()
                        firstTimeLoad = false
                    }
                    else {
                        if(nextPage())
                        setLiveDataListItems()
                    }
                }
                else
                showToast(profile.errormsg)
            }
        })
    }

    private fun checkNullOrBlank(str:String):Boolean{
        if(str==null || str.equals("")){
            return true
        }
        return false
    }
    private fun checkForAlreadyCompletedData() {
        if(!checkNullOrBlank(profileData.name)&&checkNullOrBlank(profileData.ageGroup)){
            showHideBackIcon(true)
            onboarding_pager.setCurrentItem(1)
        }else if(checkNullOrBlank(profileData.gender)){
            showHideBackIcon(true)
            onboarding_pager.setCurrentItem(2)
        }else if(checkNullOrBlank(profileData.highestEducation)){
            showHideBackIcon(true)
            onboarding_pager.setCurrentItem(3)
        }else if(checkNullOrBlank(profileData.workStatus)){
            showHideBackIcon(true)
            onboarding_pager.setCurrentItem(4)
        }
    }

    private fun setLiveDataListItems() {
        when (onboarding_pager.currentItem) {
            0 -> onboarding_pager.getChildAt(0).findViewById<EditText>(R.id.user_name).setText(
                profileData.name
            )
            1 -> if (profileData.ageGroup != null || !profileData.ageGroup.equals(""))
                setRecyclerItem(
                1,
                    profileData.ageGroup
            )
            2 -> if (profileData.gender != null || !profileData.gender.equals(""))
                setRecyclerItem(
                2,
                    profileData.gender
            )
            3 -> if (profileData.highestEducation != null || !profileData.highestEducation.equals(""))
                setRecyclerItem(
                3,
                    profileData.highestEducation
            )
            4 -> if (profileData.workStatus != null || !profileData.workStatus.equals(""))
                setRecyclerItem(
                    4, profileData.workStatus
                )
        }
    }

    private fun setRecyclerItem(pagerPosition: Int, data: String) {
//        showToast(onboarding_pager.adapter?.itemCount!!.toString()+"     "+onboarding_pager.currentItem)
        var recyclerView = allRecyclerViews.get(onboarding_pager.currentItem-1)
        var position = (recyclerView.adapter as RecyclerGenericAdapter<String>).list.indexOf(data)
        (recyclerView.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(position,0)
    }

    private fun initializeTitleAsName() {
        setProgressBarWeight(20f)
        progress_completion_tv.text = "1/5"
        title_onboarding.text = "What's your name?"
    }

    private fun initializeTitleAsAge() {
        setProgressBarWeight(40f)
        progress_completion_tv.text = "2/5"
        title_onboarding.text = "What's your age group?"
    }

    private fun initializeTitleAsGender() {
        setProgressBarWeight(60f)
        progress_completion_tv.text = "3/5"
        title_onboarding.text = "Select your gender."
    }

    private fun initializeTitleAsEducation() {
        setProgressBarWeight(80f)
        progress_completion_tv.text = "4/5"
        title_onboarding.text = "What's your highest qualification?"
    }

    private fun initializeTitleAsWorkStatus() {
        setProgressBarWeight(100f)
        progress_completion_tv.text = "5/5"
        title_onboarding.text = "What's your work status?"
    }

    private fun nextPage():Boolean{
        if(onboarding_pager.currentItem >=onboarding_pager.adapter?.itemCount!!-1)
        return setPagerData(onboarding_pager.adapter?.itemCount!!)
        else
        onboarding_pager.setCurrentItem(onboarding_pager.currentItem + 1)
        return setPagerData(onboarding_pager.currentItem)
    }

    private fun setPagerData(item:Int):Boolean {
        when (item){
            0 -> {
                showHideBackIcon(false)
                initializeTitleAsName()
            }
            1 -> initializeTitleAsAge()
            2 -> initializeTitleAsGender()
            3 -> initializeTitleAsEducation()
            4 -> initializeTitleAsWorkStatus()
            5 -> {
                viewModel.setOnboardingCompleted()
                saveSharedData(AppConstants.ON_BOARDING_COMPLETED, "true")
                popFragmentFromStack(R.id.onboardingfragment)
                navigateWithAllPopupStack(R.id.mainHomeScreen)
                navigate(R.id.profileFragment)
                return false
            }
        }
        return true
    }

    private fun showHideBackIcon(show: Boolean) {
        backpress_icon.visibility = if (show)  View.VISIBLE else View.INVISIBLE
    }

    private fun listeners() {

        next.setOnClickListener() {
            showHideBackIcon(true)
            saveDataToDB(onboarding_pager.currentItem)
            viewModel.getProfileData()
        }
        backpress_icon.setOnClickListener() {
            backPage()
            setLiveDataListItems()
        }
    }

    private fun disableViewPagerScroll() {
        onboarding_pager.isUserInputEnabled = false
    }

    private fun backPage() {
        onboarding_pager.setCurrentItem(onboarding_pager.currentItem - 1)
        setPagerData(onboarding_pager.currentItem)
    }

    private fun saveDataToDB(currentItem: Int) {
        when (currentItem) {
            0 -> viewModel.saveUserName(onboarding_pager.getChildAt(0).findViewById<EditText>(R.id.user_name).text.toString())
            1 -> viewModel.saveAgeGroup(getSelectedDataFromRecycler(1))
            2 -> viewModel.selectYourGender(getSelectedDataFromRecycler(2))
            3 -> viewModel.saveHighestQualification(getSelectedDataFromRecycler(3))
            4 -> viewModel.saveWorkStatus(getSelectedDataFromRecycler(4))
        }
    }

    private fun getSelectedDataFromRecycler(position: Int): String {
        return allRecyclerViews.get(position - 1).getChildAt(0)?.findViewById<TextView>(R.id.item)
            ?.text.toString()
    }

    private fun setProgressBarWeight(weight: Float) {
        var params =
            progress_bar_view.getLayoutParams() as LinearLayout.LayoutParams;
        params.weight = weight;
        progress_bar_view.layoutParams = params
    }

    var allRecyclerViews = ArrayList<RecyclerView>()
    private fun initializePager() {
        disableViewPagerScroll()
        val recyclerGenericAdapter: RecyclerGenericAdapter<ArrayList<String>> =
            RecyclerGenericAdapter<ArrayList<String>>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item -> showToast("") },
                RecyclerGenericAdapter.ItemInterface<ArrayList<String>?> { obj, viewHolder, position ->
                    if (position != 0) {
                        getView(viewHolder, R.id.user_name).visibility = View.GONE
                        getView(viewHolder, R.id.first_item_indicator).visibility = View.VISIBLE
                        var param = viewHolder.getView(R.id.first_item_indicator).layoutParams
                        setRecylerData(
                            getRecyclerView(viewHolder, R.id.onboarding_rv),
                            obj,
                            position,
                            param.height
                        )

                    } else {
                        viewHolder.getView(R.id.first_item_indicator).visibility = View.GONE
                    }
                })!!
        recyclerGenericAdapter.setList(viewModel.getOnboardingData())
        recyclerGenericAdapter.setLayout(R.layout.onboarding_pager_item)
        onboarding_pager.adapter = recyclerGenericAdapter
        onboarding_pager.offscreenPageLimit = 5
    }

    private val visibleThreshold = 50
    private fun setRecylerData(
        recyclerView: RecyclerView,
        dataArr: ArrayList<String>?,
        pagerPosition: Int,
        heightPagerItem: Int
    ) {
        val params: ViewGroup.LayoutParams = recyclerView.getLayoutParams()
        params.height = heightPagerItem * (dataArr?.size!!)
        recyclerView.setLayoutParams(params)

        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))

        val recyclerGenericAdapter: RecyclerGenericAdapter<String> =
            RecyclerGenericAdapter<String>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item -> },
                RecyclerGenericAdapter.ItemInterface<String?> { obj, viewHolder, position ->
                    var tv = getTextView(viewHolder, R.id.item)
                    tv.text = obj
                    if (dataArr.size >= position + 1 && position == 0) {
                        setTextViewColor(tv, R.color.onboarding_rv_item_color)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Bold.ttf"
                        )
                        tv.setTypeface(face)
                    } else if (dataArr.size >= position + 1 && position == 1) {
                        setTextViewColor(tv, R.color.onboarding_rv_item_color_60)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Regular.ttf"
                        )
                        tv.setTypeface(face)
                    } else if (dataArr.size >= position + 1 && position == 2) {
                        setTextViewColor(tv, R.color.onboarding_rv_item_color_40)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Regular.ttf"
                        )
                        tv.setTypeface(face)
                    } else if (dataArr.size >= position + 1 && position == 3) {
                        setTextViewColor(tv, R.color.onboarding_rv_item_color_10)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Regular.ttf"
                        )
                        tv.setTypeface(face)
                    } else {
                        setTextViewColor(tv, R.color.onboarding_rv_item_color_10)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Regular.ttf"
                        )
                        tv.setTypeface(face)
                    }
                })!!
        recyclerGenericAdapter.setList(dataArr)
        recyclerGenericAdapter.setLayout(R.layout.onboarding_list_item)
        recyclerView.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.adapter = recyclerGenericAdapter
        recyclerView.stopScroll()
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
                if (totalItemCount!! <= (lastVisibleItem + visibleThreshold)) {
                    if (pagerPosition == 1)
                        recyclerGenericAdapter.list.addAll(viewModel.getAgeOptions())
                    else if (pagerPosition == 2)
                        recyclerGenericAdapter.list.addAll(viewModel.getGenderOptions())
                    if (pagerPosition == 3)
                        recyclerGenericAdapter.list.addAll(viewModel.getEducationOption())
                    if (pagerPosition == 4)
                        recyclerGenericAdapter.list.addAll(viewModel.getWorkStatusOptions())
                    recyclerGenericAdapter.notifyDataSetChanged()
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

                try {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            var positionView =
                                0//(recyclerView.getLayoutManager() as LinearLayoutManager).findFirstVisibleItemPosition();
                            if (true) {
                                var firstItem =
                                    recyclerView.getChildAt(positionView)
                                        .findViewById<TextView>(R.id.item)
                                setTextViewColor(firstItem, R.color.onboarding_rv_item_color)
                                val face = Typeface.createFromAsset(
                                    activity?.getAssets(),
                                    "fonts/Lato-Bold.ttf"
                                )
                                firstItem.setTypeface(face)

                            }
                            if (true) {
                                var firstItem =
                                    recyclerView.getChildAt(positionView + 1)
                                        .findViewById<TextView>(R.id.item)
                                setTextViewColor(firstItem, R.color.onboarding_rv_item_color_60)
                                val face = Typeface.createFromAsset(
                                    activity?.getAssets(),
                                    "fonts/Lato-Regular.ttf"
                                )
                                firstItem.setTypeface(face)
                            }
                            if (true) {
                                var firstItem =
                                    recyclerView.getChildAt(positionView + 2)
                                        .findViewById<TextView>(R.id.item)
                                setTextViewColor(firstItem, R.color.onboarding_rv_item_color_40)
                                val face = Typeface.createFromAsset(
                                    activity?.getAssets(),
                                    "fonts/Lato-Regular.ttf"
                                )
                                firstItem.setTypeface(face)
                            }
                            if (true) {
                                var firstItem =
                                    recyclerView.getChildAt(positionView + 3)
                                        .findViewById<TextView>(R.id.item)
                                setTextViewColor(firstItem, R.color.onboarding_rv_item_color_10)
                                val face = Typeface.createFromAsset(
                                    activity?.getAssets(),
                                    "fonts/Lato-Regular.ttf"
                                )
                                firstItem.setTypeface(face)
                            }
                        }
                        RecyclerView.SCROLL_STATE_DRAGGING -> println("Scrolling now")
                        RecyclerView.SCROLL_STATE_SETTLING -> println("Scroll Settling")
                    }
                } catch (e: Exception) {
                }
            }
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//            }
        }
        recyclerView.addOnScrollListener(scrollListener)
        var linearSnapHelper: SnapHelper = SnapHelperOneByOne(Gravity.TOP);
        linearSnapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(
            SpeedyLinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        allRecyclerViews.add(recyclerView)
    }
    class SpeedyLinearLayoutManager : LinearLayoutManager {
        constructor(context: Context?) : super(context) {}
        constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
            context,
            orientation,
            reverseLayout
        ) {
        }

        constructor(
            context: Context?,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
        ) : super(context, attrs, defStyleAttr, defStyleRes) {
        }

        override fun smoothScrollToPosition(
            recyclerView: RecyclerView,
            state: RecyclerView.State,
            position: Int
        ) {
            val linearSmoothScroller: LinearSmoothScroller =
                object : LinearSmoothScroller(recyclerView.context) {
                    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                        return super.computeScrollVectorForPosition(targetPosition)
                    }

                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return -10f
                    }
                }
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }

        companion object {
            private const val MILLISECONDS_PER_INCH = 10000f //default is 25f (bigger = slower)
        }
    }
}