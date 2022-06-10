package com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceDropGigerRepository @Inject constructor(
    private val dropGigerService: DropGigerService
) {

    suspend fun getDropOptions(): List<DropOptionApiModel> {
        return dropGigerService
            .getDropReasons()
            .bodyFromBaseResponseElseThrow()
    }

    suspend fun dropGiger(
        gigerId: String,
        jobProfileId: String,
        reasonId: String,
        reasonText: String,
        customReason: Boolean,
        lastWorkingDate: LocalDate
    ) {

        val formattedDate = DateTimeFormatter.ISO_DATE.format(lastWorkingDate)  + " 00:00:00"
        dropGigerService.dropGiger(
            DropGigerRequest(
                reason = if (customReason) reasonText else reasonId,
                gigerId = gigerId,
                lastWorkingDate = formattedDate,
                jobProfileId = jobProfileId
            )
        ).bodyFromBaseResponseElseThrow()
    }
}