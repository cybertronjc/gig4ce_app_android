package com.gigforce.modules.feature_chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.ChatMediaRecyclerItemViewLayoutBinding
import com.gigforce.modules.feature_chat.models.ChatMediaDocsRecyclerItemData
import com.gigforce.modules.feature_chat.models.ChatMediaViewModels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatMediaRecyclerItemView(
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


    private var viewData: ChatMediaViewModels.ChatMediaImageItemData? = null

    //views
    private lateinit var thumbnailImageView: GigforceImageView
    private lateinit var playDownloadIconIV: ImageView
    private lateinit var playDownloadOverlayIV: ImageView
    private lateinit var attachmentDownloadingProgressBar: ProgressBar
    private lateinit var videoLengthLayout: View
    private lateinit var videoLength: TextView
    private lateinit var attachmentTypeIcon: ImageView

    init {
        setDefault()
        inflate()
        findViews()
    }


    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun findViews(){
        thumbnailImageView = this.findViewById(R.id.thumbnail_imageview)
        playDownloadIconIV = this.findViewById(R.id.play_download_icon_iv)
        playDownloadOverlayIV = this.findViewById(R.id.play_download_overlay_iv)
        attachmentDownloadingProgressBar = this.findViewById(R.id.attachment_downloading_pb)
        videoLengthLayout = this.findViewById(R.id.video_length_layout)
        videoLength = this.findViewById(R.id.video_length_tv)
        attachmentTypeIcon = this.findViewById(R.id.attachment_type_icon)
    }

    fun inflate() {
        LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_image_item_view, this, true)
    }


    override fun bind(data: Any?) {
        viewData = null

        data?.let {
            val chatMediaData =
                it as ChatMediaViewModels.ChatMediaImageItemData
            viewData = chatMediaData


        }
    }

    override fun onClick(v: View?) {
        val currentViewData = viewData ?: return
    }


}