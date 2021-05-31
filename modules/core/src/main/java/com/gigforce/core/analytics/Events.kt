package com.gigforce.core.analytics

object ClientActivationEvents{

    const val EVENT_USER_CLICKED = "Gig_application_viewed"
    const val EVENT_APPLICATION_PAGE_LOADED = "Gig_application_loaded"
    const val USER_TAPPED_DESCRIPTION_VIDEO = "Gig_description_video"
    const val USER_TAPPED_REQUIREMENT_VIDEO = "Gig_requirement_video"
    const val USER_TAPPED_ON_INTRESTED = "Gig_application_interested"
    const val USER_TAPPED_ON_SHARE = "Gig_application_share"
    const val USER_SUBMITTED_APPLICATION = "Gig_application_submit"
}

object LanguageEvents{
    const val LANGUAGE_SELECTED = "app_lang_selected"
}


object AuthEvents{
    const val SIGN_UP_LOADED = "signup_loaded"
    const val SIGN_UP_STARTED = "signup_started"
    const val SIGN_UP_ERROR = "signup_error"
    const val LOGIN_ERROR = "login_error"
    const val SIGN_RESEND_OTP = "signup_resend_otp"
    const val LOGIN_RESEND_OTP = "login_resend_otp"
    const val SIGN_SUCCESS = "signup_success"
    const val LOGIN_STARTED = "login_started"
    const val LOGIN_SUCCESS = "login_success"
    const val SIGN_OUT_SUCCESS = "signout_success"

    const val SIGN_UP_OTP_SCREEN_LOADED = "verify_otp_loaded"
}