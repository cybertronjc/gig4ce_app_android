package com.gigforce.core

interface IEventTracker {

    // set guid for the user
    fun setUserId(userId: String)

    // set user properties
    fun setUserProperty(propName: String, args:UserPropArgs)

    // push events
    fun pushEvent(args:TrackingEventArgs)
}

data class TrackingEventArgs(
        val eventName:String,
        val props: Map<String, Any>
){}

data class UserPropArgs(
        val userId:String
){}