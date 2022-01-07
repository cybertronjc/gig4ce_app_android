package com.gigforce.modules.feature_chat.ui

import com.gigforce.modules.feature_chat.R
import java.io.Serializable


class AttachmentOption(val id: Int, val title: String, val resourceImage: Int) : Serializable {


    companion object {
        const val DOCUMENT_ID: Int = 101
        const val CAMERA_ID: Int = 102
        const val GALLERY_ID: Int = 103
        const val AUDIO_ID: Int = 104
        const val LOCATION_ID: Int = 105
        const val VIDEO_ID: Int = 106
        val defaultList: List<AttachmentOption>
            get() {
                val attachmentOptions: MutableList<AttachmentOption> = ArrayList()
                attachmentOptions.add(
                    AttachmentOption(
                        DOCUMENT_ID.toInt(),
                        "Document",
                        R.drawable.ic_document_attachment
                    )
                )
                attachmentOptions.add(
                    AttachmentOption(
                        CAMERA_ID.toInt(),
                        "Camera",
                        R.drawable.ic_camera_attachement
                    )
                )
                attachmentOptions.add(
                    AttachmentOption(
                        GALLERY_ID.toInt(),
                        "Gallery",
                        R.drawable.ic_gallary_attachment
                    )
                )
                attachmentOptions.add(
                    AttachmentOption(
                        AUDIO_ID.toInt(),
                        "Audio",
                        R.drawable.ic_audio_attachment
                    )
                )
                attachmentOptions.add(
                    AttachmentOption(
                        LOCATION_ID.toInt(),
                        "Location",
                        R.drawable.ic_location_attachment
                    )
                )
//                attachmentOptions.add(
//                    AttachmentOption(
//                        VIDEO_ID.toInt(),
//                        "Video",
//                        R.drawable.ic_contact_attachment
//                    )
//                )
                return attachmentOptions
            }
    }
}