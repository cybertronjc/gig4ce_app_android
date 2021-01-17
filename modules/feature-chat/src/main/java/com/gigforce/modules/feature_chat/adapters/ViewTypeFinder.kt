package com.gigforce.modules.feature_chat.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.gigforce.core.CoreViewHolder
import com.gigforce.modules.feature_chat.models.ChatConstants
import com.gigforce.modules.feature_chat.models.Message
import com.gigforce.modules.feature_chat.ui.chatItems.InTextMessage
import com.gigforce.modules.feature_chat.ui.chatItems.OutTextMessage
import java.lang.IllegalArgumentException

/*
        VIEWTYPE Numbering:
            {Module: 10} {view 101}
            {view 101} WITHIN CHAT:
                {IN: 1} {component: 01}
                {OUT: 2} {component: 01}
 */
class ChatViewTypes{

    companion object {

        const val IN_TEXT = 10101
        const val OUT_TEXT = 10201

        const val IN_IMAGE = 10102
        const val OUT_IMAGE = 10202

        private const val VIEW_TYPE_DATE = 0

        private const val VIEW_TYPE_CHAT_IMAGE = 2
        private const val VIEW_TYPE_CHAT_VIDEO = 3
        private const val VIEW_TYPE_CHAT_LOCATION = 4
        private const val VIEW_TYPE_CHAT_CONTACT = 5
        private const val VIEW_TYPE_CHAT_AUDIO = 6
        private const val VIEW_TYPE_CHAT_DOCUMENT = 7
        private const val VIEW_TYPE_MESSAGE_TYPE_NOT_SUPPORTED = 8
    }
}

interface IViewTypeFinder{
    fun getViewType(data:Any):Int
    fun getViewHolder(context: Context, viewType:Int):CoreViewHolder
}

class ViewTypeFinder: IViewTypeFinder {

    override fun getViewType(data:Any):Int{
        val msg = data as Message

        return when(msg.type){
            ChatConstants.MESSAGE_TYPE_TEXT -> if(msg.flowType == "in") ChatViewTypes.IN_TEXT else ChatViewTypes.OUT_TEXT
            ChatConstants.MESSAGE_TYPE_TEXT_WITH_IMAGE -> if(msg.flowType == "in") ChatViewTypes.IN_IMAGE else ChatViewTypes.OUT_IMAGE
            else -> -1
        }
    }

    private fun getView(context: Context, viewType: Int): View {
        return when(viewType){
            ChatViewTypes.IN_TEXT -> InTextMessage(context, null)
            ChatViewTypes.OUT_TEXT -> OutTextMessage(context, null)
            ChatViewTypes.IN_IMAGE -> InTextMessage(context, null)
            ChatViewTypes.OUT_IMAGE -> InTextMessage(context, null)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getViewHolder(context: Context, viewType:Int):CoreViewHolder{
        val view = getView(context, viewType)
        return CoreViewHolder(view)
    }
}