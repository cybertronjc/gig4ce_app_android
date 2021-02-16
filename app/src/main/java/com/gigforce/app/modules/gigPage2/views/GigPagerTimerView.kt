package com.gigforce.app.modules.gigPage2.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage2.models.GigStatus
import com.google.android.material.card.MaterialCardView

class GigPagerTimerView(
        context: Context,
        attrs: AttributeSet
) : ConstraintLayout(
        context,
        attrs
) {

    //Views
    private lateinit var rootCardView: MaterialCardView

    private lateinit var gigDateTV: TextView
    private lateinit var gigTimerTV: TextView
    private lateinit var gigCheckInTimeTV: TextView

    private lateinit var gigTimerAndDetailsLayout: View
    private lateinit var gigAttendanceDetailsLayout: View

    init {
        val layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(
                R.layout.fragment_gig_page_2_timer_layout,
                this,
                false
        )
        findViews()
    }

    private fun findViews() {
        gigDateTV = findViewById(R.id.gig_date_tv)
    }

    fun setGigData(gig: Gig) {
        val gigStatus = GigStatus.fromGig(gig)

        when (gigStatus) {
            GigStatus.UPCOMING -> TODO()
            GigStatus.DECLINED -> TODO()
            GigStatus.CANCELLED -> showGigCancelled(gig)
            GigStatus.ONGOING -> TODO()
            GigStatus.PENDING -> TODO()
            GigStatus.NO_SHOW -> TODO()
            GigStatus.COMPLETED -> TODO()
            GigStatus.MISSED -> TODO()
        }
    }

    private fun showGigCancelled(gig: Gig) {
        //todo cancell any layout if

        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

    }


}