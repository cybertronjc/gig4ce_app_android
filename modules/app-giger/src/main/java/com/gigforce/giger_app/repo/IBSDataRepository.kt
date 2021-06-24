package com.gigforce.giger_app.repo

import javax.inject.Inject

interface IBSDataRepository  : ICommonConfigCardsFBRepo

class BSDataRepository @Inject constructor() : CommonConfigCardsFBRepo(),IBSDataRepository {
    val collectionName = "AppConfigs_BS"
    init {
        loadData(collectionName)
    }

}