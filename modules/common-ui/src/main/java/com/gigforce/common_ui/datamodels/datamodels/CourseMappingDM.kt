package com.gigforce.common_ui.datamodels.datamodels

data class CourseMappingDM(
    var companyId : String = "",
    var courseId : String = "",
    var isopened : Boolean = false,
    var rolesRequired : Boolean = false,
    var roles : List<String> = emptyList(),
    var userIdsRequired : Boolean = false,
    var userUids : List<String> = emptyList()
)