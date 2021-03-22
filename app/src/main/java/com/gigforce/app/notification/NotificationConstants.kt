package com.gigforce.app.notification

object NotificationConstants {

    const val INTENT_EXTRA_CLICK_ACTION = "click_action"

    object BROADCAST_ACTIONS{
        const val SHOW_CHAT_NOTIFICATION = "com.gigforce.app.gig.show_chat_notif"

    }

    object CLICK_ACTIONS {
        const val OPEN_GIG_ATTENDANCE_PAGE = "com.gigforce.app.gig.open_gig_attendance_page"
        const val OPEN_GIG_ATTENDANCE_PAGE_2 = "com.gigforce.app.gig.open_gig_page_2"
        const val OPEN_VERIFICATION_PAGE = "com.gigforce.app.verification.open_verification_page"
        const val OPEN_CHAT_PAGE = "com.gigforce.app.chats.open_chat"
        const val OPEN_GROUP_CHAT_PAGE = "com.gigforce.app.chats.open_group_chat"

    }

    object PAYLOADS{
        const val GIG_ID = "gig_id"
    }

}