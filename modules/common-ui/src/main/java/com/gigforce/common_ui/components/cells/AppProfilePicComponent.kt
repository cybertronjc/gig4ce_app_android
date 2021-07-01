package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.common_ui.viewdatamodels.AppProfilePicDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.cell_app_profile.view.*
import javax.inject.Inject

@AndroidEntryPoint
class AppProfilePicComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder,View.OnClickListener {
    private val profileImg: ImageView
    @Inject
    lateinit var userinfo: UserInfoImp
    @Inject lateinit var navigation : INavigation
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_app_profile, this, true)
        profileImg = this.findViewById(R.id.profile_image)
        setProfilePic(userinfo.getData().profilePicPath)
        cardView.setOnClickListener(this)
    }

    fun setProfilePic(image: String) {
        setImageToProfilePic(image)
    }

    override fun bind(data: Any?) {
        if (data is AppProfilePicDVM) {
            setImageToProfilePic(data.image)
        }
    }

    private fun setImageToProfilePic(image: String) {
        if(image.isNotBlank()) {
            val profilePicRef: StorageReference =
                FirebaseStorage.getInstance().reference.child("profile_pics").child(image)
            GlideApp.with(context)
                .load(profilePicRef)
                .into(profileImg)
        }
    }

    override fun onClick(v: View?) {
        navigation.navigateTo("profile")
    }

}