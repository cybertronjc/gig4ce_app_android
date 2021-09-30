package com.gigforce.common_ui.components.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.BannerCardDVM
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.extensions.gone
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class BannerCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {
    val title: TextView
    val image: ImageView
    var bannerCardData: BannerCardDVM? = null
    val topLayout: View

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.banner_card_component, this, true)
        title = this.findViewById(R.id.title)
        image = this.findViewById(R.id.background_img)
        topLayout = this.findViewById(R.id.top_layout)
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private fun setImage(imageStr: String) {
        if (imageStr.contains("http") or imageStr.contains("https")) {
            Glide.with(context)
                .load(imageStr)
                .into(image)
        }
    }

    override fun bind(data: Any?) {
        this.setOnClickListener(null)
        if (data is BannerCardDVM) {
            bannerCardData = data
            if (data.image.isNullOrEmpty() || data.apiUrl.isNullOrEmpty()) {
                topLayout.gone()
            } else {
                if (data.image.isNotBlank()) {
                    setImage(data.image)
                }
                if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                    if (data.hi?.title.isNullOrBlank()) {
                        if (data.title.isNullOrBlank()) {
                            title.gone()
                        } else {
                            title.text = data.title
                        }
                    } else {
                        title.text = data.hi?.title
                    }
                } else if (data.title.isNullOrBlank()) {
                    title.gone()
                } else
                    title.text = data.title
            }

            data.getNavArgs()?.let { navArgs ->
                if (title.isVisible)
                    navArgs.args?.putString("title", title.text.toString())
                else {
                    if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                        if (data.hi?.defaultDocTitle.isNullOrBlank()) {
                            if(data.defaultDocTitle.isNullOrBlank()){
                                navArgs.args?.putString("title", resources.getString(R.string.back_to_gigforce_ui))
                            }else {
                                navArgs.args?.putString("title", data.defaultDocTitle)
                            }
                        } else {
                            navArgs.args?.putString("title", data.hi?.defaultDocTitle)
                        }
                    } else {
                        if(data.defaultDocTitle.isNullOrBlank()){
                            navArgs.args?.putString("title", resources.getString(R.string.back_to_gigforce_ui))
                        }else {
                            navArgs.args?.putString("title", data.defaultDocTitle)
                        }
                    }
                }
                topLayout.setOnClickListener {
                    bannerCardData?.let {
                        it.apiUrl?.let {
                            bannerCardData?.docUrl?.let { docUrl ->
                                navigation.navigateToDocViewerActivity(
                                    null,
                                    docUrl,
                                    "banner",
                                    navArgs.args,
                                    context
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}