package com.gigforce.app.data.repositoriesImpl.gigs

import com.gigforce.app.domain.repositories.gigs.UpcomingGigsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

class UpcomingGigsRepostioryImpl : UpcomingGigsRepository {

    private val _upcomingGigsState = MutableSharedFlow<String>()
    val upcomingGigsState = _upcomingGigsState.asSharedFlow()

    override fun getUpcomingGigs(): Flow<String> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshUpcomingGigs() {
    }
}