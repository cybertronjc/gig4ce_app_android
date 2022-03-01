package com.gigforce.app.background.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gigforce.app.BuildConfig
import com.gigforce.app.eventbridge.EventBridgeRepo

class AttendanceWorker(context: Context,params: WorkerParameters) : CoroutineWorker(context,params){
    var eventBridgeRepo = EventBridgeRepo(BuildConfig.EVENT_BRIDGE_URL)
    override suspend fun doWork(): Result {
        var eventName = ""
        val mapData = HashMap<String,Any>()
        inputData.keyValueMap.forEach{
            if(it.key == "event_key"){
                eventName = it.value.toString()
            }else {
                mapData.put(it.key, it.value)
            }
        }
        eventBridgeRepo.setEventToEventBridge(eventName,mapData)
        return Result.success()
    }
}