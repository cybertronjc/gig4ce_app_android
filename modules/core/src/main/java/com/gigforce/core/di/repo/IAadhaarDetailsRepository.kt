package com.gigforce.core.di.repo

import com.gigforce.core.datamodels.State

interface IAadhaarDetailsRepository {
    suspend fun getStatesFromDb(): MutableList<State>
}