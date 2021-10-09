package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import com.gigforce.common_ui.R
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.viewdatamodels.PendingJoiningItemDVM
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JoiningPendingCardComponent(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    @Inject
    lateinit var navigation : INavigation

    private val jobProfileNameTV: TextView
    private val locationNameTV: TextView
    private val expectedDateTV: TextView
    private val completeJoiningBtn: MaterialButton
    private val image : GigforceImageView

    private var pendingJoining : PendingJoiningItemDVM? = null

    init {
        this.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        LayoutInflater.from(context).inflate(
            R.layout.component_pending_joining_item,
            this,
            true
        )
        jobProfileNameTV = this.findViewById(R.id.job_profile_name_textview)
        locationNameTV = this.findViewById(R.id.location_textview)
        expectedDateTV = this.findViewById(R.id.joining_date_textview)
        completeJoiningBtn = this.findViewById(R.id.complete_joining_button)
        image = this.findViewById(R.id.gigforceImageView)

        completeJoiningBtn.setOnClickListener(this)
    }

    override fun bind(data: Any?) {

        (data as PendingJoiningItemDVM?)?.let{
            pendingJoining = it

            jobProfileNameTV.text = it.jobProfileName
            expectedDateTV.text = it.expectedStartDate
            locationNameTV.text = "Location - ${it.location}"

            if(it.image.isNotBlank())
             image.loadImageIfUrlElseTryFirebaseStorage(it.image)
        }
    }

    override fun onClick(v: View?) {

        pendingJoining?.let {
            navigation.navigateTo(
                "client_activation/applicationClientActivation", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to it.jobProfileId,
                    StringConstants.JOB_PROFILE_TITLE.value to it.jobProfileName
                )
            )
        }
    }
}