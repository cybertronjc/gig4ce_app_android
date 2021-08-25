package com.gigforce.common_ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import com.gigforce.common_ui.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class GigforceSignatureView(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(
    context,
    attrs
) {

    private val drawerView: DrawerView
    private val undoLayout: View

    init {
        LayoutInflater.from(
            context
        ).inflate(
            R.layout.layout_signature_view,
            this,
            true
        ).apply {
            drawerView = this.findViewById(R.id.drawer_view)
            undoLayout = this.findViewById(R.id.undo_layout)

            undoLayout.setOnClickListener {

//                val i = getSignatureFullImage()
//                Log.d("TAG", "d")


                val i2 = getSignatureImageCroppedToSignature()
                Log.d("TAG", "d")
                // drawerView.undoDrawnStuff()
            }
        }
    }

    fun setColorResource(
        @ColorRes color: Int
    ) = drawerView.setColor(
        ResourcesCompat.getColor(
            context.resources,
            color,
            null
        )
    )

    fun hasUserDrawnSignature(): Boolean {
        return drawerView.hasUserDrawnAnything()
    }

    fun getSignatureFullImage(): Bitmap {

        if (!hasUserDrawnSignature()) {
            throw IllegalStateException("user has not drawn any thing on signature view yet")
        }

        val height = drawerView.height
        val width = drawerView.width

        Log.d(DrawerView.TAG, "orignal img : $height, $width")

        val b = Bitmap.createBitmap(
            drawerView.width,
            drawerView.height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        c.drawColor(Color.WHITE)

        drawerView.layout(
            drawerView.left,
            drawerView.top,
            drawerView.right,
            drawerView.bottom
        )

        drawerView.draw(c)
        return b
    }

    fun getSignatureImageCroppedToSignature(): Bitmap {
        val padding = 20

        val srcImage = getSignatureFullImage()

        val width = (drawerView.userDrawnRight - drawerView.userDrawnLeft) + padding * 2
        val height = (drawerView.userDrawnTop - drawerView.userDrawnBottom) + padding * 2

        val minX = drawerView.userDrawnLeft
        val minY = drawerView.userDrawnBottom

        Log.d(DrawerView.TAG, "dest imag : $height, $width")
        Log.d(DrawerView.TAG, "minX,y : $minX, $minY")

        var startX = if (minX < 0) {
            0
        } else if (minX <= padding)
            minX.toInt()
        else
            minX.toInt() - padding

        var startY = if (minY < 0) {
            0
        } else if (minY <= padding)
            minY.toInt()
        else
            minY.toInt() - padding

        var finalWidth  = 0
        if(startX + width.toInt() >= srcImage.width){
            startX = 0
            finalWidth = srcImage.width
        } else {
            finalWidth = width.toInt()
        }

        var finalHeight = 0
         if(startY + height.toInt() >= srcImage.height){
             startY = 0
             finalHeight =  srcImage.height
        } else {
             finalHeight = height.toInt()
        }

        val v = Bitmap.createBitmap(
            srcImage,
            startX,
            startY,
            finalWidth,
            finalHeight
        )

        if (!srcImage.isRecycled)
            srcImage.recycle()

        return v
    }

    fun saveSignatureFullImageTo(
        destFile: File
    ) {
        val bitmap = getSignatureFullImage()

        try {
            FileOutputStream(destFile).use { out ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    out
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (!bitmap.isRecycled)
                bitmap.recycle()
        }
    }

    fun saveBitmapCroppedToSignatureTo(
        destFile: File
    ) {
        val bitmapCroppedToSignature = getSignatureImageCroppedToSignature()

        try {
            FileOutputStream(destFile).use { out ->
                bitmapCroppedToSignature.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    out
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (!bitmapCroppedToSignature.isRecycled)
                bitmapCroppedToSignature.recycle()
        }
    }
}