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
import com.gigforce.common_ui.shimmer.ShimmerHelper
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

    private val drawableCrossFadeFactory: DrawableCrossFadeFactory by lazy {
        DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build()
    }

    fun loadImageFromFirebase(
        firebasePath: String,
        @DrawableRes placeHolder: Int = -1,
        @DrawableRes error: Int = -1,
        centerCrop: Boolean = false
    ) {
        val pathRef = firebaseStorage.reference.child(firebasePath)

        var requestManager = Glide.with(context).load(pathRef)

        if (placeHolder != -1) {
            requestManager = requestManager.placeholder(placeHolder)
        }

        if (error != -1) {
            requestManager = requestManager.error(error)
        } else {
            requestManager = requestManager.error(getErrorImage())
        }

        if (centerCrop) {
            requestManager = requestManager.centerCrop()
        }

        requestManager.into(this)
    }


    fun loadImageIfUrlElseTryFirebaseStorage(
        urlOrFirebasePath: String,
        @DrawableRes placeHolder: Int = -1,
        @DrawableRes error: Int = -1,
        centerCrop: Boolean = false
    ) {

        val isUrl = Patterns.WEB_URL.matcher(urlOrFirebasePath).matches()
        if (isUrl) {
            loadImage(
                Uri.parse(urlOrFirebasePath),
                placeHolder,
                error,
                centerCrop
            )
        } else {
            loadImageFromFirebase(
                urlOrFirebasePath,
                placeHolder,
                error,
                centerCrop
            )
        }
    }

    fun loadImage(
        image: Uri,
        @DrawableRes placeHolder: Int = -1,
        @DrawableRes error: Int = -1,
        centerCrop: Boolean = false
    ) {

        var requestManager = Glide.with(context)
            .load(image)

        if (placeHolder != -1) {
            requestManager = requestManager.placeholder(placeHolder)
        } else {
            requestManager = requestManager.placeholder(ShimmerHelper.getShimmerDrawable())
        }

        if (error != -1) {
            requestManager = requestManager.error(error)
        } else {
            requestManager = requestManager.error(getErrorImage())
        }

        if (centerCrop) {
            requestManager = requestManager.centerCrop()
        }

        requestManager.into(this)
    }

    fun loadImage(
        @DrawableRes image: Int,
        centerCrop: Boolean = false
    ) {

        var glideRequestManager = Glide.with(context)
            .load(image)
            .error(getErrorImage())

        if (centerCrop) {
            glideRequestManager = glideRequestManager.centerCrop()
        }

        glideRequestManager.into(this)
    }

    fun loadImage(
        image: Bitmap,
        centerCrop: Boolean
    ) {

        var requestManager = Glide.with(context)
            .load(image)
            .error(getErrorImage())

        if (centerCrop) {
            requestManager = requestManager.centerCrop()
        }

        requestManager.into(this)
    }

    fun loadProfilePicture(
        profilePictureThumbnail: String?,
        profilePicture: String?
    ) {
        if (profilePicture.isNullOrEmpty() && profilePictureThumbnail.isNullOrEmpty()) {
            loadDefaultUserAvatar()
        } else {

            if (!profilePictureThumbnail.isNullOrBlank()) {

                val isUrl = Patterns.WEB_URL.matcher(profilePictureThumbnail).matches()
                if (isUrl) {

                    loadImage(
                        Uri.parse(profilePictureThumbnail),
                        getDefaultUserAvatar(),
                        getDefaultUserAvatar()
                    )
                } else {

                    val fullThumbnailPath = createFullProfilePicturePath(profilePictureThumbnail)
                    loadImageFromFirebase(
                        fullThumbnailPath,
                        getDefaultUserAvatar(),
                        getDefaultUserAvatar()
                    )
                }
            } else {

                val isUrl = Patterns.WEB_URL.matcher(profilePicture).matches()
                if (isUrl) {

                    loadImage(
                        Uri.parse(profilePicture),
                        getDefaultUserAvatar(),
                        getDefaultUserAvatar()
                    )
                } else {

                    val fullPath = createFullProfilePicturePath(
                        profilePicture!!
                    )
                    loadImageFromFirebase(
                        fullPath,
                        getDefaultUserAvatar(),
                        getDefaultUserAvatar()
                    )
                }
            }
        }
    }

    private fun createFullProfilePicturePath(
        profilePicturePath: String
    ): String {

        return if (profilePicturePath.startsWith("profile_pics"))
            profilePicturePath
        else
            "profile_pics/$profilePicturePath"

    }

    private fun loadDefaultUserAvatar() {
        loadImage(getDefaultUserAvatar())
    }

    @DrawableRes
    private fun getDefaultUserAvatar(): Int = R.drawable.ic_avatar_male

    fun clearImage() {
        Glide.with(context).clear(this)
    }
}