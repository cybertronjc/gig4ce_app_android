package com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkspaceUpcomingGigersRemoteDatastore @Inject constructor(
    private val remoteService: TLWorkUpcomingGigersService
)  {

    suspend fun getUpcomingGigers(): List<UpcomingGigersApiModel> {
        return remoteService
            .getUpcomingGigers(
                GetUpcomingGigersRequest()
            ).bodyFromBaseResponseElseThrow()
            .upcomingGigers
    }
}