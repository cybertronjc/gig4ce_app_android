package com.gigforce.common_ui.chat.models

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

        const val IN_AUDIO = 10106
        const val OUT_AUDIO = 10206

        const val IN_DELETED_MESSAGE = 14301
        const val OUT_DELETED_MESSAGE = 14501

        const val GROUP_IN_TEXT = 10501
        const val GROUP_OUT_TEXT = 10601

        const val GROUP_IN_IMAGE = 10502
        const val GROUP_OUT_IMAGE = 10602

        const val GROUP_IN_VIDEO = 10503
        const val GROUP_OUT_VIDEO = 10603

        const val GROUP_IN_DOCUMENT = 10504
        const val GROUP_OUT_DOCUMENT = 10604

        const val GROUP_IN_LOCATION = 10505
        const val GROUP_OUT_LOCATION = 10605

        const val GROUP_IN_AUDIO = 10506
        const val GROUP_OUT_AUDIO = 10606

        const val GROUP_DETAILS_MEDIA = 11501
        const val GROUP_DETAILS_GROUP_MEMBER = 11601

        const val GROUP_IN_DELETED_MESSAGE = 18701
        const val GROUP_OUT_DELETED_MESSAGE = 17801

        const val GROUP_MESSAGE_READ_INFO = 17101

        const val CHAT_EVENT = 17134
    }
}