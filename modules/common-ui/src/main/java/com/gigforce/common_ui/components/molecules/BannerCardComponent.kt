package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.BannerCardDVM
import com.gigforce.core.IEventTracker
import com.gigforce.core.INavArgsProvider
import com.gigforce.core.IViewHolder
import com.gigforce.core.NavArgs
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BannerCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder, INavArgsProvider {
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

    var data: Any? = null

    override fun getNavArgs(): NavArgs? {
        val data = this.data
        if (data is INavArgsProvider)
            return data.getNavArgs()
        return null
    }

    override fun bind(data: Any?) {
        this.setOnClickListener(null)
        if(data is BannerCardDVM){
            getNavArgs()?.let {
                navigation.navigateTo(it.navPath, it.args)

            }
        }
    }
}