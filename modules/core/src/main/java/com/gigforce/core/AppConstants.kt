package com.gigforce.core

object AppConstants {
    val USER_PROFILE_PIC: String = "profile_pic"
    val USER_MOBILE_NUMBER: String = "mobileno"
    val USER_NAME: String = "username"
    val LANGUAGE_SELECTED: String = "language_selected"
    const val UNLOCK_FEATURE = false
    const val ON_BOARDING_COMPLETED = "on_boarding_completed"
    const val SHOULD_CHECK_FOR_JOININGS_APPLICATIONS = "should_check_joining_applications"
    // shared preferences keys
    const val APP_LANGUAGE_CODE = "app_lang_code";
    const val APP_LANGUAGE_NAME = "app_lang_name";
    const val DEVICE_LANGUAGE = "device_language"
    const val  INTRO_COMPLETE = "intro_complete"
    const val  ALL_MOBILE_NUMBERS_USED = "all_mobile_no_used"

    const val PINCODE_URL = "https://api.postalpincode.in/pincode/"
    const val idfyAcid = "fd5931df2bde/f8451777-05d8-4e0f-b859-ad5dfa895bd4";
    const val idfyApiKey = "1bc58043-00fb-4799-bea3-93a012d174bb";
    const val IDFY_BASE_URL = "https://eve.idfy.com/v3/tasks/"


    const val IMAGE_URL = "imageUrl"
    const val CONTACT_NAME = "contactName"



    const val INTENT_EXTRA_CHAT_TYPE = "chat_type"
    const val INTENT_EXTRA_CHAT_HEADER_ID = "chat_header_id"
    const val INTENT_EXTRA_OTHER_USER_ID = "sender_id"
    const val INTENT_EXTRA_OTHER_USER_NAME = "sender_name"
    const val INTENT_EXTRA_OTHER_USER_IMAGE = "sender_profile"
    const val INTENT_EXTRA_CHAT_MESSAGE_ID = "chat_message_id"
    const val CHAT_TYPE_USER = "user"
    const val CHAT_TYPE_GROUP = "group"
    const val INTENT_EXTRA_END_LIVE_LOCATION = "end_live_location"


    const val LEARNING_IMAGES_FIREBASE_FOLDER = "learning_images"


    const val INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT = "user_came_from_amb_screen"
    const val INTENT_EXTRA_USER_CAME_FROM_ONBOARDING_FORM = "came_from_onboarding_form"
    const val INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE = "came_from_attendance"
    const val INTENT_EXTRA_UID = "uid"

    const val INTENT_EXTRA_COURSE_ID = "course_id"

    const val REQUEST_CODE_UPLOAD_PAN_IMAGE = 2333

    const val INTENT_EXTRA_TITLE = "title"
    const val INTENT_EXTRA_CONTENT = "content"

    const val INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT = "referral_link_with_text"
    const val INTENT_EXTRA_REFERRAL_LINK = "referral_link"

    const val ACTION_OPEN_EDIT_EDUCATION_BOTTOM_SHEET = 11
    const val ACTION_OPEN_EDIT_SKILLS_BOTTOM_SHEET = 12
    const val ACTION_OPEN_EDIT_ACHIEVEMENTS_BOTTOM_SHEET = 13

    const val ACTION_OPEN_EDIT_EXPERIENCE_BOTTOM_SHEET = 21

    const val ACTION_OPEN_EDIT_ABOUT_ME_BOTTOM_SHEET = 31
    const val ACTION_OPEN_EDIT_LANGUAGE_BOTTOM_SHEET = 32


    const val INTENT_EXTRA_CAME_FROM_LANDING_SCREEN = "came_from_landing"
    const val INTENT_EXTRA_ACTION = "action_to_do"

    const val INTENT_EXTRA_GIG_ID = "gig_id"

    const val INTEN_EXTRA_DATE = "date"

    const val CONTACTS_SYNCED = "contacts_synced"


    //app bar component constants
    const val BACKGROUND_TYPE_DEFAULT = 0
    const val BACKGROUND_TYPE_PINKBAR = 101
    const val BACKGROUND_TYPE_WHITEBAR = 102
    const val BACKGROUND_TYPE_GREYBAR = 103

    //verification image view component constants
    const val UPLOAD_SUCCESS = 0
    const val DETAILS_MISMATCH = 1
    const val UNABLE_TO_FETCH_DETAILS = 2
    const val VERIFICATION_COMPLETED = 3

}