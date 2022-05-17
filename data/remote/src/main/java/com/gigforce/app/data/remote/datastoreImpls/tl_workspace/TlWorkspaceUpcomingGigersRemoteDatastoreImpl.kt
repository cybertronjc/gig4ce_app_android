package com.gigforce.app.data.remote.datastoreImpls.tl_workspace

import com.gigforce.app.data.remote.retrofitServices.TLWorkUpcomingGigersService
import com.gigforce.app.data.remote.retrofit_services.TlWorkspaceUpcomingGigersRemoteDatastore
import com.gigforce.app.data.remote.utils_ktx.bodyFromBaseResponseElseThrow
import com.gigforce.app.domain.models.tl_workspace.GetUpcomingGigersRequest
import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkspaceUpcomingGigersRemoteDatastoreImpl @Inject constructor(
    private val remoteService: TLWorkUpcomingGigersService
) : TlWorkspaceUpcomingGigersRemoteDatastore {

    override suspend fun getUpcomingGigers(): List<UpcomingGigersApiModel> {
        return remoteService
            .getUpcomingGigers(
                GetUpcomingGigersRequest()
            ).bodyFromBaseResponseElseThrow()
            .upcomingGigers
    }
}