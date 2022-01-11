package com.gigforce.modules.feature_chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatAudioViewModels
import com.gigforce.modules.feature_chat.models.ChatDocsViewModels
import com.gigforce.modules.feature_chat.models.ChatMediaViewModels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatDateRecyclerItemView (
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

    //views
    private lateinit var dateText: TextView

    init {
        setDefault()
        inflate()
        findViews()
    }

    private fun findViews() {
        dateText = this.findViewById(R.id.date_text)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_date_item_view, this, true)
    }

    override fun bind(data: Any?) {
        data?.let {
            if (it is ChatMediaViewModels.ChatMediaDateItemData){
                val mediaData = it as ChatMediaViewModels.ChatMediaDateItemData
                dateText.text = mediaData.dateString ?: ""
            } else if (it is ChatDocsViewModels.ChatMediaDateItemData){
                val mediaData = it as ChatDocsViewModels.ChatMediaDateItemData
                dateText.text = mediaData.dateString ?: ""
            } else if (it is ChatAudioViewModels.ChatMediaDateItemData){
                val mediaData = it as ChatAudioViewModels.ChatMediaDateItemData
                dateText.text = mediaData.dateString ?: ""
            }

        }
    }

    override fun onClick(v: View?) {

    }

}