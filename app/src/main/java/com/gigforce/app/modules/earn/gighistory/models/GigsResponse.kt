package com.gigforce.app.modules.earn.gighistory.models

import com.gigforce.app.core.base.models.BaseResponse
import com.gigforce.app.modules.gigPage.models.Gig


data class GigsResponse(
    override val status: Boolean = false, override val message: String = "",
    override var data: List<Gig>? = ArrayList()
) : BaseResponse<List<Gig>?>()