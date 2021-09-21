package com.gigforce.giger_app.repo

import javax.inject.Inject

interface IHomeCardsFBRepository : ICommonConfigCardsFBRepo

class HomeCardsFBRepository @Inject constructor() : CommonConfigCardsFBRepo(),
    IHomeCardsFBRepository {
    val collectionName = "AppConfigs_Home"

    init {
        loadData(collectionName)
    }


}