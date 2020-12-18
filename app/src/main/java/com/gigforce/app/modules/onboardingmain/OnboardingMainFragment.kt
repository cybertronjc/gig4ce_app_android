package com.gigforce.app.modules.onboardingmain

import android.app.Activity
import android.content.Context
import android.graphics.PointF
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.onboarding_main_fragment.*


class OnboardingMainFragment : BaseFragment() {

    companion object {
        fun newInstance() = OnboardingMainFragment()
    }

    private lateinit var profileData: ProfileData
    private lateinit var viewModel: OnboardingMainViewModel
    private var firstTimeLoad: Boolean = true
    private lateinit var usernameEditText: EditText
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

    var originalLocation = intArrayOf(0, 0);
    private fun initializeViews() {
        onboarding_root_layout.getViewTreeObserver()
            .addOnGlobalLayoutListener(keyboardLayoutListener);
        next.getLocationInWindow(originalLocation)
        initializePager()
        initializeTitleAsName()
        listeners()
        observer()
    }

    var singleTimeDBCall = true
    private fun observer() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            if (profile != null && singleTimeDBCall) {
                singleTimeDBCall = false
                profileData = profile
                if (profile.status) {
                    if (profileData.isonboardingdone) {
                        navigateToLoaderScreen()
                    } else {
                        if (firstTimeLoad) {
                            checkForAlreadyCompletedData()
                            setLiveDataListItems()
                            firstTimeLoad = false
                        } else {
                            if (nextPage())
                                setLiveDataListItems()
                        }
                    }
                } else
                    showToast(profile.errormsg)
            }
        })
    }

    private fun checkNullOrBlank(str: String): Boolean {
        if (str == null || str.equals("")) {
            return true
        }
        return false
    }

    private fun checkForAlreadyCompletedData() {

        if (checkNullOrBlank(profileData.name)) {
            // will keep pager to first page
            showBackIcon(false)
        } else if (checkNullOrBlank(profileData.ageGroup)) {
            showBackIcon(true)
            onboarding_pager.setCurrentItem(1)
            setPagerData(onboarding_pager.currentItem)
            enableNextButton(true)
        } else if (checkNullOrBlank(profileData.gender)) {
            showBackIcon(true)
            onboarding_pager.setCurrentItem(2)
            setPagerData(onboarding_pager.currentItem)
            enableNextButton(true)
        } else if (checkNullOrBlank(profileData.highestEducation)) {
            showBackIcon(true)
            onboarding_pager.setCurrentItem(3)
            setPagerData(onboarding_pager.currentItem)
            enableNextButton(true)
        } else if (checkNullOrBlank(profileData.workStatus)) {
            showBackIcon(true)
            onboarding_pager.setCurrentItem(4)
            setPagerData(onboarding_pager.currentItem)
            enableNextButton(true)
        } else {
            setOnboardingCompleteAndNavigate()
        }
    }

    private fun setLiveDataListItems() {
        when (onboarding_pager.currentItem) {
            0 -> if (profileData.name != null && !profileData.name.equals("")) {
                onboarding_pager.getChildAt(0).findViewById<EditText>(R.id.user_name)
                    .setText(profileData.name)
                enableNextButton(true)
            }
            1 -> if (profileData.ageGroup != null && !profileData.ageGroup.equals("")) {
                setRecyclerItemFromDB(
                    profileData.ageGroup
                )
                enableNextButton(true)
            }
            2 -> if (profileData.gender != null && !profileData.gender.equals("")) {
                setRecyclerItemFromDB(
                    profileData.gender
                )
                enableNextButton(true)
            }
            3 -> if (profileData.highestEducation != null && !profileData.highestEducation.equals("")) {
                setRecyclerItemFromDB(
                    profileData.highestEducation
                )
                enableNextButton(true)
            }
            4 -> if (profileData.workStatus != null && !profileData.workStatus.equals("")) {
                setRecyclerItemFromDB(
                    profileData.workStatus
                )
                enableNextButton(true)
            }
        }
    }

    private fun setRecyclerItemFromDB(data: String) {
        var recyclerView = allRecyclerViews.get(onboarding_pager.currentItem - 1)
        var position = (recyclerView.adapter as RecyclerGenericAdapter<String>).list.indexOf(data)
        (recyclerView.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(position, 0)
        makeRVItemHighlighed(recyclerView)
    }

    private fun setRecyclerItemByClick(clickedPosition: Int) {
        var recyclerView = allRecyclerViews.get(onboarding_pager.currentItem - 1)
        (recyclerView.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(
            clickedPosition,
            0
        )
        try {
            makeRVItemHighlighed(recyclerView)
        } catch (e: Exception) {

        }
    }

    private fun initializeTitleAsName() {
        setProgressBarWeight(20f)
        progress_completion_tv.text = getString(R.string.one_of_five)
        title_onboarding.text = getString(R.string.whats_ur_name)
    }

    private fun initializeTitleAsAge() {
        setProgressBarWeight(40f)
        progress_completion_tv.text = getString(R.string.two_of_five)
        title_onboarding.text = getString(R.string.whats_ur_age)
    }

    private fun initializeTitleAsGender() {
        setProgressBarWeight(60f)
        progress_completion_tv.text = getString(R.string.three_of_five)
        title_onboarding.text = getString(R.string.select_gender)
    }

    private fun initializeTitleAsEducation() {
        setProgressBarWeight(80f)
        progress_completion_tv.text = getString(R.string.four_of_five)
        title_onboarding.text = getString(R.string.highest_qualification)
    }

    private fun initializeTitleAsWorkStatus() {
        setProgressBarWeight(100f)
        progress_completion_tv.text = getString(R.string.five_of_five)
        title_onboarding.text = getString(R.string.whats_ur_work)
    }

    private fun nextPage(): Boolean {
        if (onboarding_pager.currentItem >= onboarding_pager.adapter?.itemCount!! - 1)
            return setPagerData(onboarding_pager.adapter?.itemCount!!)
        else
            onboarding_pager.setCurrentItem(onboarding_pager.currentItem + 1)
//        enableNextButton(false)
        showBackIcon(true)
        return setPagerData(onboarding_pager.currentItem)
    }

    private fun setPagerData(item: Int): Boolean {
        when (item) {
            0 -> {
                showBackIcon(false)
                initializeTitleAsName()
            }
            1 -> initializeTitleAsAge()
            2 -> initializeTitleAsGender()
            3 -> initializeTitleAsEducation()
            4 -> initializeTitleAsWorkStatus()
            5 -> {
                setOnboardingCompleteAndNavigate()
                return false
            }
        }
        return true
    }

    private fun setOnboardingCompleteAndNavigate() {
        val inviteId = sharedDataInterface.getData(StringConstants.INVITE_USER_ID.value)
        viewModel.setOnboardingCompleted(
            inviteId,
            navFragmentsData?.getData()?.getString(StringConstants.ROLE_ID.value) ?: "",
            navFragmentsData?.getData()?.getString(StringConstants.JOB_PROFILE_ID.value) ?: ""
        )
        sharedDataInterface.remove(StringConstants.INVITE_USER_ID.value)
        saveOnBoardingCompleted()
        navigateToLoaderScreen()
    }

    private fun navigateToLoaderScreen() {
//        popFragmentFromStack(R.id.onboardingfragment)
//        navigateWithAllPopupStack(R.id.mainHomeScreen)
//        navigate(R.id.authFlowFragment)
//        navigate(R.id.landinghomefragment)
        navigateWithAllPopupStack(R.id.onboardingLoaderfragment)
    }

    private fun showBackIcon(show: Boolean) {
        backpress_icon.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun listeners() {

        next.setOnClickListener() {
            singleTimeDBCall = true
            saveDataToDB(onboarding_pager.currentItem)
            viewModel.getProfileData()
        }
        backpress_icon.setOnClickListener() {
            backPage()
            setLiveDataListItems()
        }
    }

    private fun enableNextButton(enable: Boolean) {
        next.isEnabled = enable

        if (enable) {
            next.background = resources.getDrawable(R.drawable.app_gradient_button, null);
        } else {
            next.background = resources.getDrawable(R.drawable.app_gradient_button_disabled, null);
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
            0 -> {
                var enteredName =
                    onboarding_pager.getChildAt(0).findViewById<EditText>(R.id.user_name)
                        .text.toString()
                var formattedString = getFormattedString(enteredName)
                viewModel.saveUserName(formattedString.trim())
            }
            1 -> viewModel.saveAgeGroup(getSelectedDataFromRecycler(1))
            2 -> viewModel.selectYourGender(getSelectedDataFromRecycler(2))
            3 -> viewModel.saveHighestQualification(getSelectedDataFromRecycler(3))
            4 -> viewModel.saveWorkStatus(getSelectedDataFromRecycler(4))
        }
    }

    private fun getFormattedString(enteredName: String): String {
        var formattedString = ""
        var arr = enteredName.split(" ")
        for (str in arr) {
            try {
                formattedString += str.substring(
                    0,
                    1
                ).toUpperCase() + str.substring(1).toLowerCase()
            } catch (e: Exception) {

            }
            formattedString += " "
        }
        return formattedString.trim()
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
        val recyclerGenericAdapter: RecyclerGenericAdapter<OnboardingMainViewModel.OnboardingData> =
            RecyclerGenericAdapter<OnboardingMainViewModel.OnboardingData>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<OnboardingMainViewModel.OnboardingData?> { view, position, item ->
//                    showToast(
//                        ""
//                    )
                },
                RecyclerGenericAdapter.ItemInterface<OnboardingMainViewModel.OnboardingData?> { obj, viewHolder, position ->
                    if (position != 0) {
                        getView(viewHolder, R.id.user_name).visibility = View.GONE
                        getView(viewHolder, R.id.first_item_indicator).visibility = View.VISIBLE
                        var param = viewHolder.getView(R.id.first_item_indicator).layoutParams
                        setRecylerData(
                            getRecyclerView(viewHolder, R.id.onboarding_rv),
                            obj?.data,
                            position,
                            param.height,
                            obj?.defaultValue!!
                        )

                    } else {
                        usernameEditText = getEditText(viewHolder, R.id.user_name)
                        if (usernameEditText.text.toString().length <= 3) {
                            enableNextButton(false)
                        }
                        usernameEditText?.addTextChangedListener(object :
                            TextWatcher {
                            override fun afterTextChanged(s: Editable?) {}

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                var str = s.toString().trim()
                                if (str.length >= 3) {
                                    enableNextButton(true)
                                } else {
                                    enableNextButton(false)
                                }
                            }
                        })
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
        heightPagerItem: Int,
        defaultValue: Int
    ) {
        recyclerView.setOnTouchListener(CustomTouchListener(next, requireActivity()))
        val params: ViewGroup.LayoutParams = recyclerView.getLayoutParams()
        params.height = heightPagerItem * (dataArr?.size!!)
        recyclerView.setLayoutParams(params)
        //repeating same data multiple time
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))
        dataArr.addAll(ArrayList<String>(dataArr))

        val recyclerGenericAdapter: RecyclerGenericAdapter<String> =
            RecyclerGenericAdapter<String>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    setRecyclerItemByClick(position)
                    enableNextButton(true)
                },
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
        (recyclerView.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(
            defaultValue,
            0
        )
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

//                try {
//                    when (newState) {
//                        RecyclerView.SCROLL_STATE_IDLE -> makeRVItemHighlighed(recyclerView)
//                        RecyclerView.SCROLL_STATE_DRAGGING -> println("Scrolling now")
//                        RecyclerView.SCROLL_STATE_SETTLING -> println("Scroll Settling")
//                    }
//                } catch (e: Exception) {
//                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                makeRVItemHighlighed(recyclerView)
            }
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

    private fun makeRVItemHighlighed(recyclerView: RecyclerView) {
        try {
            val fontBold = Typeface.createFromAsset(
                activity?.getAssets(),
                "fonts/Lato-Bold.ttf"
            )
            val fontRegular = Typeface.createFromAsset(
                activity?.getAssets(),
                "fonts/Lato-Regular.ttf"
            )
            var positionView = 0

            var firstItem =
                recyclerView.getChildAt(positionView)
                    .findViewById<TextView>(R.id.item)
            setTextViewColor(firstItem, R.color.onboarding_rv_item_color)
            firstItem.setTypeface(fontBold)

            var secondItem =
                recyclerView.getChildAt(positionView + 1)
                    .findViewById<TextView>(R.id.item)
            setTextViewColor(secondItem, R.color.onboarding_rv_item_color_60)
            secondItem.setTypeface(fontRegular)

            var thirdItem =
                recyclerView.getChildAt(positionView + 2)
                    .findViewById<TextView>(R.id.item)
            setTextViewColor(thirdItem, R.color.onboarding_rv_item_color_40)
            thirdItem.setTypeface(fontRegular)

            var fourthItem =
                recyclerView.getChildAt(positionView + 3)
                    .findViewById<TextView>(R.id.item)
            setTextViewColor(fourthItem, R.color.onboarding_rv_item_color_10)
            fourthItem.setTypeface(fontRegular)
        } catch (e: Exception) {
        }
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
                        return 1f
                    }
                }
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }

        companion object {
            private const val MILLISECONDS_PER_INCH = 10000f //default is 25f (bigger = slower)
        }
    }

    //    override fun onBackPressed(): Boolean {
//        showToast("working with softkeyboard")
//        return false
//    }
    private val keyboardLayoutListener = OnGlobalLayoutListener {
        try {
            val heightDiff: Int =
                onboarding_root_layout.getRootView()
                    .getHeight() - onboarding_root_layout.getHeight()
//        val contentViewTop: Int = activity?.getWindow()?.findViewById<View>(Window.ID_ANDROID_CONTENT)?.getTop()!!
//        val broadcastManager =
//            LocalBroadcastManager.getInstance(requireContext())
//        if (heightDiff <= contentViewTop) {
//            onHideKeyboard()
//            val intent = Intent("KeyboardWillHide")
//            broadcastManager.sendBroadcast(intent)
//        } else {
//            val keyboardHeight = heightDiff - contentViewTop
//            onShowKeyboard(keyboardHeight)
//            val intent = Intent("KeyboardWillShow")
//            intent.putExtra("KeyboardHeight", keyboardHeight)
//            broadcastManager.sendBroadcast(intent)
//        }
//        var changedLocation = intArrayOf(0, 0);
//        next.getLocationInWindow(changedLocation)
//        if (changedLocation[1] < originalLocation[1]) {
//            next.visibility = View.INVISIBLE
//        } else {
//            next.visibility = View.VISIBLE
//        }
            if (heightDiff > 300) {
                next.visibility = View.INVISIBLE
            } else {
                next.visibility = View.VISIBLE

            }
        } catch (e: java.lang.Exception) {
        }
    }
    private var isOpened = false
    private fun onShowKeyboard(keyboardHeight: Int) {
        showToast("working open")

    }

    private fun onHideKeyboard() {
        showToast("working")
    }

    class CustomTouchListener(var next: View, var activity: Activity) : View.OnTouchListener {
        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            enableNextButton()
            return false
        }

        private fun enableNextButton() {
            next.isEnabled = true
            next.background = activity.resources.getDrawable(R.drawable.app_gradient_button, null);


        }
    }
}