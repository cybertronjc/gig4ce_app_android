package com.gigforce.common_ui.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.common_ui.R
import com.gigforce.core.IViewHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.cell_app_bar.view.*
import javax.inject.Inject

@AndroidEntryPoint
class AppBarComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder {
    @Inject lateinit var userinfo: UserInfoImp
    var setProfileName: String
        get() = profile_name.text as String
        set(value) {
            profile_name.text = value
        }

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_app_bar, this, true)
        setProfilePic(userinfo.getData().profilePicPath)
        setProfileName = userinfo.getData().profileName
    }


    fun setProfilePic(image: String) {
        appProfileComponent.setProfilePic(image)
    }



    override fun bind(data: Any?) {

    }
}