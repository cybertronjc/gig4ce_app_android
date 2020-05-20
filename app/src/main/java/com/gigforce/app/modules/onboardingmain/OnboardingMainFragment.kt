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
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import androidx.recyclerview.widget.SnapHelper
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper
import kotlinx.android.synthetic.main.onboarding_main_fragment.*


class OnboardingMainFragment : BaseFragment() {

    companion object {
        fun newInstance() = OnboardingMainFragment()
    }

    private lateinit var viewModel: OnboardingMainViewModel

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

        val hashMapForRV = HashMap<Int, Boolean>()
    private fun initializeViews() {
        for(i in 0..viewModel.getOnboardingData().size)
            hashMapForRV.put(i,false)
        initializePager()
        listeners()
    }

    private fun listeners() {
        next.setOnClickListener(){
            onboarding_pager.setCurrentItem(onboarding_pager.currentItem+1)
        }
    }

    private fun initializePager() {
        val recyclerGenericAdapter: RecyclerGenericAdapter<ArrayList<String>> =
            RecyclerGenericAdapter<ArrayList<String>>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item -> showToast("abc")},
                RecyclerGenericAdapter.ItemInterface<ArrayList<String>?> { obj, viewHolder, position ->
                    if(position!=0) {
                        getView(viewHolder, R.id.first_item_indicator).visibility = View.VISIBLE
                        var param = viewHolder.getView(R.id.first_item_indicator).layoutParams
                        setRecylerData(getRecyclerView(viewHolder, R.id.onboarding_rv), obj,position,param.height)
                    }
                    else{
                        viewHolder.getView(R.id.first_item_indicator).visibility = View.GONE
                    }
                })!!
        recyclerGenericAdapter.setList(viewModel.getOnboardingData())
        recyclerGenericAdapter.setLayout(R.layout.onboarding_pager_item)
        onboarding_pager.adapter = recyclerGenericAdapter
        onboarding_pager.setOnTouchListener(OnTouchListener { v, event -> false })
    }

    private val visibleThreshold = 4
    private fun setRecylerData(recyclerView: RecyclerView, dataArr: ArrayList<String>?,pagerPosition:Int,heightPagerItem:Int) {
            val params: ViewGroup.LayoutParams = recyclerView.getLayoutParams()
            params.height = heightPagerItem * (dataArr?.size!!)
            recyclerView.setLayoutParams(params)
        val recyclerGenericAdapter: RecyclerGenericAdapter<String> =
            RecyclerGenericAdapter<String>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item -> showToast("abc")},
                RecyclerGenericAdapter.ItemInterface<String?> { obj, viewHolder, position ->
                    var tv = getTextView(viewHolder,R.id.item)
                    tv.text = obj
                    if(dataArr.size==position+1 && position==0){
                        setTextViewColor(tv,R.color.onboarding_rv_item_color)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Bold.ttf"
                        )
                        tv.setTypeface(face)
                    }else if(dataArr.size==position+1 &&position==1){
                        setTextViewColor(tv,R.color.onboarding_rv_item_color_60)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Regular.ttf"
                        )
                        tv.setTypeface(face)
                    }else if(dataArr.size==position+1 && position==2){
                        setTextViewColor(tv,R.color.onboarding_rv_item_color_40)
                        val face = Typeface.createFromAsset(
                            activity?.getAssets(),
                            "fonts/Lato-Regular.ttf"
                        )
                        tv.setTypeface(face)
                    }else if(dataArr.size==position+1 && position==3){
                        setTextViewColor(tv,R.color.onboarding_rv_item_color_10)
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

        var scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if(true) {
                            var firstItem = recyclerView.getChildAt(0).findViewById<TextView>(R.id.item)
                            setTextViewColor(firstItem, R.color.onboarding_rv_item_color)
                            val face = Typeface.createFromAsset(
                                activity?.getAssets(),
                                "fonts/Lato-Bold.ttf"
                            )
                            firstItem.setTypeface(face)
                        }
                        if(true){
                            var firstItem = recyclerView.getChildAt(1).findViewById<TextView>(R.id.item)
                            setTextViewColor(firstItem,R.color.onboarding_rv_item_color_60)
                            val face = Typeface.createFromAsset(
                                activity?.getAssets(),
                                "fonts/Lato-Regular.ttf"
                            )
                            firstItem.setTypeface(face)
                        }
                        if(true){
                            var firstItem = recyclerView.getChildAt(2).findViewById<TextView>(R.id.item)
                            setTextViewColor(firstItem,R.color.onboarding_rv_item_color_40)
                            val face = Typeface.createFromAsset(
                                activity?.getAssets(),
                                "fonts/Lato-Regular.ttf"
                            )
                            firstItem.setTypeface(face)
                        }
                        if(true){
                            var firstItem = recyclerView.getChildAt(3).findViewById<TextView>(R.id.item)
                            setTextViewColor(firstItem,R.color.onboarding_rv_item_color_10)
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

                val totalItemCount = recyclerView!!.layoutManager?.itemCount
                var layoutManager: LinearLayoutManager? = null
                if (layoutManager == null) {
                    layoutManager = recyclerView.layoutManager as LinearLayoutManager
                }
                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
                val lastVisibleItem = layoutManager!!.findLastVisibleItemPosition()
                if (totalItemCount!! <= (lastVisibleItem + visibleThreshold)) {
                    if(pagerPosition==1)
                    recyclerGenericAdapter.list.addAll(viewModel.getAgeOptions())
                    else if(pagerPosition==2)
                        recyclerGenericAdapter.list.addAll(viewModel.getGenderOptions())
                    if(pagerPosition==3)
                        recyclerGenericAdapter.list.addAll(viewModel.getEducationOption())
                    if(pagerPosition==4)
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
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }

        }
        recyclerView.addOnScrollListener(scrollListener)
//        val linearSnapHelper: LinearSnapHelper = LinearSnapHelper()
        val snapHelperTop: SnapHelper = GravitySnapHelper(Gravity.TOP)
        snapHelperTop.attachToRecyclerView(recyclerView)
        recyclerView.setLayoutManager(SpeedyLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))

    }
    class SnapHelperOneByOne : LinearSnapHelper() {
        override fun findTargetSnapPosition(
            layoutManager: RecyclerView.LayoutManager,
            velocityX: Int,
            velocityY: Int
        ): Int {
            if (layoutManager !is ScrollVectorProvider) {
                return RecyclerView.NO_POSITION
            }
            val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
            val myLayoutManager = layoutManager as LinearLayoutManager
            val position1 = myLayoutManager.findFirstVisibleItemPosition()
            val position2 = myLayoutManager.findLastVisibleItemPosition()
            var currentPosition = layoutManager.getPosition(currentView)
            if (velocityX > 400) {
                currentPosition = position2
            } else if (velocityX < 400) {
                currentPosition = position1
            }
            return if (currentPosition == RecyclerView.NO_POSITION) {
                RecyclerView.NO_POSITION
            } else currentPosition
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
                        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
                    }
                }
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }

        companion object {
            private const val MILLISECONDS_PER_INCH = 0.001f //default is 25f (bigger = slower)
        }
    }
}