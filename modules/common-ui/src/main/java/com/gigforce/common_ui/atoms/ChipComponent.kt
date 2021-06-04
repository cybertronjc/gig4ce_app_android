package com.gigforce.common_ui.atoms

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.gigforce.common_ui.R
import com.gigforce.common_ui.cells.ColorOptions
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import java.util.jar.Attributes

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

//    var chipSelectListener : OnChipSelectListener? = null
//
//    fun setOnChipSelectListener(listener: OnChipSelectListener){
//        this.chipSelectListener = listener
//    }

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
            this.bgColorOption = BackGroundColorOptions.getByValue(styleAttributeSet.getInt(R.styleable.ChipComponent_chip_backgroundColor, 0))
            this.textColorOption = TextColorOptions.getByValue(styleAttributeSet.getInt(R.styleable.ChipComponent_chip_textColor, 0))
            this.strokeColorOptions = StrokeColorOptions.getByValue(styleAttributeSet.getInt(R.styleable.ChipComponent_chip_strokeColor, 0))
            chipText = styleAttributeSet.getString(R.styleable.ChipComponent_chip_text).toString()
            chipStrokeSize = styleAttributeSet.getDimension(R.styleable.ChipComponent_chip_strokeSize, 0f).toInt()
            chipCornerRadius = styleAttributeSet.getDimension(R.styleable.ChipComponent_chipCornerRadius, 0f).toInt()
            chipStrokeWidth = styleAttributeSet.getDimension(R.styleable.ChipComponent_chipStrokeWidth, 0f).toInt()
            chipSelected = styleAttributeSet.getBoolean(R.styleable.ChipComponent_chipSelected, false)
            chipSelectable = styleAttributeSet.getBoolean(R.styleable.ChipComponent_chip_selectable, false)

            setText(chipText)
            setTextColor(textColorOption.value)

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
            else -> R.color.white
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
            else -> R.color.white
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
                else -> R.color.white
            }
            this.setTextColor(selectedTextColor)
        }

    override fun setChecked(checked: Boolean) {
        var wasChecked = isChecked()
        super.setChecked(checked);

        if (wasChecked != checked) {
//            chipSelectListener?.onChipSelect(rootView, checked)
            setTextColor(TextColorOptions.Lipstick.value)
        }
    }



    //    private fun setTextColor(lipstick: TextColorOptions) {
//        chipTextColor = lipstick
//    }

    //    companion object {
//        private const val TAG = "MaterialChipView"
//    }
//    var chipIcon: Drawable? = null
//        set(value) {
//            field = value
//            buildView()
//        }
//    var chipIconBitmap: Bitmap? = null
//        set(value) {
//            field = value
//            buildView()
//        }
//    var closable = false
//        set(value) {
//            field = value
//            if (value) {
//                selectable = false
//                chipSelectableWithoutIcon = false
//            }
//            buildView()
//        }
//    var selectable = false
//        set(value) {
//            field = value
//            if (value) closable = false
//            buildView()
//        }
//    var chipSelected = false
//        set(value) {
//            if (closable || selectable || chipSelectableWithoutIcon) {
//                field = value
//                buildView()
//            }
//        }
//    var chipBackgroundColor = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//    var chipSelectedBackgroundColor = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//    var selectedStrokeColor = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//
//    var selectedStrokeSize = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//    var chipTextColor = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//    var chipSelectedTextColor = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//
//    var cornerRadius = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//    var strokeSize = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//    var strokeColor = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//    var chipHorizontalPadding = 0
//        set(value) {
//            field = value
//            buildView()
//        }
//
//    var chipSelectableWithoutIcon = false
//        set(value) {
//            field = value
//            if (value) closable = false
//            buildView()
//        }
//
//    var text: CharSequence = ""
//        @JvmName("_getText") get
//        @JvmName("_setText")
//        set(value) {
//            field = value.trim()
//        }
//
////    private var iconText: String? = null
////    private var iconTextColor = 0
////    private var iconTextBackgroundColor = 0
//
//    var onCloseClickListener: OnCloseClickListener? = null
//    var onSelectClickListener: OnSelectClickListener? = null
//
//    constructor(context: Context) : this(context, null, 0)
//    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
//        attrs?.let { initTypedArray(it) } ?: initDefaultValues()
//        buildView()
//        setSingleLine()
//        ellipsize = TextUtils.TruncateAt.END
//    }
//    private fun initTypedArray(attrs: AttributeSet) {
//        if (context == null) return
//
//        val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.Chip, 0, 0)
//
//        chipIcon = ta.getDrawable(R.styleable.ChipComponent_chip_chipIcon)
//        closable = ta.getBoolean(R.styleable.ChipComponent_chip_closable, false)
//        selectable = ta.getBoolean(R.styleable.ChipComponent_chip_selectable, false)
//        chipBackgroundColor = ta.getColor(R.styleable.ChipComponent_chip_backgroundColor, ContextCompat.getColor(context, R.color.colorChipBackground))
//        chipSelectedBackgroundColor =
//            ta.getColor(R.styleable.ChipComponent_chip_selectedBackgroundColor, ContextCompat.getColor(context, R.color.colorChipBackgroundClicked))
//        chipTextColor = ta.getColor(R.styleable.ChipComponent_chip_textColor, ContextCompat.getColor(context, R.color.colorChipText))
//        chipSelectedTextColor =
//            ta.getColor(R.styleable.ChipComponent_chip_selectedTextColor, ContextCompat.getColor(context, R.color.colorChipTextClicked))
//       // chipCloseColor = ta.getColor(R.styleable.ChipComponent_chip_closeColor, ContextCompat.getColor(context, R.color.colorChipCloseInactive))
//        //chipSelectedCloseColor =
//            ta.getColor(R.styleable.ChipComponent_chip_selectedCloseColor, ContextCompat.getColor(context, R.color.colorChipCloseClicked))
//        cornerRadius = ta.getDimensionPixelSize(R.styleable.ChipComponent_chip_cornerRadius, resources.getDimensionPixelSize(R.dimen.chip_height) / 2)
//        strokeSize = ta.getDimensionPixelSize(R.styleable.ChipComponent_chip_strokeSize, 0)
//        strokeColor = ta.getColor(R.styleable.ChipComponent_chip_strokeColor, ContextCompat.getColor(context, R.color.colorChipCloseClicked))
//        selectedStrokeSize = ta.getDimensionPixelSize(R.styleable.ChipComponent_chip_selectedStrokeSize, 0)
//        selectedStrokeColor =
//            ta.getColor(R.styleable.ChipComponent_chip_selectedStrokeColor, ContextCompat.getColor(context, R.color.colorChipCloseInactive))
//        val iconText = ta.getString(R.styleable.ChipComponent_chip_iconText)
//        val iconTextColor = ta.getColor(R.styleable.ChipComponent_chip_iconTextColor, ContextCompat.getColor(context, R.color.colorChipCloseClicked))
//        val iconTextBackgroundColor =
//            ta.getColor(R.styleable.ChipComponent_chip_iconTextBackgroundColor, ContextCompat.getColor(context, R.color.colorChipBackgroundClicked))
//        //setIconText(iconText ?: "", iconTextColor, iconTextBackgroundColor)
//
//        ta.recycle()
//
//        if (closable && selectable || chipSelectableWithoutIcon) {
//            throw IllegalStateException("Chip must be either selectable or closable. You set both true")
//        }
//    }
//
//    private fun initDefaultValues() {
//        chipBackgroundColor = ContextCompat.getColor(context, R.color.colorChipBackground)
//        chipSelectedBackgroundColor = ContextCompat.getColor(context, R.color.colorChipBackgroundClicked)
//        chipTextColor = ContextCompat.getColor(context, R.color.colorChipText)
//        chipSelectedTextColor = ContextCompat.getColor(context, R.color.colorChipTextClicked)
//       // chipCloseColor = ContextCompat.getColor(context, R.color.colorChipCloseInactive)
//       // chipSelectedCloseColor = ContextCompat.getColor(context, R.color.colorChipCloseClicked)
//        cornerRadius = resources.getDimensionPixelSize(R.dimen.chip_height) / 2
//        val iconTextColor = ContextCompat.getColor(context, R.color.colorChipCloseClicked)
//        val iconTextBackgroundColor = ContextCompat.getColor(context, R.color.colorChipBackgroundClicked)
//        //setIconText(iconText ?: "", iconTextColor, iconTextBackgroundColor)
//
//        if (closable && selectable || chipSelectableWithoutIcon) {
//            throw IllegalStateException("Chip must be either selectable or closable. You set both true")
//        }
//    }
//
//    private fun buildView() {
//        createPaddings()
//        createChipText()
//        createBackground()
//        //createChipIcons()
//    }
//
//    private fun createPaddings() {
//        gravity = Gravity.CENTER
//        val startPadding = if (chipIcon == null ) {
//            if (chipHorizontalPadding == 0) resources.getDimensionPixelSize(R.dimen.chip_icon_horizontal_margin) else chipHorizontalPadding
//        } else {
//            0
//        }
//        val endPadding = when {
//            selectable || closable -> resources.getDimensionPixelSize(R.dimen.chip_close_horizontal_margin)
//            chipSelectableWithoutIcon -> if (chipHorizontalPadding == 0) resources.getDimensionPixelSize(R.dimen.chip_icon_horizontal_margin) else chipHorizontalPadding
//            else -> if (chipHorizontalPadding == 0) resources.getDimensionPixelSize(R.dimen.chip_icon_horizontal_margin) else chipHorizontalPadding
//        }
//        setPaddingRelative(startPadding, 0, endPadding, 0)
//        compoundDrawablePadding =
//            if (chipHorizontalPadding == 0) resources.getDimensionPixelSize(R.dimen.chip_icon_horizontal_margin) else chipHorizontalPadding
//    }

//    private fun createChipText() {
//        setTextColor(if (chipSelected) chipSelectedTextColor else chipTextColor)
//    }

//
//    private fun createBackground() {
//        val radius = chipCornerRadius.toFloat()
//        val radii = floatArrayOf(radius, radius, radius, radius, radius, radius, radius, radius)
//        var _strokeColor = chipStrokeColor
//        var _strokeSize = chipStrokeSize
//        var _chipBackgroundColor = chipBackgroundColor
//
//        if (chipSelected) {
//            _strokeColor = selectedStrokeColor
//            _strokeSize = selectedStrokeSize
//            _chipBackgroundColor = chipSelectedBackgroundColor
//        }
//
//        background = GradientDrawable().apply {
//            shape = GradientDrawable.RECTANGLE
//            cornerRadii = radii
//            setColor(_chipBackgroundColor)
//            setStroke(_strokeSize, _strokeColor)
//        }
//    }
//
//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        setMeasuredDimension(measuredWidth, resources.getDimensionPixelSize(R.dimen.chip_height))
//    }
//
//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        if (event == null) return super.onTouchEvent(event)
//
//        var bounds: Rect
//
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                var positionX = event.x.toInt()
//                var positionY = event.y.toInt()
//
//                val right = compoundDrawables[2]
//
//                right?.let { drawableRight ->
//                    bounds = drawableRight.bounds
//
//                    val extraClickingArea = (13 * resources.displayMetrics.density + 0.5).toInt()
//
//                    positionX += extraClickingArea
//                    positionY -= extraClickingArea
//
//                    positionX = width - positionX
//                    if (positionX <= 0) positionX += extraClickingArea
//                    if (positionY <= 0) positionY = event.y.toInt()
//
////                    if (bounds.contains(positionX, positionY)) {
////                        if (closable) {
////                            chipSelected = true
////                            createBackground()
////                            createChipText()
////                            onCloseClickListener?.onCloseClick(this)
////                        }
////                        event.action = MotionEvent.ACTION_CANCEL
////                        return true
////                    }
//                }
////                if (chipSelectableWithoutIcon)
////                    return true
//            }
//            MotionEvent.ACTION_UP -> {
//                var positionX = event.x.toInt()
//                var positionY = event.y.toInt()
//
//                val left = compoundDrawables[0]
//                val right = compoundDrawables[2]
//
//                left?.let { drawableLeft ->
//                    bounds = drawableLeft.bounds
//
//                    val extraClickArea = (13 * resources.displayMetrics.density + 0.5).toInt()
//
//                    if (!bounds.contains(positionX, positionY)) {
//                        positionX -= extraClickArea
//                        positionY -= extraClickArea
//
//                        if (positionX <= 0) positionX = event.x.toInt()
//                        if (positionY <= 0) positionY = event.y.toInt()
//
//                        if (positionX < positionY) positionY = positionX
//                    } else {
////                        onIconClickListener?.onIconClick(this)
//                        event.action = MotionEvent.ACTION_CANCEL
//                        return false
//                    }
//                }
//
//                right?.let { drawableRight ->
//                    bounds = drawableRight.bounds
//
//                    val extraClickingArea = (13 * resources.displayMetrics.density + 0.5).toInt()
//
//                    positionX += extraClickingArea
//                    positionY -= extraClickingArea
//
//                    positionX = width - positionX
//                    if (positionX <= 0) positionX += extraClickingArea
//                    if (positionY <= 0) positionY = event.y.toInt()
//
//                    if (bounds.contains(positionX, positionY)) {
//                        if (chipSelectable) {
//                            chipSelected = !chipSelected
//                            chipSelectListener?.onChipSelect(this, chipSelected)
//                        }
////                        else if (closable) {
////                            chipSelected = false
////                            onCloseClickListener?.onCloseClick(this)
////                        }
//                        buildView()
//                        event.action = MotionEvent.ACTION_CANCEL
//                        return false
//                    }
//                }
////                if (closable) {
////                    chipSelected = false
////                    buildView()
////                }
////                if (chipSelectableWithoutIcon) {
////                    chipSelected = !chipSelected
////                    onSelectClickListener?.onSelectClick(this, chipSelected)
////                    buildView()
////                }
//            }
//        }
//
//        return super.onTouchEvent(event)
//    }
//
//    override fun setText(text: CharSequence?, type: BufferType?) {
//        super.setText(text?.trim(), type)
//    }

}


