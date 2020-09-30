package com.gigforce.app.modules.learning.models

data class CourseMapping(
    var companyId : String = "",
    var courseId : String = "",
    var roles_required : Boolean = false,
    var roles : List<String> = emptyList(),
    var userIdsRequired : Boolean = false,
    var userUids : List<String> = emptyList()
    )