package com.gigforce.app.data.remote.retrofit_services

import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel

interface TlWorkspaceUpcomingGigersRemoteDatastore {

    suspend fun getUpcomingGigers(): List<UpcomingGigersApiModel>
}