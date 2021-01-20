package com.gigforce.modules.feature_chat.core

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

        const val IN_VIDEO = 10103
        const val OUT_VIDEO = 10203

        const val IN_DOCUMENT = 10104
        const val OUT_DOCUMENT = 10204

        const val IN_LOCATION = 10105
        const val OUT_LOCATION = 10205
    }
}