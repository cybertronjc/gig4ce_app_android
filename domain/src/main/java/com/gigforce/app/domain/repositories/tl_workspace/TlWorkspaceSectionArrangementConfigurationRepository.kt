package com.gigforce.app.domain.repositories.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection


/**
 * Returns TL- Workspace screen items to be shown
 * and in what order
 */
interface TlWorkspaceSectionArrangementConfigurationRepository {

    suspend fun getSectionsListAndArrangementInfo() : List<TLWorkSpaceSection>
}