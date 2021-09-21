package com.gigforce.core.di.repo

interface IProfileFirestoreRepository : IBaseFirestoreRepository {
    fun createEmptyProfile(
        latitude: Double ,
        longitude: Double ,
        locationAddress: String
    )
    fun createEmptyProfile()
}