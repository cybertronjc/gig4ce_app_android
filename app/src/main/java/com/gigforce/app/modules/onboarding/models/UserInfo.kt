package com.gigforce.app.modules.onboarding.models

data class UserInfo(
    var id:String = "",
    var name: String = "",
    var dob: String = "",
    var gender: String = "",
    var qualification: String = "",
    var yoq: String = "",
    var profilePic: String = "",
    var isStudent: Boolean = false,
     //if studentorworker is true or student
    var partime: Boolean = false,
     //if studentorworker is false or worker
    var company: String = "",
    var role: String = "",
    var yoe: String = "",
     //for both student and worker:
    var hoursofwork: String = "",
    var daysofwork: String = ""
){
}