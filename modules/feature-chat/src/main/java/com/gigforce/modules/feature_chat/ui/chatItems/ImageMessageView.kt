package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.ChatMessage
import java.io.File


abstract class ImageMessageView(
        private val type : String,
        context: Context,
        attrs: AttributeSet?
) : MediaMessage(context, attrs),
View.OnClickListener{

    private lateinit var imageView: ImageView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var downloadIconIV: ImageView
    private lateinit var downloadOverlayIV: ImageView
    private lateinit var attachmentDownloadingProgressBar: ProgressBar

    init {
        setDefault()
        inflate()
        findViews()
        setOnClickListeners()
    }

    private fun findViews() {
        imageView = this.findViewById(R.id.iv_image)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        downloadIconIV = this.findViewById(R.id.download_icon_iv)
        downloadOverlayIV = this.findViewById(R.id.download_overlay_iv)
        attachmentDownloadingProgressBar = this.findViewById(R.id.attachment_downloading_pb)
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        val resId = if (type == ChatConstants.FLOW_TYPE_IN) R.layout.item_chat_text_with_image_in else R.layout.item_chat_text_with_image_out
        LayoutInflater.from(context).inflate(resId, this, true)
    }

    private fun setOnClickListeners() {
        cardView.setOnClickListener(this)
    }

    private fun handleImageNotDownloaded(msg: ChatMessage){
        attachmentDownloadingProgressBar.gone()
        downloadOverlayIV.visible()
        downloadIconIV.visible()

        loadThumbnail(msg)
    }

    private fun loadThumbnail(msg:ChatMessage){
        Glide.with(context)
                .load(msg.thumbnail)
                .placeholder(getCircularProgressDrawable())
                .into(imageView)
    }

    private fun handleDownloadInProgress(msg:ChatMessage){
        downloadOverlayIV.visible()
        downloadIconIV.gone()
        attachmentDownloadingProgressBar.visible()

        this.loadThumbnail(msg)
    }


    private fun handleImage(msg:ChatMessage){
        imageView.setImageDrawable(null)

        val downloadedFile = returnFileIfAlreadyDownloadedElseNull()

        if(downloadedFile != null){
            handleImageDownloaded(downloadedFile)
        }else if(msg.attachmentCurrentlyBeingDownloaded)
        {
            handleDownloadInProgress(msg)
        }else{
            handleImageNotDownloaded(msg)
        }
    }

    private fun handleImageDownloaded(downloadedFile: File) {

        attachmentDownloadingProgressBar.gone()
        downloadOverlayIV.gone()
        downloadIconIV.gone()

        Glide.with(context)
                .load(downloadedFile)
                .placeholder(getCircularProgressDrawable())
                .into(imageView)
    }

    override fun onBind(msg: ChatMessage) {
        textViewTime.text = msg.timestamp?.toDisplayText()
        handleImage(msg)
    }

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    override fun onClick(v: View?) {
        //todo
    }
}

class InImageMessageView(context: Context, attrs: AttributeSet?) : ImageMessageView("in", context, attrs)
class OutImageMessageView(context: Context, attrs: AttributeSet?) : ImageMessageView("out", context, attrs)