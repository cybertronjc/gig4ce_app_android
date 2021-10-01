package com.gigforce.common_ui.viewdatamodels

import com.gigforce.common_ui.viewconfigs.AppModuleLevelViewTypes
import com.gigforce.common_ui.viewdatamodels.HindiTranslationMapping
import com.gigforce.core.SimpleDVM

class PendingJoiningItemDVM(
    val jobProfileId: String = "",
    val jobProfileName: String = "",
    val location: String = "",
    val expectedStartDate: String = ""
) : SimpleDVM(AppModuleLevelViewTypes.VIEW_PENDING_JOINING_SECTION)