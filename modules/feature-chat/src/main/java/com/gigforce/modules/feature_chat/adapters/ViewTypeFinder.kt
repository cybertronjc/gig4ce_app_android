package com.gigforce.modules.feature_chat.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.gigforce.core.CoreViewHolder
import com.gigforce.core.IDataViewTypeGetter
import com.gigforce.core.IViewTypeFinder
import com.gigforce.modules.feature_chat.ViewTypes
import com.gigforce.modules.feature_chat.models.ChatConstants
import com.gigforce.modules.feature_chat.models.ChatListItemDataObject
import com.gigforce.modules.feature_chat.models.Message
import com.gigforce.modules.feature_chat.ui.ChatListItem
import com.gigforce.modules.feature_chat.ui.chatItems.InTextMessage
import com.gigforce.modules.feature_chat.ui.chatItems.OutTextMessage
import java.lang.IllegalArgumentException
import javax.inject.Inject

class ViewTypeFinder @Inject constructor(): IViewTypeFinder {

    private fun getView(context: Context, viewType: Int): View {
        return when(viewType){
            ViewTypes.CHAT_HEADER -> ChatListItem(context)
            ViewTypes.IN_TEXT -> InTextMessage(context, null)
            ViewTypes.OUT_TEXT -> OutTextMessage(context, null)
            ViewTypes.IN_IMAGE -> InTextMessage(context, null)
            ViewTypes.OUT_IMAGE -> InTextMessage(context, null)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getViewHolder(context: Context, viewType:Int):CoreViewHolder{
        val view = getView(context, viewType)
        return CoreViewHolder(view)
    }
}