package com.gigforce.common_ui.molecules

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.INavArgsProvider
import com.gigforce.core.INavigationProvider
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.feature_item_card2.view.*
import javax.inject.Inject

@AndroidEntryPoint
class FeatureItemCard2(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.feature_item_card2, this, true)
    }

    @Inject lateinit var navigation:INavigation

    override fun bind(data: Any?) {

        this.setOnClickListener(null)

        if(data is FeatureItemCard2DVM){

            feature_title.text = data.title

            data.getNavArgs() ?. let {
                this.setOnClickListener{ view ->
                    navigation.NavigateTo(it.path, it.args)
                }
            }

            if(data.image is Int){
                feature_icon.setImageResource(data.image)
            }
            else if(data.image is String){
                if(data.image.contains("http")){
                    Glide.with(context)
                        .load(data.image)
                        .into(feature_icon)
                }else{
//                    imgLayout.gone()
                }
            }
            else{
            }
        }

    }
}