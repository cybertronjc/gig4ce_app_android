package com.gigforce.modules.feature_chat.models

import com.gigforce.core.SimpleDVM
import com.gigforce.modules.feature_chat.FeatureChatViewTypes

open class ChatMediaViewModels(
    val type: Int
) : SimpleDVM(type){


    data class ChatMediaImageItemData(
        val imageUrl: String
    ) : ChatMediaViewModels(
        FeatureChatViewTypes.ChatMediaRecyclerItem
    )

    data class ChatMediaDateItemData(
        val dateString: String
    ): ChatMediaViewModels(
        FeatureChatViewTypes.ChatDateRecyclerItem
    )
}

open class ChatDocsViewModels(
    val type: Int
) : SimpleDVM(type){

    data class ChatMediaDocItemData(
        val docUrl: String,
        val docName: String,
        val docDetail: String,
        val docDate: String
    ) : ChatDocsViewModels(
        FeatureChatViewTypes.ChatDocumentRecyclerItem
    )


    data class ChatMediaDateItemData(
        val dateString: String
    ): ChatDocsViewModels(
        FeatureChatViewTypes.ChatDateRecyclerItem
    )

}

open class ChatAudioViewModels(
    val type: Int
) : SimpleDVM(type){

    data class ChatMediaAudioItemData(
        val audioUrl: String,
        val audioName: String,
        val audioDetail: String,
        val audioDate: String
    ) : ChatAudioViewModels(
        FeatureChatViewTypes.ChatAudioRecyclerItem
    )

    data class ChatMediaDateItemData(
        val dateString: String
    ): ChatAudioViewModels(
        FeatureChatViewTypes.ChatDateRecyclerItem
    )

}