package com.gigforce.app.modules.onboarding.models

data class OnboardingChatLog(
    val id:String,
    val flow:String,
    val text:String,
    val input_type:String,
    val userid: String,
    val user_type: String
) {
}