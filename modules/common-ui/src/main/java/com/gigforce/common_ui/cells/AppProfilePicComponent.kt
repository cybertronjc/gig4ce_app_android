package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.AppProfilePicDVM
import com.gigforce.core.IViewHolder

class AppProfilePicComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    private val profileImg : ImageView
    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_app_profile, this, true)
        profileImg = this.findViewById(R.id.profile_image)
    }

    fun setProfilePic(image: String){
        setImageToProfilePic(image)
    }

    override fun bind(data: Any?) {
        if(data is AppProfilePicDVM){
            setImageToProfilePic(data.image)
        }
    }

    private fun setImageToProfilePic(image: String) {
        Glide.with(context)
            .load(image)
            .into(profileImg)
    }

}