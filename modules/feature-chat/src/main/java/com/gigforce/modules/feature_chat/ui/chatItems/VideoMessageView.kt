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
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toDisplayText
import com.gigforce.core.extensions.visible
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.models.ChatMessage
import java.util.concurrent.TimeUnit

abstract class VideoMessageView(
        val type: String,
        context: Context,
        attrs: AttributeSet?
) : MediaMessage(
        context,
        attrs
) {

    //View
    private lateinit var linearLayout: LinearLayout
    private lateinit var imageView: ImageView
    private lateinit var textViewTime: TextView
    private lateinit var cardView: CardView
    private lateinit var attachmentNameTV: TextView
    private lateinit var playDownloadIconIV: ImageView
    private lateinit var playDownloadOverlayIV: ImageView
    private lateinit var attachmentUploadingDownloadingProgressBar: ProgressBar
    private lateinit var videoLength: TextView

    init {
        setDefault()
        inflate()
    }

    fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    fun inflate() {
        if (type == "in")
            LayoutInflater.from(context).inflate(R.layout.item_chat_text_with_video_in, this, true)
        else
            LayoutInflater.from(context).inflate(R.layout.item_chat_text_with_video_out, this, true)
        loadViews()
    }

    fun loadViews() {
        linearLayout = this.findViewById(R.id.ll_msgContainer)
        imageView = this.findViewById(R.id.iv_image)
        textViewTime = this.findViewById(R.id.tv_msgTimeValue)
        cardView = this.findViewById(R.id.cv_msgContainer)
        attachmentNameTV = this.findViewById(R.id.tv_file_name)
        playDownloadIconIV = this.findViewById(R.id.play_download_icon_iv)
        playDownloadOverlayIV = this.findViewById(R.id.play_download_overlay_iv)
        attachmentUploadingDownloadingProgressBar = this.findViewById(R.id.attachment_downloading_pb)
        videoLength = this.findViewById(R.id.video_length_tv)
    }

    override fun onBind(msg: ChatMessage) {

        attachmentNameTV.text = msg.attachmentName
        videoLength.text = convertMicroSecondsToNormalFormat(msg.videoLength)

        if (msg.attachmentPath.isNullOrBlank()) {
            imageView.setImageDrawable(null)
            imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                            context.resources,
                            android.R.color.white,
                            null
                    )
            )
            playDownloadOverlayIV.visible()
            playDownloadIconIV.gone()
            attachmentUploadingDownloadingProgressBar.visible()

            msg.thumbnailBitmap?.let {
                Glide.with(context)
                        .load(it)
                        .into(imageView)
            }
        } else {
            attachmentUploadingDownloadingProgressBar.gone()

            val downloadedFile = returnFileIfAlreadyDownloadedElseNull()
            val fileHasBeenDownloaded = downloadedFile != null

            if (fileHasBeenDownloaded) {
                attachmentUploadingDownloadingProgressBar.gone()

                playDownloadOverlayIV.visible()
                playDownloadIconIV.visible()
                Glide.with(context).load(R.drawable.ic_play_2).into(playDownloadIconIV)

//                Glide.with(context).load(downloadedFile).placeholder(getCircularProgressDrawable())
//                        .into(imageView)
            } else {
                Glide.with(context)
                        .load(msg.thumbnail)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView)

                if (msg.attachmentCurrentlyBeingDownloaded) {

                    playDownloadOverlayIV.visible()
                    playDownloadIconIV.gone()
                    attachmentUploadingDownloadingProgressBar.visible()
                } else {

                    attachmentUploadingDownloadingProgressBar.gone()
                    playDownloadOverlayIV.visible()
                    playDownloadIconIV.visible()
                    Glide.with(context).load(R.drawable.ic_download_24).into(playDownloadIconIV)
                }
            }
        }

        if (msg.thumbnail.isNullOrBlank()) {
            imageView.setImageDrawable(null)
            imageView.setBackgroundColor(
                    ResourcesCompat.getColor(
                            context.resources,
                            android.R.color.white,
                            null
                    )
            )
        } else {
            Glide.with(context).load(msg.thumbnail).into(imageView)
        }

        when (msg.flowType) {
            "in" -> {
                textViewTime.text = msg.timestamp?.toDisplayText()
                textViewTime.setTextColor(Color.parseColor("#979797"))
                linearLayout.setBackgroundColor(Color.parseColor("#19eeeeee"))
                textViewTime.setTextColor(Color.parseColor("#000000"))
                attachmentNameTV.setTextColor(Color.parseColor("#E91E63"))

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
                attachmentNameTV.setTextColor(Color.parseColor("#ffffff"))

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

    private fun convertMicroSecondsToNormalFormat(videoAttachmentLength: Long): String {
        if (videoAttachmentLength == 0L)
            return ""

        if (videoAttachmentLength > 3600000) {
            return String.format(
                    "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(videoAttachmentLength),
                    TimeUnit.MILLISECONDS.toMinutes(videoAttachmentLength) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(videoAttachmentLength)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(videoAttachmentLength) -
                            TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                            videoAttachmentLength
                                    )
                            )
            )
        } else {
            return String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(videoAttachmentLength) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(videoAttachmentLength)
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(videoAttachmentLength) -
                            TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                            videoAttachmentLength
                                    )
                            )
            )
        }
    }

}

class InVideoMessageView(context: Context, attrs: AttributeSet?) : VideoMessageView("in", context, attrs)
class OutVideoMessageView(context: Context, attrs: AttributeSet?) : VideoMessageView("out", context, attrs)