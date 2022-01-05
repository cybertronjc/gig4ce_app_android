package com.gigforce.modules.feature_chat.models

import com.gigforce.core.SimpleDVM

open class ChatMediaViewModels(
    val type: Int
) : SimpleDVM(type){

    data class ChatMediaImageItemData(
        val imageUrl: String
    ) : ChatMediaViewModels(

    )

    data class ChatMediaDocItemData(
        val docUrl: String,
        val docName: String,
        val docDetail: String,
        val docDate: String
    ) : ChatMediaViewModels(

    )

    data class ChatMediaAudioItemData(
        val audioUrl: String,
        val audioName: String,
        val audioDetail: String,
        val audioDate: String
    ) : ChatMediaViewModels(

    )

    data class ChatMediaDateItemData(
        val dateString: String
    ): ChatMediaViewModels(

    )
}