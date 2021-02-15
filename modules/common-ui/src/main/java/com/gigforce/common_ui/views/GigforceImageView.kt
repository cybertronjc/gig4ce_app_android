package com.gigforce.common_ui.views

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.util.Patterns
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.gigforce.common_ui.R
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.FirebaseStorage

class GigforceImageView(
    context: Context,
    attrs: AttributeSet
) : ShapeableImageView(
    context,
    attrs
) {
    @DrawableRes
    private var _errorImage: Int? = null
    private fun getErrorImage(): Int {
        return _errorImage ?: R.drawable.ic_error_loading_image
    }

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val drawableCrossFadeFactory : DrawableCrossFadeFactory by lazy {
        DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    }

    fun loadImageFromFirebase(
        firebasePath: String
    ) {
        val pathRef = firebaseStorage.reference.child(firebasePath)

        Glide.with(context)
            .load(pathRef)
            .error(getErrorImage())
            .into(this)
    }

    fun loadImageIfUrlElseTryFirebaseStorage(
        urlOrFirebasePath: String
    ) {

        val isUrl = Patterns.WEB_URL.matcher(urlOrFirebasePath).matches()
        if (isUrl) {
            loadImage(Uri.parse(urlOrFirebasePath))
        } else {
            loadImageFromFirebase(urlOrFirebasePath)
        }
    }

    fun loadImage(
        image: Uri
    ) {

        Glide.with(context)
            .load(image)
            .error(getErrorImage())
            .into(this)
    }

    fun loadImage(
        @DrawableRes image: Int
    ) {

        Glide.with(context)
            .load(image)
            .error(getErrorImage())
            .into(this)
    }

    fun loadImage(
         image : Bitmap
    ) {

        Glide.with(context)
            .load(image)
            .error(getErrorImage())
            .into(this)
    }
}