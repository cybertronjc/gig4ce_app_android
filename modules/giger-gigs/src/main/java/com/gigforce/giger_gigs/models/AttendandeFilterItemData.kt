package com.gigforce.giger_gigs.models

data class AttendanceFilterItemShift(
        val shift : String,
        val shiftTimeForView : String
){

    override fun toString(): String {
        return shiftTimeForView
    }
}