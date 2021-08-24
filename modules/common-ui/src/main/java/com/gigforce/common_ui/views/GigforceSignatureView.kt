package com.gigforce.common_ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import com.gigforce.common_ui.R

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

                val i = getSignatureFullImage()
                Log.d("TAG", "d")


                val i2 = getSignatureImageCroppedToSignature()
                Log.d("TAG", "d")
                // drawerView.undoDrawnStuff()
            }
        }
    }

    fun setColorResource(
        @ColorRes color: Int
    ) {
        drawerView.setColor(color)
    }

    fun hasUserDrawnSignature(): Boolean {
        return drawerView.hasUserDrawnAnything()
    }

    fun getSignatureFullImage(): Bitmap {
        val height = drawerView.height
        val width = drawerView.width

        Log.d(DrawerView.TAG,"orignal img : $height, $width")

        val b = Bitmap.createBitmap(
            drawerView.width,
            drawerView.height,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
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
        val padding = 30

        val width = (drawerView.userDrawnRight - drawerView.userDrawnLeft) + padding * 2
        val height = (drawerView.userDrawnTop - drawerView.userDrawnBottom) + padding * 2

        val minX = drawerView.userDrawnLeft
        val minY = drawerView.userDrawnBottom

        Log.d(DrawerView.TAG,"sign img : $height, $width")

        val b = Bitmap.createBitmap(
            width.toInt(),
            height.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        drawerView.layout(
            minX.toInt() +drawerView.userDrawnLeft.toInt(),
            minY.toInt() +drawerView.userDrawnTop.toInt(),
            minX.toInt() + drawerView.userDrawnRight.toInt(),
            minY.toInt() + drawerView.userDrawnBottom.toInt()
        )

        drawerView.draw(c)
        return b
    }


//    fun getImageCroppedToSignatureOnly() : Bitmap{
//
//    }


}