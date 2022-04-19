package com.gigforce.app.domain.repositories.gigs

import kotlinx.coroutines.flow.Flow

interface UpcomingGigsRepository {

    fun getUpcomingGigs() : Flow<String>

    suspend fun refreshUpcomingGigs()
}