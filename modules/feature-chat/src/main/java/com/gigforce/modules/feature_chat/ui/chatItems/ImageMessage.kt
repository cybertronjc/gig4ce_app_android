package com.gigforce.modules.feature_chat.ui.chatItems

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.models.Message


abstract class ImageMessage(
        val type: String,
        context: Context,
        attrs: AttributeSet?
) : MediaMessage(context, attrs){

    private lateinit var linearLayout: LinearLayout
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
    }

    private fun findViews() {
        linearLayout = this.findViewById(R.id.ll_msgContainer)
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
        if (type == "in")
            LayoutInflater.from(context).inflate(R.layout.msg_chat_in_text, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.msg_chat_out_text, this, true)
    }

    override fun bind(data: Any?) {
        val msg = data as Message? ?: return

        if (msg.attachmentPath.isNullOrBlank()) {
            imageView.setImageDrawable(null)
            imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                            context.resources,
                            android.R.color.white,
                            null
                    )
            )
            downloadIconIV.gone()
            downloadOverlayIV.visible()

            if (msg.thumbnailBitmap != null) {
                Glide.with(context)
                        .load(msg.thumbnailBitmap)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)

//                    msg.thumbnailBitmap?.let {
//
//                        if (!it.isRecycled)
//                            it.recycle()
//                    }
            }
            attachmentDownloadingProgressBar.visible()
        } else {
            attachmentDownloadingProgressBar.gone()

            val downloadedFile = returnFileIfAlreadyDownloadedElseNull(
                    ChatConstants.ATTACHMENT_TYPE_IMAGE,
                    msg.attachmentPath!!
            )
            val fileHasBeenDownloaded = downloadedFile != null

            if (fileHasBeenDownloaded) {
                attachmentDownloadingProgressBar.gone()
                downloadOverlayIV.gone()

                downloadIconIV.gone()
                Glide.with(context)
                        .load(downloadedFile)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)
            } else {

                Glide.with(context)
                        .load(msg.thumbnail)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)

                if (msg.attachmentCurrentlyBeingDownloaded) {
                    downloadOverlayIV.visible()
                    downloadIconIV.gone()
                    attachmentDownloadingProgressBar.visible()
                } else {
                    attachmentDownloadingProgressBar.gone()
                    downloadOverlayIV.visible()
                    downloadIconIV.visible()
                    Glide.with(context)
                            .load(R.drawable.ic_download_24)
                            .into(downloadIconIV)
                }
            }
        }

        when (msg.flowType) {

            "in" -> {
                textViewTime.text = msg.timestamp?.toDisplayText()
                textViewTime.setTextColor(Color.parseColor("#979797"))
                linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))

                val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(26, 5, 160, 5)
                cardView.layoutParams = layoutParams
            }
            "out" -> {
                textViewTime.text = msg.timestamp?.toDisplayText()
                linearLayout.setBackgroundColor(Color.parseColor("#E91E63"))
                textViewTime.setTextColor(Color.parseColor("#ffffff"))

                val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.gravity = Gravity.END
                layoutParams.setMargins(160, 5, 26, 5)
                cardView.layoutParams = layoutParams
            }
        }

    }

    fun getCircularProgressDrawable(): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 20f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }
}

class InImageMessage(context: Context, attrs: AttributeSet?) : ImageMessage("in", context, attrs)
class OutImageMessage(context: Context, attrs: AttributeSet?) : ImageMessage("out", context, attrs)