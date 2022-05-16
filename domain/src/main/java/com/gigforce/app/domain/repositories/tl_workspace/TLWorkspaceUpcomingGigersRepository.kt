package com.gigforce.app.domain.repositories.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel

interface TLWorkspaceUpcomingGigersRepository {

    suspend fun getUpcomingGigers() : List<UpcomingGigersApiModel>
}