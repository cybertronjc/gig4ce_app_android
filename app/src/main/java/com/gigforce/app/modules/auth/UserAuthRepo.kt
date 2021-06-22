package com.gigforce.app.modules.auth

import com.gigforce.common_ui.remote.GigerAuthService
import com.gigforce.common_ui.viewdatamodels.UserAuthStatusModel
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics

class UserAuthRepo(private val iBuildConfigVM: IBuildConfigVM) {
    private val gigerAuthService: GigerAuthService = RetrofitFactory.createService(
            GigerAuthService::class.java
    )
    suspend fun getUserAuthStatus(mobileNo : String):UserAuthStatusModel{
        var userAuthStatus = gigerAuthService.getGigersAuthStatus(iBuildConfigVM.getUserRegisterInfoUrl(),mobileNo)
        if(userAuthStatus.isSuccessful){
            return userAuthStatus.body()!!
        }
        else{
            FirebaseCrashlytics.getInstance().log("Exception : checkIfSignInOrSignup Method ${userAuthStatus.message()}")
            throw Exception("Issue in Authentication result")
        }
    }
}