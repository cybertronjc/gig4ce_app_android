package com.gigforce.modules.feature_chat.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.gigforce.common_ui.chat.ChatConstants
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.chat.models.GroupMedia
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.IEventTracker
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.fb.FirebaseUtils
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.ChatMediaRecyclerItemViewLayoutBinding
import com.gigforce.modules.feature_chat.models.ChatMediaDocsRecyclerItemData
import com.gigforce.modules.feature_chat.models.ChatMediaViewModels
import com.gigforce.modules.feature_chat.screens.AudioPlayerBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.concurrent.TimeUnit
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

    @Inject
    lateinit var chatFileManager : ChatFileManager


    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }



    private var viewData: ChatMediaViewModels.ChatMediaImageItemData? = null

    //views
    private lateinit var thumbnailIV: GigforceImageView
    private lateinit var playDownloadIconIV: ImageView
    private lateinit var playDownloadOverlayIV: ImageView
    private lateinit var attachmentDownloadingProgressBar: ProgressBar
    private lateinit var videoLengthLayout: View
    private lateinit var videoLength: TextView
    private lateinit var attachmentTypeIcon: ImageView

    private var imagesDirectoryRef: File =
        File(chatFileManager.gigforceDirectory, ChatConstants.DIRECTORY_IMAGES)

    private var videosDirectoryRef: File =
        File(chatFileManager.gigforceDirectory, ChatConstants.DIRECTORY_VIDEOS)

    private var documentsDirectoryRef: File =
        File(chatFileManager.gigforceDirectory, ChatConstants.DIRECTORY_DOCUMENTS)

    private var audiosDirectoryRef: File = File(chatFileManager.gigforceDirectory, ChatConstants.DIRECTORY_AUDIOS)

    private var itemsDownloading: MutableList<GroupMedia> = mutableListOf()

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
        thumbnailIV = this.findViewById(R.id.thumbnail_imageview)
//        thumbnailImageView = this.findViewById(R.id.thumbnail_imageview)
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

            val attachmentPath = viewData!!.attachmentPath ?: return
            val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

            val downloadedFile =
                returnFileAlreadyDownloadedElseNull(
                    viewData!!.attachmentType,
                    fileName
                )
            val fileHasBeenDownloaded = downloadedFile != null

            if (fileHasBeenDownloaded) {
                itemsDownloading.remove(viewData)
                attachmentDownloadingProgressBar.gone()

                if ((viewData!!.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE)  || (viewData!!.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE)) {
                    playDownloadOverlayIV.gone()
                    playDownloadIconIV.gone()
                    videoLengthLayout.gone()

                    GlideApp.with(context).load(downloadedFile).into(thumbnailIV)
                } else if ((viewData!!.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO) || (viewData!!.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO)) {

                    playDownloadOverlayIV.visible()
                    playDownloadIconIV.visible()
                    videoLengthLayout.visible()

                    videoLength.text =
                        convertMicroSecondsToNormalFormat(viewData!!.videoAttachmentLength)
                    GlideApp.with(context).load(R.drawable.ic_play).into(attachmentTypeIcon)

                    if (viewData!!.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(viewData!!.thumbnail!!)

                    GlideApp.with(context).load(R.drawable.ic_play_2).into(playDownloadIconIV)
                }
                else {
                    throw IllegalArgumentException("other types not supperted yet")
                }
            } else {
                val isFileDownloading = itemsDownloading.contains(viewData!!)

                if ((viewData!!.attachmentType == ChatConstants.ATTACHMENT_TYPE_IMAGE)  || (viewData!!.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE)) {
                    videoLengthLayout.gone()

                    if (viewData!!.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(viewData!!.thumbnail!!)
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
                        GlideApp.with(context).load(R.drawable.ic_download_24).into(playDownloadIconIV)
                    }
                } else if ((viewData!!.attachmentType == ChatConstants.ATTACHMENT_TYPE_VIDEO)  || (viewData!!.attachmentType == ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO)) {

                    videoLengthLayout.visible()
                    videoLength.text =
                        convertMicroSecondsToNormalFormat(viewData!!.videoAttachmentLength)

                    GlideApp.with(context).load(R.drawable.ic_play).into(attachmentTypeIcon)

                    if (viewData!!.thumbnail != null)
                        thumbnailIV.loadImageIfUrlElseTryFirebaseStorage(viewData!!.thumbnail!!)
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
                        GlideApp.with(context).load(R.drawable.ic_download_24).into(playDownloadIconIV)
                    }
                }
                else {
                    throw IllegalArgumentException("other types not supperted yet")
                }
            }
        }
    }

    override fun onClick(v: View?) {
        val currentViewData = viewData ?: return
        val attachmentPath = currentViewData.attachmentPath ?: return
        val fileName: String = FirebaseUtils.extractFilePath(attachmentPath)

        val downloadedFile =
            returnFileAlreadyDownloadedElseNull(
                currentViewData.attachmentType,
                fileName
            )

        onMediaClick(
            fileDownloaded = downloadedFile != null,
            fileIfDownloaded = downloadedFile,
            media = currentViewData
        )
    }

    fun onMediaClick(
        fileDownloaded: Boolean,
        fileIfDownloaded: File?,
        media: ChatMediaViewModels.ChatMediaImageItemData
    ){
        if (fileDownloaded) {
            //Open the file
            Log.d("type", "type : ${media.attachmentType}")
            when (media.attachmentType) {
                ChatConstants.ATTACHMENT_TYPE_IMAGE -> {

                    if (fileIfDownloaded != null) {
                        chatNavigation.openFullScreenImageViewDialogFragment(
                            fileIfDownloaded.toUri()
                        )
                    }
                }
                ChatConstants.ATTACHMENT_TYPE_VIDEO -> {

                    if (fileIfDownloaded != null) {
                        chatNavigation.openFullScreenVideoDialogFragment(
                            fileIfDownloaded.toUri()
                        )
                    }
                }
                ChatConstants.ATTACHMENT_TYPE_DOCUMENT -> {
                    openDocument(fileIfDownloaded!!)
                }
                ChatConstants.ATTACHMENT_TYPE_AUDIO -> {
                    openAudioPlayerBottomSheet(fileIfDownloaded!!)
                }

                ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> {

                    if (fileIfDownloaded != null) {
                        chatNavigation.openFullScreenImageViewDialogFragment(
                            fileIfDownloaded.toUri()
                        )
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_VIDEO -> {

                    if (fileIfDownloaded != null) {
                        chatNavigation.openFullScreenVideoDialogFragment(
                            fileIfDownloaded.toUri()
                        )
                    }
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_DOCUMENT -> {
                    openDocument(fileIfDownloaded!!)
                }
                ChatConstants.MESSAGE_TYPE_TEXT_WITH_AUDIO -> {
                    openAudioPlayerBottomSheet(fileIfDownloaded!!)
                }
            }

        } else {
            //Start downloading the file
//            if (chatType == ChatConstants.CHAT_TYPE_USER){
//                chatViewModel.downloadAndSaveFile(chatFileManager.gigforceDirectory, position, media)
//            } else if (chatType == ChatConstants.CHAT_TYPE_GROUP){
//                viewModel.downloadAndSaveFile(chatFileManager.gigforceDirectory, position, media)
//            }


        }
    }

    private fun openAudioPlayerBottomSheet(file: File) {
        navigation.navigateTo(
            "chats/audioPlayer", bundleOf(
                AudioPlayerBottomSheetFragment.INTENT_EXTRA_URI to file.path!!
            )
        )
    }

    private fun openDocument(file: File) {
        Intent(Intent.ACTION_VIEW).apply {

            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )
            setDataAndType(
                uri,
                ImageMetaDataHelpers.getImageMimeType(
                    context,
                    file.toUri()
                )
            )

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(this)
            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Unable to open document",
                    Toast.LENGTH_SHORT
                ).show()
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



}