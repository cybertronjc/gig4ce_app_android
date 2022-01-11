package com.gigforce.modules.feature_chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatAudioViewModels
import com.gigforce.modules.feature_chat.models.ChatMediaViewModels
import com.gigforce.modules.feature_chat.ui.chatItems.MessageFlowType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
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

    //Views
    private lateinit var audioIcon : ImageView
    private lateinit var audioFileName : TextView
    private lateinit var audioFileDetails : TextView
    private lateinit var audioFileDate : TextView

    init {
        setDefault()
        inflate()
        findViews()
    }

    private fun findViews() {
        audioIcon = this.findViewById(R.id.audio_icon)
        audioFileName = this.findViewById(R.id.audio_file_name)
        audioFileDetails = this.findViewById(R.id.audio_file_details)
        audioFileDate = this.findViewById(R.id.audio_file_date)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
            LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_audio_item_view, this, true)
    }


    override fun bind(data: Any?) {

        data?.let {
            val mediaData = it as ChatAudioViewModels.ChatMediaAudioItemData
            audioFileName.text = mediaData.audioName ?: ""
            audioFileDetails.text = mediaData.audioDetail ?: ""
            audioFileDate.text = mediaData.audioDate ?: ""
        }

    }

    override fun onClick(v: View?) {

    }

}