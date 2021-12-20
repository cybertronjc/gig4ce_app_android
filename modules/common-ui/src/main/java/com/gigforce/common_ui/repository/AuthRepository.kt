package com.gigforce.common_ui.repository

import com.gigforce.common_ui.ext.bodyOrThrow
import com.gigforce.common_ui.remote.AuthService
import com.gigforce.core.fb.BaseFirestoreDBRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService
) {

    suspend fun getOrCreateUserInAuthAndProfile(
        userMobile: String,
        userName: String
    ) = authService.getOrCreateUserInAuthAndProfile(
        mobileNumber = userMobile,
        userName = userName
    ).bodyOrThrow()

}