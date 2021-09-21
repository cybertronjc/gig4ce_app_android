package com.gigforce.giger_gigs.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.giger_gigs.R
import com.google.android.material.card.MaterialCardView

class GigStatusCardView(
    context: Context,
    attrs: AttributeSet
) : MaterialCardView(
    context,
    attrs
) {

    private lateinit var statusCardView: MaterialCardView

    private lateinit var gigStatusIV: ImageView
    private lateinit var gigStatusTV: TextView

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(
            R.layout.fragment_gig_page_2_status_card_view,
            this,
            true
        )
        findViews(view)
    }

    private fun findViews(view: View) {
        statusCardView = view.findViewById(R.id.status_card_view)

        gigStatusTV = view.findViewById(R.id.gig_status_tv)
        gigStatusIV = view.findViewById(R.id.gig_status_iv)
    }

    fun setGigData(status: GigStatus) {

        gigStatusTV.text = resources.getString(status.getStatusStringRes())
        Glide.with(context).load(status.getIconForStatus()).into(gigStatusIV)
        statusCardView.strokeColor = ResourcesCompat.getColor(
            context.resources,
            status.getColorForStatus(),
            null
        )
    }
}