package com.gigforce.client_activation.client_activation.models

data class CourseMapping(
    var companyId : String = "",
    var courseId : String = "",
    var isopened : Boolean = false,
    var rolesRequired : Boolean = false,
    var roles : List<String> = emptyList(),
    var userIdsRequired : Boolean = false,
    var userUids : List<String> = emptyList()
    )