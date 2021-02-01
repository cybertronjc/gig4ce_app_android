package com.gigforce.common_ui.cells

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.cell_standard_action_card.view.*

enum class ColorOptions(val value: Int) {

    Default(0),
    LightPink(201),
    LightBlue(202),
    Lipstick(203),
    GRAY(204);


    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}

open class StandardActionCardComponent(context: Context, attrs: AttributeSet?) :
        FrameLayout(context, attrs),
        IViewHolder {

    private var buttonClickListener: OnClickListener? = null
    private var secondButtonClickListener: OnClickListener? = null
    private var colorOption: ColorOptions = ColorOptions.Default

    init {
        this.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_standard_action_card, this, true)

        attrs?.let {
            val styledAttributeSet = context.obtainStyledAttributes(it, R.styleable.StandardActionCardComponent, 0, 0)
            this.colorOption = ColorOptions.getByValue(styledAttributeSet.getInt(R.styleable.StandardActionCardComponent_colorOptions, 0))
//            var titleColor = styledAttributeSet.getColor(R.styleable.StandardActionCardComponent_titleTextColor, 0)
//            if(titleColor!=0){
//                tv_title.setTextColor(titleColor)
//            }
//            subtitle.setTextColor(styledAttributeSet.getColor(R.styleable.StandardActionCardComponent_subtitleTextColor, 0))
            backgroundColor = this.colorOption
        }

        primary_action.setOnClickListener {
            buttonClickListener?.onClick(it)
        }
        secondary_action.setOnClickListener {
            secondButtonClickListener?.onClick(it)
        }
    }

    var backgroundColor: ColorOptions
        get() = backgroundColor
        set(value) {
            val selectedColor = when (value) {
                ColorOptions.LightPink -> R.color.light_pink
                ColorOptions.LightBlue -> R.color.light_blue
                ColorOptions.Lipstick -> R.color.lipstick
                ColorOptions.GRAY -> R.color.grey
                else -> R.color.white
            }
            setBackgroundColor(ContextCompat.getColor(context, selectedColor))
        }

    fun setPrimaryActionClick(buttonClickListener: OnClickListener) {
        this.buttonClickListener = buttonClickListener
    }

    fun setSecondryActionClick(secondButtonClickListener: OnClickListener) {
        this.secondButtonClickListener = secondButtonClickListener
    }

    fun setImageFromUrl(url: String) {
        GlideApp.with(context)
                .load(url)
                .into(image)
    }

    fun setImageFromUrl(storageReference: StorageReference) {
        GlideApp.with(context)
                .load(storageReference)
                .into(image)
    }

    fun setImage(data: StandardActionCardDVM) {
        data.imageUrl?.let {
            setImageFromUrl(it)
            return
        }

//        data.imageRes ?. let {
//            feature_icon.setImageResource(data.imageRes)
//        }
//
//        data.image_type ?. let{
//            val firebaseStoragePath = "gs://gigforce-dev.appspot.com/pub/app_icons/ic_${data.image_type}.png"
//            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(firebaseStoragePath)
//            setImageFromUrl(gsReference)
//        }
    }
    var applyMargin: Boolean
        get() = applyMargin
        set(value) {
            if(value) {
                val params =
                        LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT
                        )
                val left: Int = getPixelValue(16)//context.resources.getDimension(R.dimen.size4))
                val top: Int = getPixelValue(16)
                val right: Int = getPixelValue(16)
                val bottom: Int = getPixelValue(16)
                params.setMargins(left, top, right, bottom)
                layoutParams = params
            }
        }

    fun getPixelValue(value: Int): Int {
        return (value * Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun bind(data: Any?) {
        primary_action.gone()
        secondary_action.gone()
        if (data is StandardActionCardDVM) {
            /*if (data.image is String && (data.image as String).contains("http")) {
                Glide.with(context)
                        .load(data.image as String)
                        .into(image)
            } else */if (data.image is Int) {
                image.setImageResource(data.image)
            } else {
            }
            tv_title.text = data.title
            tv_desc.text = data.subtitle

            setImage(data)
            data.action1?.let {
                primary_action.visible()
                primary_action.text = it.title?:""
            }?:primary_action.gone()

            data.action2?.let {
                secondary_action.visible()
                secondary_action.text = it.title?:""
            }?:secondary_action.gone()

            backgroundColor = ColorOptions.getByValue(data.bgcolor)
            applyMargin = data.marginRequired
//            if (data.action.isNotBlank()) {
//                primary_action.text = data.action
//            } else primary_action.gone()
//
//            if (data.secondAction.isNotBlank()) {
//                secondary_action.visible()
//                secondary_action.text = data.secondAction
//            } else secondary_action.gone()
        }
    }

}
