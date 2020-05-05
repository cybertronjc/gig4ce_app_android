package com.gigforce.app.modules.verification.service

import com.gigforce.app.modules.verification.AppConstants

object ApiFactory{

    val idfyApi : IdfyApiAadhaar = RetrofitFactory.retrofit(AppConstants.IDFY_BASE_URL)
        .create(IdfyApiAadhaar::class.java)

}