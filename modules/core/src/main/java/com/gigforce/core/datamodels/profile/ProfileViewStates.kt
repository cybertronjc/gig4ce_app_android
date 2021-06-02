package com.gigforce.core.datamodels.profile

sealed class ProfileViewStates

object SettingUserAsAmbassador : ProfileViewStates()
object UserSetAsAmbassadorSuccessfully : ProfileViewStates()
data class ErrorWhileSettingUserAsAmbassador(val error : String) : ProfileViewStates()