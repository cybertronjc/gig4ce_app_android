package com.gigforce.app.di.implementations

import com.gigforce.app.BuildConfig
import com.gigforce.core.di.interfaces.IBuildConfig
import javax.inject.Inject

class BuildConfigImp @Inject constructor(): IBuildConfig {

    override fun getDrivingCertificateMethod(): String =
        when (BuildConfig.BUILD_TYPE) {
            "dev" -> "DrivingCertificate-Dev"
            "prod" -> "DrivingCertificate-Prod"
            "staging" -> "DrivingCertificate-Staging"
            else -> ""
        }


}