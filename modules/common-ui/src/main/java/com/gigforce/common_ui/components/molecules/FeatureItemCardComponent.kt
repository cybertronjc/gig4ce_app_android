package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.core.*
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.android.exoplayer2.analytics.AnalyticsListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.feature_item_card.view.*
import javax.inject.Inject

@AndroidEntryPoint
open class FeatureItemCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder, INavArgsProvider
{
    val title: TextView
    val subtitle: TextView
    val image: ImageView

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.feature_item_card, this, true)
        title = this.findViewById(R.id.title)
        subtitle = this.findViewById(R.id.subtitle)
        image = this.findViewById(R.id.imgage)
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    var data:Any? = null

    override fun getNavArgs():NavArgs? {
        val data = this.data
        if(data is INavArgsProvider)
            return data.getNavArgs()
        return null
    }

    override fun bind(data: Any?) {
        this.data = data
        this.setOnClickListener(null)
        if (data is FeatureItemCardDVM) {
            getNavArgs() ?. let {
                this.setOnClickListener{ view ->
                    navigation.navigateTo(it.navPath, it.args)
                    data.eventName?.let {
                        eventTracker.pushEvent(TrackingEventArgs(it, data.props))
                    }

                }
            }
            if(data.isSelectedView) borderFrameLayout.visible() else borderFrameLayout.gone()
            setImage(data)
            setSubtitle(data)

        }
    }

    private fun setImage(data: FeatureItemCardDVM) {
        if (data.image is String) {
            if(data.image.contains("http") or data.image.contains("https")) {
                Glide.with(context)
                    .load(data.image)
                    .into(image)
            }
        } else if (data.image is Int) {
            image.setImageResource(data.image)
        } else {

        }
    }

    private fun setSubtitle(data:FeatureItemCardDVM) {
        if (data.title.isNotBlank())
            title.text = data.title
        else title.gone()
        if (data.subtitle?.isNotBlank() == true) {
            subtitle.text = data.subtitle
        }
        else subtitle.gone()
    }

}