package com.gigforce.app.modules.profile.models

sealed class ProfileViewStates

object SettingUserAsAmbassador : ProfileViewStates()
object UserSetAsAmbassadorSuccessfully : ProfileViewStates()
data class ErrorWhileSettingUserAsAmbassador(val error : String) : ProfileViewStates()