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
        mixpanel?.identify(userId);
        mixpanel?.getPeople()?.identify(userId)
        mixpanel?.track("User identified")
    }

    override fun setUserProperty(props: Map<String, Any>) {
        mixpanel?.registerSuperPropertiesMap(props)
    }

    override fun removeUserProperty(prop: String) {
        mixpanel?.unregisterSuperProperty(prop)
    }

    override fun pushEvent(args: TrackingEventArgs) {
        mixpanel?.trackMap(args.eventName,args.props)
    }
}