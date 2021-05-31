package com.gigforce.verification.oldverification.service

import com.gigforce.core.AppConstants
import com.gigforce.core.retrofit.IdfyApiAadhaar

object ApiFactory{

    val idfyApi : IdfyApiAadhaar = RetrofitFactory.retrofit(AppConstants.IDFY_BASE_URL)
        .create(IdfyApiAadhaar::class.java)

}