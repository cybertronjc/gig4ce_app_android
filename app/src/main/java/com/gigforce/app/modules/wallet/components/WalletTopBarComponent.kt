package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.modules.preferences.PreferencesFragment.Companion.storage
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.top_profile_bar.view.*

class WalletTopBarComponent: ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.wallet_top_bar_component, this)
        imageName = "ysharma.jpg"
    }

    private fun displayImage() {
        if (imageName != "avatar.jpg" && imageName != ""){
            val profilePicRef: StorageReference =
                storage.reference.child("profile_pics").child(imageName!!)
            GlideApp.with(this.context!!)
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        } else{
            GlideApp.with(this.context!!)
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }

    var imageName: String? = null
       set(value) {
           field = value
           displayImage()
       }

}