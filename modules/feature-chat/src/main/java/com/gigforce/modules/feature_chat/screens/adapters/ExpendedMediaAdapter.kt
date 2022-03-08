package com.gigforce.modules.feature_chat.screens.adapters

import android.content.Context
import android.text.format.Formatter.formatShortFileSize
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.models.GroupMedia
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.utils.DateHelper
import com.gigforce.modules.feature_chat.R
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class ExpendedMediaAdapter(
    private val context: Context,
    private val refToGigForceAttachmentDirectory: File,
    private val requestManager: RequestManager,
    private val onGroupMediaClickListener: GroupMediaRecyclerAdapter.OnGroupMediaClickListener
):  RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    companion object {
        const val VIEW_TYPE_IMAGES = 1
        const val VIEW_TYPE_DOCS = 2
        const val VIEW_TYPE_AUDIO = 3
    }

    private var mediaList: List<GroupMedia> = emptyList()

    private var type: Int = -1

    private var imagesDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)

    private var videosDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_VIDEOS)

    private var documentsDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_DOCUMENTS)

    private var audiosDirectoryRef: File = File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_AUDIOS)

    private var itemsDownloading: MutableList<GroupMedia> = mutableListOf()

    private inner class View1ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
//        var message: TextView = itemView.findViewById(R.id.textView)
        private var thumbnailIV: GigforceImageView = itemView.findViewById(R.id.thumbnail_imageview)
        private var playDownloadIconIV: ImageView =
            itemView.findViewById(R.id.play_download_icon_iv)
        private var playDownloadOverlayIV: ImageView =
            itemView.findViewById(R.id.play_download_overlay_iv)
        private val attachmentDownloadingProgressBar: ProgressBar =
            itemView.findViewById(R.id.attachment_downloading_pb)
        private var videoLengthLayout: View = itemView.findViewById(R.id.video_length_layout)
        private val videoLength: TextView = itemView.findViewById(R.id.video_length_tv)
        private val attachmentTypeIcon: ImageView = itemView.findViewById(R.id.attachment_type_icon)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(position: Int) {
            val groupMedia = mediaList[position]
            //message.text = recyclerViewModel.textData
            val attachmentPath = groupMedia.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(
                    groupMedia.attachmentType,
                    fileName
                )
            val fileHasBeenDownloaded = downloadedFile != null

            if (fileHasBeenDownloaded) {
                itemsDownloading.remove(groupMedia)
                attachmentDownloadingProgressBar.gone()

                if ((groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE)  || (groupMedia.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE)) {
                    playDownloadOverlayIV.gone()
                    playDownloadIconIV.gone()
                    videoLengthLayout.gone()

                    requestManager.load(downloadedFile).into(thumbnailIV)
                } else if ((groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO) || (groupMedia.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO)) {

                    playDownloadOverlayIV.visible()
                    playDownloadIconIV.visible()
                    videoLengthLayout.visible()

                    videoLength.text =
                        convertMicroSecondsToNormalFormat(groupMedia.videoAttachmentLength)
                    requestManager.load(R.drawable.ic_play).into(attachmentTypeIcon)

                    if (groupMedia.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(groupMedia.thumbnail!!)

                    requestManager.load(R.drawable.ic_play_2).into(playDownloadIconIV)
                } else {
                    throw IllegalArgumentException("other types not supperted yet")
                }
            } else {
                val isFileDownloading = itemsDownloading.contains(groupMedia)

                if ((groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE)  || (groupMedia.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE)) {
                    videoLengthLayout.gone()

                    if (groupMedia.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(groupMedia.thumbnail!!)
                    else
                        thumbnailIV.clearImage()

                    if (isFileDownloading) {
                        playDownloadOverlayIV.visible()
                        playDownloadIconIV.gone()
                        attachmentDownloadingProgressBar.visible()
                    } else {
                        attachmentDownloadingProgressBar.gone()
                        playDownloadOverlayIV.visible()
                        playDownloadIconIV.visible()
                        requestManager.load(R.drawable.ic_download_24).into(playDownloadIconIV)
                    }
                } else if ((groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO)  || (groupMedia.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO)) {

                    videoLengthLayout.visible()
                    videoLength.text =
                        convertMicroSecondsToNormalFormat(groupMedia.videoAttachmentLength)

                    requestManager.load(R.drawable.ic_play).into(attachmentTypeIcon)

                    if (groupMedia.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(groupMedia.thumbnail!!)
                    else
                        thumbnailIV.clearImage()

                    if (isFileDownloading) {
                        playDownloadOverlayIV.visible()
                        playDownloadIconIV.gone()
                        attachmentDownloadingProgressBar.visible()
                    } else {
                        attachmentDownloadingProgressBar.gone()
                        playDownloadOverlayIV.visible()
                        playDownloadIconIV.visible()
                        requestManager.load(R.drawable.ic_download_24).into(playDownloadIconIV)
                    }
                } else {
                    throw IllegalArgumentException("other types not supperted yet")
                }
            }
        }

        override fun onClick(p0: View?) {
            val pos = adapterPosition
            val groupMedia = mediaList[pos]

            val attachmentPath = groupMedia.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(
                    groupMedia.attachmentType,
                    fileName
                )

            onGroupMediaClickListener.onChatMediaClicked(
                position = pos,
                fileDownloaded = downloadedFile != null,
                fileIfDownloaded = downloadedFile,
                media = groupMedia
            )
        }
    }

    private inner class View2ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        //var message: TextView = itemView.findViewById(R.id.textView)

        private var docIcon: ImageView = itemView.findViewById(R.id.doc_icon)
        private var docFileName: TextView = itemView.findViewById(R.id.doc_file_name)
        private var docFileDetails: TextView = itemView.findViewById(R.id.doc_file_details)
        private var docFileDate: TextView = itemView.findViewById(R.id.doc_file_date)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(position: Int) {
            val mediaData = mediaList[position]
            //message.text = recyclerViewModel.textData
            val attachmentPath = mediaData.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(
                    mediaData.attachmentType,
                    fileName
                )
            val fileHasBeenDownloaded = downloadedFile != null


            docFileName.text = mediaData.attachmentName ?: ""
            val details = mediaData.attachmentName?.split(".")
            //val fileSize = Integer.parseInt((File(mediaData.attachmentPath).length()/1024).toString())
            docFileDetails.text = details?.get(details.size - 1)?.capitalize() ?: ""
            val date = mediaData.timestamp?.toDate()?.let { DateHelper.getDateInDDMMYYYY(it) }
            docFileDate.text = date
        }

        override fun onClick(p0: View?) {
            val pos = adapterPosition
            val groupMedia = mediaList[pos]

            val attachmentPath = groupMedia.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(
                    groupMedia.attachmentType,
                    fileName
                )

            onGroupMediaClickListener.onChatMediaClicked(
                position = pos,
                fileDownloaded = downloadedFile != null,
                fileIfDownloaded = downloadedFile,
                media = groupMedia
            )
        }
    }

    private inner class View3ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        //var message: TextView = itemView.findViewById(R.id.textView)
        private var audioIcon: ImageView = itemView.findViewById(R.id.audio_icon)
        private var audioFileName: TextView = itemView.findViewById(R.id.audio_file_name)
        private var audioFileDetails: TextView = itemView.findViewById(R.id.audio_file_details)
        private var audioFileDate: TextView = itemView.findViewById(R.id.audio_file_date)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(position: Int) {
            val audioData = mediaList[position]
            //message.text = recyclerViewModel.textData
            audioFileName.text = audioData.attachmentName ?: ""
            val details = audioData.attachmentName?.split(".")
            audioFileDetails.text = details?.get(details.size - 1)?.capitalize() ?: ""
            val date = audioData.timestamp?.toDate()?.let { DateHelper.getDateInDDMMYYYY(it) }
            audioFileDate.text = date
        }

        override fun onClick(p0: View?) {
            val pos = adapterPosition
            val groupMedia = mediaList[pos]

            val attachmentPath = groupMedia.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(
                    groupMedia.attachmentType,
                    fileName
                )

            onGroupMediaClickListener.onChatMediaClicked(
                position = pos,
                fileDownloaded = downloadedFile != null,
                fileIfDownloaded = downloadedFile,
                media = groupMedia
            )
        }
    }

    fun setData(groupMedia: List<GroupMedia>, type: Int) {
        this.mediaList = groupMedia
        this.type = type
        notifyDataSetChanged()
    }

    fun setItemAsDownloading(index: Int) {
        if (mediaList.size > index) {
            itemsDownloading.add(mediaList[index])
            notifyDataSetChanged()
        }
    }

    fun setItemAsNotDownloading(index: Int) {
        if (mediaList.size > index) {
            itemsDownloading.remove(mediaList[index])
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder =  View1ViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_image_item_view, parent, false))
        if (viewType == VIEW_TYPE_IMAGES) {
            viewHolder =  View1ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_image_item_view, parent, false)
            )
        }
        if (viewType == VIEW_TYPE_DOCS) {
            viewHolder =  View2ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_document_item_view, parent, false)
            )
        }
        if (viewType == VIEW_TYPE_AUDIO) {
            viewHolder =  View3ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.recycler_view_chat_audio_item_view, parent, false)
            )
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (type === VIEW_TYPE_IMAGES) {
            (holder as View1ViewHolder).bind(position)
        } else if (type === VIEW_TYPE_DOCS) {
            (holder as View2ViewHolder).bind(position)
        } else if (type === VIEW_TYPE_AUDIO) {
            (holder as View3ViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
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

    private fun returnFileAlreadyDownloadedElseNull(
        type: String,
        fileName: String
    ): File? {
        if ((type == ChatConstants.ATTACHMENT_TYPE_IMAGE) || (type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE)) {

            val file = File(imagesDirectoryRef, fileName)
            if (file.exists())
                return file
            else
                return null

        } else if ((type == ChatConstants.ATTACHMENT_TYPE_VIDEO) || (type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO)) {
            val file = File(videosDirectoryRef, fileName)
            if (file.exists())
                return file
            else
                return null
        } else if ((type == ChatConstants.ATTACHMENT_TYPE_DOCUMENT) || (type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT)) {
            val file = File(documentsDirectoryRef, fileName)
            if (file.exists())
                return file
            else
                return null

        } else if ((type == ChatConstants.ATTACHMENT_TYPE_AUDIO) || (type == ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO)) {
            val file = File(audiosDirectoryRef, fileName)
            if (file.exists())
                return file
            else
                return null

        }

        throw IllegalArgumentException("other types not supperted yet")
    }

    override fun getItemViewType(position: Int): Int {
        return type
    }


    interface OnMediaClickListener {
        fun onMediaClicked(
            position: Int,
            fileDownloaded: Boolean,
            fileIfDownloaded: File?,
            media: GroupMedia
        )
    }

}