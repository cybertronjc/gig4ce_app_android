package com.gigforce.app.eventbridge

import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.retrofit.RetrofitFactory
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class EventBridgeRepo(private val iBuildConfigVM: IBuildConfigVM) {
    private val eventBridgeService : EventBridgeService =  RetrofitFactory.createService(EventBridgeService::class.java)
    suspend fun setEventToEventBridge( eventName : String,  props : Map<String,Any>?){
        FirebaseAuth.getInstance().currentUser?.let {
            val eventBridgeModel = EventBridgeModel(activityType = eventName,gigerId = it.uid)
            try {
                eventBridgeService.setStatus(iBuildConfigVM.getUserRegisterInfoUrl(),eventBridgeModel)
            }catch (e : Exception){

            }

        }

    }
}