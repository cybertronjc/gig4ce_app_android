package com.gigforce.core.datamodels

data class AccessLogResponse(val status:Boolean ?= false,val _id : String?="",val siplyResponseStatus :Boolean? = false, val responseURL : String?=null) {

}