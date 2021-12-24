package com.gigforce.modules.feature_chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import javax.inject.Inject

class ChatAudioRecyclerItemView (
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var eventTracker: IEventTracker


    override fun bind(data: Any?) {

    }

    override fun onClick(v: View?) {

    }

}