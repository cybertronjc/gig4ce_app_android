package com.gigforce.common_ui.viewdatamodels.models.progress

data class CourseMapping(
    var companyId : String = "",
    var courseId : String = "",
    var isopened : Boolean = false,
    var rolesRequired : Boolean = false,
    var roles : List<String> = emptyList(),
    var userIdsRequired : Boolean = false,
    var userUids : List<String> = emptyList()
    )