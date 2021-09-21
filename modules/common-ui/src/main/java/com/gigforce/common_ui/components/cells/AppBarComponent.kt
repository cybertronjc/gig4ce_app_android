package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.cell_app_bar.view.*
import javax.inject.Inject

@AndroidEntryPoint
class AppBarComponent(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    IViewHolder, View.OnClickListener {
    @Inject
    lateinit var userinfo: UserInfoImp
    @Inject
    lateinit var navigation: INavigation
    var setProfileName: String
        get() = userinfo.getData().profileName
        set(value) {
            profile_name.text = value
        }

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.cell_app_bar, this, true)
        setProfileName = userinfo.getData().profileName
        chat_icon.setOnClickListener(this)
    }


    override fun bind(data: Any?) {
    }

    override fun onClick(v: View?) {
        navigation.navigateTo("chats/chatList")
    }


}