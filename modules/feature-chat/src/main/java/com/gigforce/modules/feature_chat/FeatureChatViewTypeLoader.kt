package com.gigforce.modules.feature_chat

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.modules.feature_chat.views.ChatAudioRecyclerItemView
import com.gigforce.modules.feature_chat.views.ChatMediaRecyclerItemView
import com.gigforce.modules.feature_chat.views.ChatDocumentRecyclerItemView
import com.gigforce.modules.feature_chat.views.ChatDateRecyclerItemView


object FeatureChatViewTypeLoader : IViewTypeLoader {

    override fun getView(
        context: Context,
        viewType: Int
    ): View? {

        return when (viewType) {

                FeatureChatViewTypes.ChatDateRecyclerItem -> ChatDateRecyclerItemView(
                context,
                null
                )
                FeatureChatViewTypes.ChatMediaRecyclerItem -> ChatMediaRecyclerItemView(
                context,
                null
                )
                FeatureChatViewTypes.ChatDocumentRecyclerItem -> ChatDocumentRecyclerItemView(
                context,
                null
                )
                FeatureChatViewTypes.ChatAudioRecyclerItem -> ChatAudioRecyclerItemView(
                context,
                null
                )
                else -> null

            }

        }

}