package com.gigforce.common_ui.components.cells

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.StandardActionCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.cell_standard_action_card.view.*
import javax.inject.Inject

enum class ColorOptions(val value: Int) {

    Default(0),
    White(200),
    LightPink(201),
    LightBlue(202),
    Lipstick(203),
    GreyLight(204);

    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}

enum class TextColorOptions(val value: Int) {

    Default(0),
    White(200),
    LightPink(201),
    LightBlue(202),
    Lipstick(203),
    GreyLight(204);


    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}

@AndroidEntryPoint
open class StandardActionCardComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder {

    private var buttonClickListener: OnClickListener? = null
    private var secondButtonClickListener: OnClickListener? = null
    private var bgColorOption: ColorOptions = ColorOptions.Default
    private var textColorOption: TextColorOptions = TextColorOptions.Default

    @Inject
    lateinit var navigation: INavigation

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_standard_action_card, this, true)

        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(it, R.styleable.StandardActionCardComponent, 0, 0)
            this.bgColorOption = ColorOptions.getByValue(
                styledAttributeSet.getInt(
                    R.styleable.StandardActionCardComponent_bgcolor,
                    0
                )
            )
            this.textColorOption = TextColorOptions.getByValue(
                styledAttributeSet.getInt(
                    R.styleable.StandardActionCardComponent_textcolor,
                    0
                )
            )
            backgroundColor = this.bgColorOption
            textColor = this.textColorOption

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
                ColorOptions.White -> R.color.white
                ColorOptions.LightPink -> R.color.light_pink
                ColorOptions.LightBlue -> R.color.light_blue
                ColorOptions.Lipstick -> R.color.lipstick
                ColorOptions.GreyLight -> R.color.greyLight
                else -> R.color.white
            }
            setBackgroundColor(ContextCompat.getColor(context, selectedColor))
        }

    var textColor: TextColorOptions
        get() = textColor
        set(value) {
            val selectedColor = when (value) {
                TextColorOptions.White -> R.color.white
                TextColorOptions.LightPink -> R.color.light_pink
                TextColorOptions.LightBlue -> R.color.light_blue
                TextColorOptions.Lipstick -> R.color.lipstick
                TextColorOptions.GreyLight -> R.color.greyLight
                else -> 0
            }
            if (selectedColor != 0) {
                tv_title.setTextColor(ContextCompat.getColor(context, selectedColor))
                tv_desc.setTextColor(ContextCompat.getColor(context, selectedColor))
            }
        }

    fun setPrimaryActionClick(buttonClickListener: OnClickListener) {
        this.buttonClickListener = buttonClickListener
    }

    fun setSecondryActionClick(secondButtonClickListener: OnClickListener) {
        this.secondButtonClickListener = secondButtonClickListener
    }

    fun setImageFromUrl(url: String, imageType: String) {
        GlideApp.with(context.applicationContext)
            .load(url)
            .into(if (imageType.equals("image")) image else icon)
    }

    fun setImageFromFirebaseUrl(storageReference: StorageReference, imageType: String) {
        GlideApp.with(context.applicationContext)
            .load(storageReference)
            .into(if (imageType.equals("image")) image else icon)
    }

    fun setImage(data: StandardActionCardDVM) {

        if(data.imageType.equals("image"))
        {
            image.visible()
            icon.gone()
        } else {
            image.gone()
            icon.visible()
        }

        if (data.image is Int && data.image != -1) {
            image.setImageResource(data.image)
        } else {
            data.imageUrl?.let {
                if (it.isNotBlank() && it.isNotEmpty()) {
                    if (it.contains("http") || it.contains("https")) {
                        setImageFromUrl(it, data.imageType)
                    } else {
                        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(it)
                        setImageFromFirebaseUrl(gsReference,data.imageType)
                    }
                }
                return
            }
        }
    }

    var applyMargin: Boolean
        get() = applyMargin
        set(value) {
            val params =
                LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
            if (value) {
                val left: Int = getPixelValue(16)//context.resources.getDimension(R.dimen.size4))
                val top: Int = getPixelValue(16)
                val right: Int = getPixelValue(16)
                val bottom: Int = getPixelValue(16)
                params.setMargins(left, top, right, bottom)
                layoutParams = params
            } else {
                val left: Int = getPixelValue(0)//context.resources.getDimension(R.dimen.size4))
                val top: Int = getPixelValue(0)
                val right: Int = getPixelValue(0)
                val bottom: Int = getPixelValue(0)
                params.setMargins(left, top, right, bottom)
                layoutParams = params
            }
        }

    fun getPixelValue(value: Int): Int {
        return (value * Resources.getSystem().displayMetrics.density).toInt()
    }


    private fun playvideo(link: String?) {
        val appIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(link))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(link)
        )
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    override fun bind(data: Any?) {
        primary_action.gone()
        secondary_action.gone()
        applyMargin = false
        if (data is StandardActionCardDVM) {
            tv_title.text = data.title
            tv_desc.text = data.desc
            setImage(data)

            data.action1?.let {
                primary_action.visible()
                primary_action.text = it.title ?: ""
                primary_action.setOnClickListener { it2 ->
                    it.type?.let { it1 ->
                        when (it1) {
                            "youtube_video" -> playvideo(it.link)
                            "navigation" -> navigation.navigateTo(it.navPath ?: "",data.bundle)
                        }
                    }
                }
            } ?: primary_action.gone()

            data.action2?.let {
                secondary_action.isVisible =
                    it.title?.isNotEmpty() ?: false && it.title?.isNotBlank() ?: false
                secondary_action.text = it.title ?: ""
            } ?: secondary_action.gone()

            backgroundColor = ColorOptions.getByValue(data.bgcolor.toInt())
            textColor = TextColorOptions.getByValue(data.textColor)
            applyMargin = data.marginRequired

        }
    }
}
