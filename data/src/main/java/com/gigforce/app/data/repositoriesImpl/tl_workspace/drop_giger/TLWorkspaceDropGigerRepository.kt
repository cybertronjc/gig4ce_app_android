package com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger

import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceDropGigerRepository @Inject constructor() {

    suspend fun getDropOptions() : List<DropOption>{
        return listOf(
            DropOption(
                dropLocalizedText = "ded",
                reasonId = "ded",
                customReason = false
            ),
            DropOption(
                dropLocalizedText = "ddd",
                reasonId = "dddd",
                customReason = false
            )
        )
    }

    suspend fun dropGiger(
        gigerId : String,
        jobProfileId : String,
        reasonId : String,
        reasonText : String,
        lastWorkingDate : LocalDate
    ) {

    }
}