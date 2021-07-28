package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.giger_app.service.APPRenderingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface IMainNavDataRepository {
    fun reload()
    fun getData(): LiveData<List<FeatureItemCard2DVM>>
}

class MainNavDataRepository @Inject constructor(private val buildConfig: IBuildConfig) :
    IMainNavDataRepository {
    private val appRenderingService = RetrofitFactory.createService(APPRenderingService::class.java)
    private var data: MutableLiveData<List<FeatureItemCard2DVM>> = MutableLiveData()
    private var needToReflect = true

    init {
        reload()
    }

    override fun reload() {

        FirebaseFirestore.getInstance().collection("AppConfigs")
            .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid)
            .addSnapshotListener { value, error ->
                value?.documents?.let {
                    if (it.isNotEmpty() && needToReflect) {
                        val list = it[0].data?.get("data") as? List<Map<String, Any>>
                        list?.let {
                            val mainNavData = ArrayList<FeatureItemCard2DVM>()
                            for (item in list) {
//                                var jsonObject = Gson().toJsonTree(list.get(0)).asJsonObject
//                                var dataModelObject = GsonBuilder().create().fromJson(
//                                    jsonObject.toString(),
//                                    FeatureItemCard2DVM::class.java
//                                )
//                                mainNavData.add(dataModelObject)
                                val title = item.get("title") as? String ?: "-"
                                val index = (item.get("index") as? Long) ?: 500
                                val icon_type = item.get("icon") as? String
                                val navPath = item.get("navPath") as? String
                                mainNavData.add(
                                    FeatureItemCard2DVM(
                                        title = title,
                                        icon = icon_type,
                                        navPath = navPath,
                                        index = index.toInt()
                                    )
                                )
                            }
                            mainNavData.sortBy { it.index }
                            data.value = mainNavData
                            receivedNotifyToServer()
                            needToReflect = false
                        }
                    } else {
                        needToReflect = true
                        receivedNotifyToServer()
                    }
                }
            }
    }

    private fun receivedNotifyToServer() {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            notifyToServer()
        }
    }


    suspend fun notifyToServer() {
        try {
            var jsonData = JsonObject()
            jsonData.addProperty("userId", FirebaseAuth.getInstance().currentUser?.uid!!)
            appRenderingService.notifyToServer(buildConfig.getApiBaseURL(), jsonData)
        } catch (e: Exception) {

        }
    }

    override fun getData(): LiveData<List<FeatureItemCard2DVM>> {
        return data
    }

}