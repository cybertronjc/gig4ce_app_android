package com.gigforce.common_ui.components.atoms

import android.content.Context
import android.util.AttributeSet
import com.gigforce.common_ui.R
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

enum class BackGroundColorOptions(val value: Int) {

    Default(0),
    White(200),
    LightPink(201),
    LightBlue(202),
    Lipstick(203),
    GRAY(204);


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
    GRAY(204);


    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}
enum class StrokeColorOptions(val value: Int) {

    Default(0),
    White(200),
    LightPink(201),
    LightBlue(202),
    Lipstick(203),
    GRAY(204);


    companion object {
        private val VALUES = values()
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}


@AndroidEntryPoint
class ChipComponent : Chip{
    companion object {
        private const val TAG = "MaterialChipView"
    }

    var chipSelectable = false
    var chipStrokeWidth = 0
    var chipCornerRadius = 0
    var chipStrokeSize = 0
    var chipText = ""
    var chipSelected = false

    private var bgColorOption = BackGroundColorOptions.Default
    private var textColorOption = TextColorOptions.Default
    private var strokeColorOptions = StrokeColorOptions.Default

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs, R.attr.chipComponent){
        //extract attributes
        extractAttributesValues(context, attrs)
    }

    private fun extractAttributesValues(context: Context, attrs: AttributeSet) {
        val styleAttributeSet = context?.obtainStyledAttributes(attrs, R.styleable.ChipComponent)
        try {
            this.bgColorOption = BackGroundColorOptions.getByValue(styleAttributeSet.getInt(R.styleable.ChipComponent_chip_backgroundColor, -1))
            this.textColorOption = TextColorOptions.getByValue(styleAttributeSet.getInt(R.styleable.ChipComponent_chip_textColor, -1))
            this.strokeColorOptions = StrokeColorOptions.getByValue(styleAttributeSet.getInt(R.styleable.ChipComponent_chip_strokeColor, -1))
            chipText = styleAttributeSet.getString(R.styleable.ChipComponent_chip_text).toString()
            chipStrokeSize = styleAttributeSet.getDimension(R.styleable.ChipComponent_chip_strokeSize, 0f).toInt()
            chipCornerRadius = styleAttributeSet.getDimension(R.styleable.ChipComponent_chipCornerRadius, 0f).toInt()
            chipStrokeWidth = styleAttributeSet.getDimension(R.styleable.ChipComponent_chipStrokeWidth, 0f).toInt()
            chipSelected = styleAttributeSet.getBoolean(R.styleable.ChipComponent_chipSelected, false)
            chipSelectable = styleAttributeSet.getBoolean(R.styleable.ChipComponent_chip_selectable, false)

            setText(chipText)
            chipTextColor = textColorOption

            chipBackGroundColor = this.bgColorOption
            chipStrokeColor = this.strokeColorOptions
            chipTextColor = this.textColorOption


        }
        finally {
            styleAttributeSet.recycle()
        }

    }


    var chipBackGroundColor: BackGroundColorOptions
        get() = chipBackGroundColor
    set(value) {
        val selectedBackgroundColor = when(value){
            BackGroundColorOptions.White -> R.color.white
            BackGroundColorOptions.LightPink -> R.color.light_pink
            BackGroundColorOptions.LightBlue -> R.color.light_blue
            BackGroundColorOptions.Lipstick -> R.color.lipstick
            BackGroundColorOptions.GRAY -> R.color.grey
            else -> R.color.app_chip_bck_colors
        }
        this.setChipBackgroundColorResource(selectedBackgroundColor)
    }


    var chipStrokeColor: StrokeColorOptions
    get() = chipStrokeColor
    set(value) {
        val selectedStrokeColor = when(value){
            StrokeColorOptions.White -> R.color.white
            StrokeColorOptions.LightPink -> R.color.light_pink
            StrokeColorOptions.LightBlue -> R.color.light_blue
            StrokeColorOptions.Lipstick -> R.color.lipstick
            StrokeColorOptions.GRAY -> R.color.grey
            else -> R.color.app_enrolled_users_chip_stroke_colors
        }
        this.setChipStrokeColorResource(selectedStrokeColor)
    }

    var chipTextColor: TextColorOptions
        get() = chipTextColor
        set(value) {
            val selectedTextColor = when(value){
                TextColorOptions.White -> R.color.white
                TextColorOptions.LightPink -> R.color.light_pink
                TextColorOptions.LightBlue -> R.color.light_blue
                TextColorOptions.Lipstick -> R.color.lipstick
                TextColorOptions.GRAY -> R.color.grey
                else -> R.color.app_chip_text_colors
            }
            this.setTextColor(selectedTextColor)
        }



//    override fun setChecked(checked: Boolean) {
//        var wasChecked = isChecked()
//        super.setChecked(checked);
//        if(checked){
//            chipTextColor = TextColorOptions.Lipstick
//            chipBackGroundColor = BackGroundColorOptions.Lipstick
//        }
//        else{
//            chipTextColor = TextColorOptions.Default
//            chipBackGroundColor = BackGroundColorOptions.Default
//        }
////        if (wasChecked != checked) {
////            setTextColor(TextColorOptions.Lipstick.value)
////        }
//    }


    fun setData( chipModel : ChipGroupModel){
        setText(chipModel.text)
    }

}


