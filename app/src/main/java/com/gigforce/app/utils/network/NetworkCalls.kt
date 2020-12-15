package com.gigforce.app.utils.network

import com.gigforce.app.BuildConfig
import com.gigforce.app.modules.client_activation.models.DrivingCertificateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkCalls {

    @GET(BuildConfig.DRIVING_CERTIFICATE_METHOD)
    suspend fun downloadDrivingLicense(@Query("_id") applicationId: String, @Query("driving_certificate_id") drivingCertId: String): DrivingCertificateResponse
}