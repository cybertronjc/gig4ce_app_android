package com.gigforce.app.modules.verification

import com.gigforce.app.BuildConfig

object AppConstants{
    const val JSON_PLACEHOLDER_BASE_URL = "https://jsonplaceholder.typicode.com"
    const val IDFY_BASE_URL = "https://eve.idfy.com/v3/tasks/"
    const val IDFY_PHOTO_URL = "https://eve.idfy.com/v3/tasks/sync/extract/ind_aadhaar"
    var idfyAcid = "fd5931df2bde/f8451777-05d8-4e0f-b859-ad5dfa895bd4";//BuildConfig.IDFY_AC_ID
    var idfyApiKey = "1bc58043-00fb-4799-bea3-93a012d174bb";//BuildConfig.IDFY_API_KEY

}