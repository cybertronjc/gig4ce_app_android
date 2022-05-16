package com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers

import com.gigforce.app.data.remote.retrofit_services.TlWorkspaceUpcomingGigersRemoteDatastore
import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceUpcomingGigersRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceUpcomingGigersRepositoryImpl @Inject constructor(
    private val remoteDatastore: TlWorkspaceUpcomingGigersRemoteDatastore
) : TLWorkspaceUpcomingGigersRepository {

    override suspend fun getUpcomingGigers(): List<UpcomingGigersApiModel> {
        TODO("Not yet implemented")
    }
}