package com.gigforce.giger_gigs.travelling_info

class ResponseObjectModel<T>{
    var status: Boolean?=false
    var message: String?=""
    var data: T?=null
}