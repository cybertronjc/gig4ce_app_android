package com.gigforce.profile.onboarding

interface OnFragmentFormCompletionListener {
    fun enableDisableNextButton(validate: Boolean)

    fun checkForButtonText() {}

    fun profilePictureSkipPressed()
}