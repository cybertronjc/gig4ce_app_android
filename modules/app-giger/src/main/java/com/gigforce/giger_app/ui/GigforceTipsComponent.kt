package com.gigforce.giger_app.ui

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.core.AppConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toBundle
import com.gigforce.giger_app.R
import com.gigforce.giger_app.dataviewmodel.GigForceTipsDVM
import com.gigforce.landing_screen.landingscreen.LandingPageConstants

class GigforceTipsComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {
    private val TIP_SCROLL_TIME_OUT: Long = 10_000 // 10 sec
    var forward = true
    init {
        this.setOrientationAndRows(0, 1)
    }

    override fun bind(data: Any?) {
        if(data is GigForceTipsDVM){
            if(data.allTips.isNotEmpty()) {
                data.allTips.map { it.bundle = mapOf(
                    LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN to true,
                    LandingPageConstants.INTENT_EXTRA_ACTION to AppConstants.ACTION_OPEN_EDIT_LANGUAGE_BOTTOM_SHEET
                ).toBundle() }
                setCollection(data.allTips)
                if (getFeatureRV().onFlingListener == null) {
                    var pagerHelper = PagerSnapHelper()
                    pagerHelper.attachToRecyclerView(getFeatureRV())
                }
                var handler = Handler()
                val runnableCode = object : Runnable {
                    override fun run() {
                        try {
                            var currentVisiblePosition =
                                (getFeatureRV().layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//                        if ((gigforce_tip.adapter as RecyclerGenericAdapter<TitleSubtitleModel>).list.size == currentVisiblePosition + 1) {
//                            forward = false
//                        }
                            if ((getFeatureRV().adapter?.itemCount) == currentVisiblePosition + 1) {
                                forward = false
                            }
                            if (currentVisiblePosition == 0) {
                                forward = true
                            }
                            if (!forward) {
                                getFeatureRV().smoothScrollToPosition(currentVisiblePosition - 1)
                            } else
                                getFeatureRV().smoothScrollToPosition(currentVisiblePosition + 1)


                            handler.postDelayed(this, TIP_SCROLL_TIME_OUT)
                        } catch (e: Exception) {

                        }

                    }
                }
                handler.postDelayed(runnableCode, TIP_SCROLL_TIME_OUT)

            }
            else this.findViewById<ConstraintLayout>(R.id.top_cl).gone()
        }
    }
}