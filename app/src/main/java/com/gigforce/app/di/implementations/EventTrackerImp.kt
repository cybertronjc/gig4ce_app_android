package com.gigforce.app.di.implementations

import android.app.Activity
import android.content.Context
import com.gigforce.app.MainApplication
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.UserPropArgs
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class EventTrackerImp @Inject constructor(
    @ActivityContext val context: Context
) : IEventTracker{

    var mixpanel: MixpanelAPI? = ((context as Activity)?.application as MainApplication).mixpanel


    override fun setUserId(userId: String) {

    }

    override fun setUserProperty(propName: String, args: UserPropArgs) {

    }

    override fun pushEvent(args: TrackingEventArgs) {
        mixpanel?.trackMap(args.eventName,args.props)
    }
}