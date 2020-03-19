package com.gigforce.app.modules.profile

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.cardview.widget.CardView
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.top_profile_bar.view.*

class TopProfileBar: CardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    lateinit var storage: FirebaseStorage
    init {
        storage = FirebaseStorage.getInstance()
        View.inflate(context, R.layout.top_profile_bar, this)
    }

    private fun displayImage() {
        if (imageName != null) {
            val profilePicRef: StorageReference =
                storage.reference.child("profile_pics").child(imageName!!)
            GlideApp.with(this.context!!)
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }

    // Getters and setter
    var imageName: String = ""
        set(value) {
            field = value
            displayImage()
        }

    var userName: String = ""
        set(value) {
            field = value
            user_name.text = userName
        }
}