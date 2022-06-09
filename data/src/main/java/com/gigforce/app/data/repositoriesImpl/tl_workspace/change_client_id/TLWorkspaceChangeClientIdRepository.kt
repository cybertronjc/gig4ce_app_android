package com.gigforce.app.data.repositoriesImpl.tl_workspace.change_client_id

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import com.gigforce.app.data.remote.bodyOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkspaceChangeClientIdRepository @Inject constructor(
    private val changeClientIdService: ChangeClientIdService,
    private val loggedInUser : FirebaseAuthStateListener
) {

    suspend fun changeClientId(
        newClientId: String,
        gigerId: String,
        gigerMobile: String,
        gigerName: String,
        jobProfileId: String,
        jobProfileName: String,
        businessId: String
    ) {
        val request = ChangeClientIdRequest(
            data = listOf(
                DataItem(
                    clientId = newClientId,
                    jobProfile = jobProfileName,
                    gigerName = gigerName,
                    gigerMobile = gigerMobile,
                    gigerId = gigerId
                )
            ),
            clientIdUpdateLog = ClientIdUpdateLog(
                updatedBy = "android_app_tl_workspace_change_client_id",
                source = loggedInUser.getCurrentSignInInfo()?.uid
            ), filters = Filters(
                businessId = businessId,
                jobProfileId = jobProfileId
            )
        )

        changeClientIdService.changeClientId(
            request
        ).bodyFromBaseResponseElseThrow()
    }
}