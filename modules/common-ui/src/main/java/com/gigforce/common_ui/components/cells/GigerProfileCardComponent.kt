package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.gigforce.common_ui.R
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.GlideApp
import kotlinx.android.synthetic.main.simple_card_component.view.*

class GigerProfileCardComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs){
    val view : View
    private val profileImg: ImageView
    private val logoImg: ImageView
    private val gigerName: TextView
    private val gigerNumber: TextView
    private val jobProfileName: TextView
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context).inflate(R.layout.giger_profile_card_component_layout, this, true)
        profileImg = this.findViewById(R.id.gigerImg)
        logoImg = this.findViewById(R.id.jobProfileLogo)
        gigerName = this.findViewById(R.id.gigerName)
        gigerNumber = this.findViewById(R.id.gigerNumber)
        jobProfileName = this.findViewById(R.id.jobProfileTitle)
    }

    fun setProfilePicture(image: String){
        GlideApp.with(context)
            .load(image)
            .placeholder(ShimmerHelper.getShimmerDrawable())
            .into(profileImg)
    }

    fun setJobProfileLogo(image: String){
        GlideApp.with(context)
            .load(image)
            .placeholder(ShimmerHelper.getShimmerDrawable())
            .into(logoImg)
    }

    fun setProfileCard(gigerProfileCardDVM: GigerProfileCardDVM){
        gigerProfileCardDVM?.let {
            gigerName.text = it.name
            gigerNumber.text = it.number
            jobProfileName.text = it.jobProfileName
            setProfilePicture(it.gigerImg)
            setJobProfileLogo(it.jobProfileLogo)
        }
    }
}