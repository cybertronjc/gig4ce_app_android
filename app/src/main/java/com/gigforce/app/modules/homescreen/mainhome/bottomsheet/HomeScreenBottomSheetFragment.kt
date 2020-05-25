package com.gigforce.app.modules.homescreen.mainhome.bottomsheet

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import kotlinx.android.synthetic.main.home_screen_bottom_sheet_fragment.*
import java.util.ArrayList

class HomeScreenBottomSheetFragment : BaseFragment() {

    companion object {
        fun newInstance() = HomeScreenBottomSheetFragment()
    }

    private lateinit var viewModel: HomeScreenBottomSheetViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.home_screen_bottom_sheet_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeScreenBottomSheetViewModel::class.java)
        initializeBottomSheet()
    }



    private fun initializeBottomSheet() {
//        nsv.setBackground(generateBackgroundWithShadow(nsv,R.color.white,
//            R.dimen.eight_dp,R.color.gray_color,R.dimen.five_dp, Gravity.TOP))
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        initializeUpcomingGigBottomSheet()
        initializeFeaturesBottomSheet()
        initializeLearningBottomSheet()
        initializeAssessmentBottomSheet()
        application_version.text = "version " + getCurrentVersion()
        listener()
    }

    private fun listener() {
        kyc_ll.setOnClickListener() {
            navigate(R.id.verification)
        }
        video_resume.setOnClickListener() {
            navigate(R.id.videoResumeFragment)
        }
        show_upcominggig_layout.setOnClickListener() {
            showKYCAndHideUpcomingLayout(false)
        }
    }

    private fun showKYCAndHideUpcomingLayout(show: Boolean) {
        if (show) {
            kyc_video_resume.visibility = View.VISIBLE
            upcoming_gig_title.visibility = View.GONE
            upcoming_gig_rv.visibility = View.GONE
        } else {
            upcoming_gig_title.visibility = View.VISIBLE
            upcoming_gig_rv.visibility = View.VISIBLE
            kyc_video_resume.visibility = View.GONE
        }
    }

    var width: Int = 0
    private fun initializeUpcomingGigBottomSheet() {
        val itemWidth = ((width / 5) * 4).toInt()
        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        val recyclerGenericAdapter: RecyclerGenericAdapter<UpcomingGigModel> =
            RecyclerGenericAdapter<UpcomingGigModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    showKYCAndHideUpcomingLayout(
                        true
                    )
                },
                RecyclerGenericAdapter.ItemInterface<UpcomingGigModel?> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.card_view).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.card_view).layoutParams = lp
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.upcoming_gig_item)
        upcoming_gig_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        upcoming_gig_rv.adapter = recyclerGenericAdapter

    }

    fun generateBackgroundWithShadow(
        view: View, @ColorRes backgroundColor: Int,
        @DimenRes cornerRadius: Int,
        @ColorRes shadowColor: Int,
        @DimenRes elevation: Int,
        shadowGravity: Int
    ): Drawable? {
        val cornerRadiusValue =
            view.context.resources.getDimension(cornerRadius)
        val elevationValue = view.context.resources.getDimension(elevation).toInt()
        val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)
        val backgroundColorValue = ContextCompat.getColor(view.context, backgroundColor)
        val outerRadius = floatArrayOf(
            cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
            cornerRadiusValue, cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
            cornerRadiusValue
        )
        val backgroundPaint = Paint()
        backgroundPaint.setStyle(Paint.Style.FILL)
        backgroundPaint.setShadowLayer(cornerRadiusValue, 0F, 0F, 0)
        val shapeDrawablePadding = Rect()
        shapeDrawablePadding.left = elevationValue
        shapeDrawablePadding.right = elevationValue
        val DY: Int
        when (shadowGravity) {
            Gravity.CENTER -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue
                DY = 0
            }
            Gravity.TOP -> {
                shapeDrawablePadding.top = elevationValue * 2
                shapeDrawablePadding.bottom = elevationValue
                DY = -1 * elevationValue / 3
            }
            Gravity.BOTTOM -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue * 2
                DY = elevationValue / 3
            }
            else -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue * 2
                DY = elevationValue / 3
            }
        }
        val shapeDrawable = ShapeDrawable()
        shapeDrawable.setPadding(shapeDrawablePadding)
        shapeDrawable.paint.color = backgroundColorValue
        shapeDrawable.paint
            .setShadowLayer(cornerRadiusValue / 3, 0f, DY.toFloat(), shadowColorValue)
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)
        val drawable =
            LayerDrawable(arrayOf<Drawable>(shapeDrawable))
        drawable.setLayerInset(
            0,
            elevationValue,
            elevationValue * 2,
            elevationValue,
            elevationValue * 2
        )
        return drawable
    }

    private fun initializeFeaturesBottomSheet() {
        var datalist: ArrayList<FeatureModel> = ArrayList<FeatureModel>()
        datalist.add(FeatureModel("My Gig", R.drawable.mygig))
        datalist.add(FeatureModel("Explore", R.drawable.ic_search_calendar))
        datalist.add(FeatureModel("Wallet", R.drawable.wallet))
        datalist.add(FeatureModel("Profile", R.drawable.profile))
        datalist.add(FeatureModel("Learning", R.drawable.learning))
        datalist.add(FeatureModel("Settings", R.drawable.settings))
        datalist.add(FeatureModel("Chat", R.drawable.chat))
        datalist.add(FeatureModel("Testing", R.drawable.chat))
        val itemWidth = ((width / 7) * 1.6).toInt()
        val recyclerGenericAdapter: RecyclerGenericAdapter<FeatureModel> =
            RecyclerGenericAdapter<FeatureModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigateToFeature(
                        position
                    )
                },
                RecyclerGenericAdapter.ItemInterface<FeatureModel?> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.card_view).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.card_view).layoutParams = lp
                    getImageView(viewHolder, R.id.feature_icon).setImageResource(obj?.icon!!)
                    getTextView(viewHolder, R.id.feature_title).text = obj.title

                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.feature_item)
        feature_rv.setLayoutManager(
            GridLayoutManager(
                activity, 2,
                GridLayoutManager.HORIZONTAL, false
            )
        );
        feature_rv.adapter = recyclerGenericAdapter
    }

    private fun navigateToFeature(position: Int) {
        when (position) {
            0 -> showToast("")
            1 -> showToast("")
            2 -> showToast("")
            3 -> navigate(R.id.profileFragment)
            4 -> showToast("")
            5 -> navigate(R.id.settingFragment)
            6 -> showToast("")
            7 -> navigate(R.id.onboardingfragment)
        }
    }

    private fun initializeLearningBottomSheet() {
        val itemWidth = ((width / 5) * 3.1).toInt()
        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        val recyclerGenericAdapter: RecyclerGenericAdapter<UpcomingGigModel> =
            RecyclerGenericAdapter<UpcomingGigModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item -> showToast("") },
                RecyclerGenericAdapter.ItemInterface<UpcomingGigModel?> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.card_view).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.card_view).layoutParams = lp
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        learning_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_rv.adapter = recyclerGenericAdapter
    }

    private fun initializeAssessmentBottomSheet() {
        val itemWidth = ((width / 5) * 2.8).toInt()
        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        datalist.add(UpcomingGigModel())
        datalist.add(UpcomingGigModel())
        val recyclerGenericAdapter: RecyclerGenericAdapter<UpcomingGigModel> =
            RecyclerGenericAdapter<UpcomingGigModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item -> showToast("") },
                RecyclerGenericAdapter.ItemInterface<UpcomingGigModel?> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.assessment_cl).layoutParams = lp
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
        assessment_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        assessment_rv.adapter = recyclerGenericAdapter
    }

}