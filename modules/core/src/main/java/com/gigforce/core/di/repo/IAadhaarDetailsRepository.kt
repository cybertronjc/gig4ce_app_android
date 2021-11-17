package com.gigforce.core.di.repo

import com.gigforce.core.datamodels.City
import com.gigforce.core.datamodels.State
import com.gigforce.core.datamodels.profile.ProfileNominee
import com.gigforce.core.datamodels.verification.AadhaarDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel

interface IAadhaarDetailsRepository {
    suspend fun getStatesFromDb(): MutableList<State>
    suspend fun getVerificationDetails(uid: String): VerificationBaseModel?
    suspend fun getCities(stateCode: String): MutableList<City>
    suspend fun setAadhaarFromVerificationModule(uid: String, nomineeAsFather : Boolean,aadhaardetails: AadhaarDetailsDataModel): Boolean
    suspend fun getProfileNominee(uid: String):ProfileNominee?
}