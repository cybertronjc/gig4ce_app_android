package com.gigforce.client_activation.client_activation.explore

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileDVM
import com.gigforce.client_activation.client_activation.models.JpExplore
import com.gigforce.client_activation.databinding.LayoutJobProfileCardComponentBinding
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible

class JobProfileCardComponent(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), View.OnClickListener {

    private var viewBinding: LayoutJobProfileCardComponentBinding
    var applyNowClickListener: ApplyNowClickListener? = null

    fun setOnApplyNowClickListener(listener: ApplyNowClickListener) {
        this.applyNowClickListener = listener
    }
    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutJobProfileCardComponentBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    }

    fun bindData(jpExplore: JobProfileDVM?) = viewBinding.apply {
        jpExplore?.let {
            if (it.title?.isNotBlank() == true){
                if (it.subTitle?.isNotBlank() == true){
                    gigTitle.text = it.title + " - " + it.subTitle
                }else{
                    gigTitle.text = it.title
                }
            }else {
                gigTitle.text = "Profile N/A"
            }

            Glide.with(context).load(it.cardImage).transition(DrawableTransitionOptions().crossFade()).placeholder(ShimmerHelper.getShimmerDrawable()).into(cardImage)

            if (it.jp_applicationStatus == "")
                gigStatus.gone()
            else{
                gigStatus.visible()}

            gigStatus.text = if (it.jp_applicationStatus == "Interested" || it.jp_applicationStatus == "Inprocess") "Pending" else it.jp_applicationStatus

            var actionButtonText =
                if (it.jp_applicationStatus == "Interested") "Complete Application" else if (it.jp_applicationStatus == "Inprocess") "Complete Application"
                else if (it.jp_applicationStatus == "") "Apply Now"  else ""
            Log.d("actionText", actionButtonText)
            if (actionButtonText == ""){
                dividerOne.invisible()
                applyNow.gone()
                Log.d("empty", "true")}
            else{
                dividerOne.visible()
                Log.d("empty", "true")
                applyNow.visible()
                applyNow.text = actionButtonText}

            applyNow.setOnClickListener {
                applyNowClickListener?.onApplyNowClicked(it, applyNow.text.toString())
            }
        }

    }

    fun setStatusColor(colorStatus: Int){
        viewBinding.gigStatus.setTextColor(colorStatus)
    }

    override fun onClick(p0: View?) {

    }

}

interface ApplyNowClickListener {

    fun onApplyNowClicked(v: View, text: String)
}