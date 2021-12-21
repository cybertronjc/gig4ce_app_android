package com.gigforce.modules.feature_chat.models

import com.gigforce.core.SimpleDVM
import com.gigforce.modules.feature_chat.FeatureChatViewTypes


open class ChatMediaDocsRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class ChatDocumentRecyclerItemData(
        val documentPath: String? = null
    ) : ChatMediaDocsRecyclerItemData(
        FeatureChatViewTypes.ChatDocumentRecyclerItem
    )

    data class ChatAudioRecyclerItemData(
        val audioPath: String? = null
    ) : ChatMediaDocsRecyclerItemData(
        FeatureChatViewTypes.ChatAudioRecyclerItem
    )

    data class ChatMediaRecyclerItemData(
        val mediaPath: String? = null
    ): ChatMediaDocsRecyclerItemData(
        FeatureChatViewTypes.ChatMediaRecyclerItem
    )

    data class ChatDateRecyclerItemData(
        val dateTime: String? = null
    ): ChatMediaDocsRecyclerItemData(
        FeatureChatViewTypes.ChatDateRecyclerItem
    )
}