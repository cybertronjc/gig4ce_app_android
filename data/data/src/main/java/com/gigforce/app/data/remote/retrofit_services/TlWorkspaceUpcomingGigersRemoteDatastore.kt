package com.gigforce.app.data.remote.retrofit_services

import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkspaceUpcomingGigersRemoteDatastore @Inject constructor() {

    suspend fun getUpcomingGigers(): List<UpcomingGigersApiModel> {
        return emptyList()
    }

}