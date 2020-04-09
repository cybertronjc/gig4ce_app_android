package com.gigforce.app.modules.verification.service

import com.gigforce.app.modules.verification.AppConstants

object ApiFactory{

    val placeholderApi : PlaceholderApi = RetrofitFactory.retrofit(AppConstants.JSON_PLACEHOLDER_BASE_URL)
                                                .create(PlaceholderApi::class.java)

    val idfyApi : IdfyApi = RetrofitFactory.retrofit(AppConstants.IDFY_BASE_URL)
        .create(IdfyApi::class.java)
}