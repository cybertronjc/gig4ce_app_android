package com.gigforce.common_ui.viewdatamodels

import android.os.Bundle
import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

class AssessmentCardDVM(
        var title: String = "",
        var completed: Boolean = false,
        var videoLengthString: String = "00:00",
        var args: Bundle? = null,
        var navPath: String? = null
) : SimpleDVM(CommonViewTypes.VIEW_ASSESMENT_ITEM_CARD)