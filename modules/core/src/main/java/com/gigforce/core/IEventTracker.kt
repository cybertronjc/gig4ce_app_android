package com.gigforce.core

interface IEventTracker {

    // set guid for the user
    fun setUserId(userId: String)

    // set user properties
    fun setUserProperty(props: Map<String, Any>)

    fun removeUserProperty(prop:String)

    // push events
    fun pushEvent(args:TrackingEventArgs)

    //set user profile properties
    fun setProfileProperty(args: ProfilePropArgs)

    fun setUpAnalyticsTools()
    fun logoutUserFromAnalytics()
    fun setUserName(name:String)
}

data class TrackingEventArgs(
    val eventName:String,
    val props: Map<String, Any>?
){}

data class UserPropArgs(
   val userId:String
){}

data class ProfilePropArgs(
    val propertyName: String,
    val propertyValue: Any?
)