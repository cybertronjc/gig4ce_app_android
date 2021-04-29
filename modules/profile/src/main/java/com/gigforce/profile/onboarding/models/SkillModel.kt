package com.gigforce.profile.onboarding.models

import com.gigforce.core.fb.BaseFirestoreDataModel

data class SkillModel(
        var id: String = "",
        var skillDetail :SkillDetailModel?=null
): BaseFirestoreDataModel(tableName = "skills"){
}

data class SkillDetailModel(var hasExperience: Boolean,var experiencedIn : ArrayList<String>){

}

