package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.synthetic.main.simple_card_component.view.*

enum class SubTitleColor(val value : String){
    DEFAULT(""),
    GREEN("GREEN"),
    YELLOW("YELLOW"),
    RED("RED")
}

class SimpleCardComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    val view : View
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        view = LayoutInflater.from(context).inflate(R.layout.simple_card_component, this, true)
        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(it, R.styleable.SimpleCardComponent,0,0)
            val title = styledAttributeSet.getString(R.styleable.SimpleCardComponent_title)?:""
            val subTitle = styledAttributeSet.getString(R.styleable.SimpleCardComponent_caption)?:""
            setTitle(title)
            setSubTitle(subTitle)
        }

        view.setOnClickListener{
            customOnclickListner?.onClick(it)
            if(right_image.isVisible){
                right_image.gone()
            }  else{
                right_image.visible()
            }
        }

    }


    fun setTitle(title1 : String){
        title.text = title1
    }

    fun setSubTitle(subtitle1 : String){
        subtitle.text = subtitle1
    }

    var customOnclickListner : OnClickListener?=null

    open fun setOnclickListner(onclickListner : OnClickListener ){
        this.customOnclickListner = onclickListner
    }

    fun getIsSelected() : Boolean =
        right_image.isVisible

    fun setViewSelected(selected : Boolean){
        if(selected) right_image.visible() else right_image.gone()
    }
    fun setLeftImage(drawable: Int){
        left_img.setImageDrawable(resources.getDrawable(drawable))
    }

    fun setVerified(color: String){
//        if (verified) verified_img.visible() else verified_img.gone()
        if(color.toUpperCase().equals(SubTitleColor.GREEN.value)) {
            subtitle.setTextColor(resources.getColor(R.color.green))
        } else if(color.toUpperCase().equals(SubTitleColor.YELLOW.value)) {
            subtitle.setTextColor(resources.getColor(R.color.yellow))
        } else if(color.toUpperCase().equals(SubTitleColor.RED.value)) {
            subtitle.setTextColor(resources.getColor(R.color.app_red))
        }

    }

    override fun bind(data: Any?) {
        if(data is SimpleCardDVM){
            setTitle(data.title)
            setSubTitle(data.subtitle)
            setViewSelected(data.isSelected)
            setLeftImage(data.image)
            data.color?.let { setVerified(it) }
            view.setOnClickListener(null)
            view.setOnClickListener{
                customOnclickListner?.onClick(it)
                if(right_image.isVisible){
                    right_image.gone()
                    data.isSelected = false
                }  else{
                    right_image.visible()
                    data.isSelected = true
                }
            }
        }
    }




}