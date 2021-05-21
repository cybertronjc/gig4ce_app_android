package com.gigforce.modules.feature_chat.screens.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
//import com.gigforce.app.R
//import com.gigforce.app.core.gone
//import com.gigforce.app.core.visible
//import com.gigforce.app.modules.chatmodule.ChatConstants
//import com.gigforce.app.modules.chatmodule.models.GroupMedia
//import com.gigforce.core.utils.FirebaseUtils
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.modules.feature_chat.R
import com.gigforce.common_ui.core.ChatConstants
import com.gigforce.modules.feature_chat.models.GroupMedia
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.util.concurrent.TimeUnit

class GroupMediaRecyclerAdapter(
    private val context: Context,
    private val refToGigForceAttachmentDirectory: File,
    private val requestManager: RequestManager,
    private val onGroupMediaClickListener: OnGroupMediaClickListener
) : RecyclerView.Adapter<GroupMediaRecyclerAdapter.GroupMediaViewHolder>() {

    private var groupMedia: List<GroupMedia> = emptyList()
    private var itemsDownloading: MutableList<GroupMedia> = mutableListOf()
    private val uid: String by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

    private var imagesDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_IMAGES)

    private var videosDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_VIDEOS)

    private var documentsDirectoryRef: File =
        File(refToGigForceAttachmentDirectory, ChatConstants.DIRECTORY_DOCUMENTS)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_item_group_media_2,
            parent,
            false
        )
        return GroupMediaViewHolder(view)
    }

    override fun getItemCount(): Int {
        return groupMedia.size
    }

    override fun onBindViewHolder(holder: GroupMediaViewHolder, position: Int) {
        holder.bindValues(groupMedia.get(position))
    }

    fun setData(groupMedia: List<GroupMedia>) {
        this.groupMedia = groupMedia
        notifyDataSetChanged()
    }

    fun setItemAsDownloading(index: Int) {
        if (groupMedia.size > index) {
            itemsDownloading.add(groupMedia[index])
            notifyDataSetChanged()
        }
    }

    fun setItemAsNotDownloading(index: Int) {
        if (groupMedia.size > index) {
            itemsDownloading.remove(groupMedia[index])
            notifyDataSetChanged()
        }
    }

    inner class GroupMediaViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

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

        fun bindValues(groupMedia: GroupMedia) {
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

                if (groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE) {
                    playDownloadOverlayIV.gone()
                    playDownloadIconIV.gone()
                    videoLengthLayout.gone()

                    requestManager.load(downloadedFile).into(thumbnailIV)
                } else if (groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO) {

                    playDownloadOverlayIV.visible()
                    playDownloadIconIV.visible()
                    videoLengthLayout.visible()

                    videoLength.text =
                        convertMicroSecondsToNormalFormat(groupMedia.videoAttachmentLength)
                    requestManager.load(R.drawable.ic_play).into(attachmentTypeIcon)

                    if (groupMedia.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(groupMedia.thumbnail!!)

                    requestManager.load(R.drawable.ic_play_2).into(playDownloadIconIV)
                } else if (groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_DOCUMENT) {
                    requestManager.load(R.drawable.ic_document_background)
                        .into(thumbnailIV)

                    videoLengthLayout.gone()
                    videoLength.text = ""

                    playDownloadOverlayIV.gone()
                    playDownloadIconIV.setImageDrawable(null)
                    attachmentDownloadingProgressBar.gone()
                } else {
                    throw IllegalArgumentException("other types not supperted yet")
                }
            } else {
                val isFileDownloading = itemsDownloading.contains(groupMedia)

                if (groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE) {
                    videoLengthLayout.gone()

                    if (groupMedia.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(groupMedia.thumbnail!!)

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
                } else if (groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO) {

                    videoLengthLayout.visible()
                    videoLength.text =
                        convertMicroSecondsToNormalFormat(groupMedia.videoAttachmentLength)
                    requestManager.load(groupMedia.thumbnail).into(thumbnailIV)
                    requestManager.load(R.drawable.ic_play).into(attachmentTypeIcon)

                    if (groupMedia.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(groupMedia.thumbnail!!)

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
                } else if (groupMedia.attachmentType == ChatConstants.ATTACHMENT_TYPE_DOCUMENT) {
                    //Need work
                    requestManager.load(R.drawable.ic_document_media_list)
                        .into(playDownloadOverlayIV)

                    videoLengthLayout.gone()
                    videoLength.text = ""

                    if (isFileDownloading) {
                        playDownloadOverlayIV.visible()
                        playDownloadIconIV.setImageDrawable(null)
                        attachmentDownloadingProgressBar.visible()
                    } else {
                        attachmentDownloadingProgressBar.gone()
                        playDownloadOverlayIV.visible()
                        requestManager.load(R.drawable.ic_download_24).into(playDownloadOverlayIV)
                    }
                } else {
                    throw IllegalArgumentException("other types not supperted yet")
                }
            }
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
            if (type == ChatConstants.ATTACHMENT_TYPE_IMAGE) {

                val file = File(imagesDirectoryRef, fileName)
                if (file.exists())
                    return file
                else
                    return null

            } else if (type == ChatConstants.ATTACHMENT_TYPE_VIDEO) {
                val file = File(videosDirectoryRef, fileName)
                if (file.exists())
                    return file
                else
                    return null
            } else if (type == ChatConstants.ATTACHMENT_TYPE_DOCUMENT) {
                val file = File(documentsDirectoryRef, fileName)
                if (file.exists())
                    return file
                else
                    return null

            }

            throw IllegalArgumentException("other types not supperted yet")
        }

        override fun onClick(v: View?) {
            val pos = adapterPosition
            val groupMedia = groupMedia[pos]

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


    interface OnGroupMediaClickListener {
        fun onChatMediaClicked(
            position: Int,
            fileDownloaded: Boolean,
            fileIfDownloaded: File?,
            media: GroupMedia
        )
    }
}