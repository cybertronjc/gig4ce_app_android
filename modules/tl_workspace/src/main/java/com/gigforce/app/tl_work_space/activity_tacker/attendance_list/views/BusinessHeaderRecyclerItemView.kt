package com.gigforce.app.tl_work_space.activity_tacker.attendance_list.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.activity_tacker.attendance_list.GigerAttendanceUnderManagerViewEvents
import com.gigforce.app.tl_work_space.activity_tacker.attendance_list.StatusFilters
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceRecyclerItemData
import com.gigforce.app.tl_work_space.databinding.RecyclerRowBusinessNameShiftTimeBinding
import com.gigforce.core.IViewHolder

class BusinessHeaderRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    private lateinit var viewBinding: RecyclerRowBusinessNameShiftTimeBinding
    private var viewData: AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData? = null

    init {
        setDefault()
        inflate()
        setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowBusinessNameShiftTimeBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        viewData = null

        (data as AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData?)?.let {
            viewData = it

            viewBinding.activeCountTextview.text = it.activeCount.toString()
            viewBinding.enabledCountTextview.text = it.enabledCount.toString()
            viewBinding.inactiveCountTextview.text = it.inActiveCount.toString()
            viewBinding.companyNameTv.text = it.businessName
            selectSelectedStatus(
                it.currentlySelectedStatus
            )

            if (it.expanded) {

                viewBinding.companyNameTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.pink_text, null)
                )
                animateImageRotationFromCollapsedToExpanded()
            } else {

                viewBinding.companyNameTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey, null)
                )
                animateImageRotationFromExpandedToCollapsed()
            }
        }
    }

    private fun selectSelectedStatus(currentlySelectedStatus: String) {
        when (currentlySelectedStatus) {
            StatusFilters.ENABLED -> {

                viewBinding.enabledCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
                )
                viewBinding.enabledLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
                )

                viewBinding.activeCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
                viewBinding.activeLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )

                viewBinding.inactiveCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
                viewBinding.inactiveLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
            }
            StatusFilters.ACTIVE -> {

                viewBinding.enabledCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
                viewBinding.enabledLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )

                viewBinding.activeCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
                )
                viewBinding.activeLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
                )

                viewBinding.inactiveCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
                viewBinding.inactiveLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
            }
            StatusFilters.INACTIVE -> {

                viewBinding.enabledCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
                viewBinding.enabledLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )

                viewBinding.activeCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )
                viewBinding.activeLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey_medium, null)
                )

                viewBinding.inactiveCountTextview.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
                )
                viewBinding.inactiveLabel.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
                )
            }
        }
    }

    private fun animateImageRotationFromCollapsedToExpanded() {

//        getRotationAnimation().apply {
//            setAnimationListener(object : Animation.AnimationListener {
//
//                override fun onAnimationStart(animation: Animation?) {}
//                override fun onAnimationRepeat(animation: Animation?) {}
//
//                override fun onAnimationEnd(animation: Animation?) {
//
//                    viewBinding.collapseButton.setImageDrawable(
//                        ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_up, null)
//                    )
//                }
//            })
//            viewBinding.collapseButton.startAnimation(this)
//        }

        viewBinding.collapseButton.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_up, null)
        )
    }

    private fun getRotationAnimation(): RotateAnimation {
        return RotateAnimation(
            0.0f,
            180.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        ).apply {
            duration = 500 //2 Sec
            fillAfter = true
        }
    }

    private fun animateImageRotationFromExpandedToCollapsed() {
//        getRotationAnimation().apply {
//
//            setAnimationListener(object : Animation.AnimationListener {
//
//                override fun onAnimationStart(animation: Animation?) {}
//                override fun onAnimationRepeat(animation: Animation?) {}
//
//                override fun onAnimationEnd(animation: Animation?) {
//
//                    viewBinding.collapseButton.setImageDrawable(
//                        ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_drop, null)
//                    )
//                }
//            })
//            viewBinding.collapseButton.startAnimation(this)
//        }

        viewBinding.collapseButton.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_drop, null)
        )
    }

    override fun onClick(
        p0: View?
    ) {
        viewData?.let {
            it.viewModel.handleEvent(
               GigerAttendanceUnderManagerViewEvents.BusinessHeaderClicked(
                    it
                )
            )
        }
    }
}
