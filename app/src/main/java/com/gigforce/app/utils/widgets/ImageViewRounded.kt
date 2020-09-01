package com.gigforce.app.utils.widgets

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.gigforce.app.R

class ImageViewRounded : AppCompatImageView {
    private var radius = 8f

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    fun init(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.ImageViewRounded, defStyle, 0)
        radius = a.getDimension(R.styleable.ImageViewRounded_radius, 8f)
        a.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable as BitmapDrawable ?: return
        if (width == 0 || height == 0) {
            return
        }
        val fullSizeBitmap = drawable.bitmap
        val scaledWidth = measuredWidth
        val scaledHeight = measuredHeight
        val mScaledBitmap: Bitmap
        mScaledBitmap =
            if (scaledWidth == fullSizeBitmap.width && scaledHeight == fullSizeBitmap.height) {
                fullSizeBitmap
            } else {
                Bitmap.createScaledBitmap(
                    fullSizeBitmap,
                    scaledWidth,
                    scaledHeight,
                    true /* filter */
                )
            }
        val roundBitmap = getRoundedCornerBitmap(
            context, mScaledBitmap, radius.toInt(), scaledWidth, scaledHeight,
            false, false, false, false
        )
        canvas.drawBitmap(roundBitmap, 0f, 0f, null)
    }

    companion object {
        fun getRoundedCornerBitmap(
            context: Context,
            input: Bitmap?,
            pixels: Int,
            w: Int,
            h: Int,
            squareTL: Boolean,
            squareTR: Boolean,
            squareBL: Boolean,
            squareBR: Boolean
        ): Bitmap {
            val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val densityMultiplier = context.resources.displayMetrics.density
            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, w, h)
            val rectF = RectF(rect)

            //make sure that our rounded corner is scaled appropriately
            val roundPx = pixels * densityMultiplier
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint)


            //draw rectangles over the corners we want to be square
            if (squareTL) {
                canvas.drawRect(0f, h / 2.toFloat(), w / 2.toFloat(), h.toFloat(), paint)
            }
            if (squareTR) {
                canvas.drawRect(w / 2.toFloat(), h / 2.toFloat(), w.toFloat(), h.toFloat(), paint)
            }
            if (squareBL) {
                canvas.drawRect(0f, 0f, w / 2.toFloat(), h / 2.toFloat(), paint)
            }
            if (squareBR) {
                canvas.drawRect(w / 2.toFloat(), 0f, w.toFloat(), h / 2.toFloat(), paint)
            }
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(input!!, 0f, 0f, paint)
            return output
        }
    }
}