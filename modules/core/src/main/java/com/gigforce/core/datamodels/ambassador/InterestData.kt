package com.gigforce.core.datamodels.ambassador

import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.datamodels.profile.Skill2

data class InterestData(
    val interest : List<Skill2>,
    val profileData : ProfileData?
)