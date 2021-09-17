package com.gigforce.core.di.repo

import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.verification.VerificationBaseModel

interface IAadhaarDetailsRepository {
    suspend fun getStatesFromDb(): MutableList<State>
    suspend fun getVerificationDetails(): VerificationBaseModel?
    suspend fun getCities(stateCode: String): MutableList<City>
}