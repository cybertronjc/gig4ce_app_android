package com.gigforce.app.modules.profile

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.utils.GlideApp
import com.google.android.material.card.MaterialCardView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.top_profile_bar.view.*


class TopProfileBar: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    lateinit var storage: FirebaseStorage
    init {
        storage = FirebaseStorage.getInstance()
        View.inflate(context, R.layout.top_profile_bar, this)

//        back_button.setOnClickListener {
//            (context as MainActivity).onBackPressed()
//        }
    }


    private fun displayImage() {
        if (imageName != "avatar.jpg" && imageName != "") {
            val profilePicRef: StorageReference =
                storage.reference.child("profile_pics").child(imageName!!)
            GlideApp.with(this.context!!)
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }else{
            GlideApp.with(this.context!!)
                .load(R.drawable.avatar)
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