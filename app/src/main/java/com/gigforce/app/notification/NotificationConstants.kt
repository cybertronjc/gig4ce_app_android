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
        const val OPEN_CALENDAR_HOME_SCREEN = "com.gigforce.app.calendar.open_calendar_home_screen"
        const val OPEN_JOINING_SCREEN = "com.gigforce.app.joinings.open_joining_screen"
        const val OPEN_VERIFICATION_PAN_SCREEN = "com.gigforce.app.verification.pan"
        const val OPEN_VERIFICATION_DL_SCREEN = "com.gigforce.app.verification.dl"
        const val OPEN_VERIFICATION_AADHAAR_SCREEN = "com.gigforce.app.verification.aadhaar"
        const val OPEN_VERIFICATION_BANK_SCREEN = "com.gigforce.app.verification.bank"

    }

    object PAYLOADS{
        const val GIG_ID = "gig_id"
    }

    object TOPICS{
        const val TOPIC_SYNC_DATA = "sync_data"
    }

    object GlobalKeys{
        const val IS_SILENT_PUSH = "gigforce_silent_push"
        const val SILENT_PURPOSE = "silent_push_purpose"

        const val TASK_SYNC_GEOFENCES = "sync_geofences"
        const val TASK_SYNC_CURRENT_LOCATION_FOR_GIG = "sync_location_for_gig"
        const val TASK_UNSYNCED_DATA = "sync_unsynced_data"
    }
}