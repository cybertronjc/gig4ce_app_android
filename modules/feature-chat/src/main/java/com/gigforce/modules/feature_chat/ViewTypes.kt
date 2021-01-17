package com.gigforce.modules.feature_chat

/*
        VIEWTYPE Numbering:
            {Module: 10} {view 101}
            {view 101} WITHIN CHAT:
                {Header: 0} {component: 01}
                {IN: 1} {component: 01}
                {OUT: 2} {component: 01}
 */
class ViewTypes {
    companion object {

        const val CHAT_HEADER = 10001

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