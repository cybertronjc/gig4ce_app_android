package com.gigforce.app.eventbridge

import android.util.Log
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject

class EventBridgeRepo(private val event_bridge_url: String) {
    private val eventBridgeService: EventBridgeService =
        RetrofitFactory.createService(EventBridgeService::class.java)

    suspend fun setEventToEventBridge(eventName: String, props: Map<String, Any>?) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            val eventBridgeModel = EventBridgeModel(activityType = eventName, gigerId = user.uid)
            try {
//                eventBridgeService.setStatus(event_bridge_url,eventBridgeModel)
                var propData = HashMap<String, Any>()
                props?.let {
                    for(data in it.entries){
                        propData.put(data.key,data.value)
                    }
                }
                propData.put("gigerId", user.uid)
                propData.put("activityType", eventName)

                var jsonObject = Gson().toJsonTree(propData).asJsonObject
                eventBridgeService.setMapStatus(event_bridge_url, jsonObject)
            } catch (e: Exception) {
                Log.e("errorfound",e.toString())
            }

        }

    }
}