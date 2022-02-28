package com.gigforce.giger_app.repo

import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.common_ui.viewdatamodels.HindiTranslationMapping
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.giger_app.service.APPRenderingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

interface IFeatureIconsDataRepository {
    fun getDataBS(currentVersionCode: Int): Flow<List<FeatureItemCard2DVM>>
}

class FeatureIconsDataRepository @Inject constructor(
    private val buildConfig: IBuildConfig
) :
    IFeatureIconsDataRepository {
    private var currentVersionCode: Int = 0
    private val appRenderingService = RetrofitFactory.createService(APPRenderingService::class.java)
    private var reloadCount = 0


    private fun receivedNotifyToServer() {
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        scope.launch {
            notifyToServer()
        }
    }

    private suspend fun notifyToServer() {
        try {
            var jsonData = JsonObject()
            jsonData.addProperty("userId", FirebaseAuth.getInstance().currentUser?.uid!!)
            jsonData.addProperty(
                "versionCode",
                currentVersionCode
            )
            appRenderingService.notifyToServer(buildConfig.getApiBaseURL(), jsonData)
        } catch (e: Exception) {

        }
    }


    //------------------------------second type of views - for bottom sheet(pop up)----------
    private fun arrangeDataAndSetObserverBS(iconList: Any): ArrayList<FeatureItemCard2DVM> {
        val list = iconList as? List<Map<String, Any>>
        val mainNavData = ArrayList<FeatureItemCard2DVM>()
        list?.let {
            for (item in list) {
                try {
                    val title = item.get("title") as? String ?: "-"
                    val index = (item.get("index") as? Long) ?: 500
                    val icon_type = item.get("icon") as? String
                    val navPath = item.get("navPath") as? String
                    val active = item.get("active") as? Boolean ?: true
                    val type = item.get("type") as? String ?: ""
                    val subicons = item.get("subicons") as? List<Long> ?: null
                    var hi: HindiTranslationMapping? = null
                    item.get("hi")?.let {
                        try {
                            hi = Gson().fromJson(
                                JSONObject(it as? Map<*, *>).toString(),
                                HindiTranslationMapping::class.java
                            )
                        } catch (e: Exception) {
                        }
                    }
                    mainNavData.add(
                        FeatureItemCard2DVM(
                            active = active,
                            title = title,
                            icon = icon_type,
                            navPath = navPath,
                            index = index,
                            type = type,
                            subicons = subicons,
                            hi = hi
                        )
                    )
                } catch (e: Exception) {

                }

            }
            val tempMainNavData =
                mainNavData.filter { it.active == true }
                    .filter { it.type != "folder" && it.type != "icon" } as ArrayList<FeatureItemCard2DVM>
            tempMainNavData.sortBy { it.index }
            mainNavData.clear()
            mainNavData.addAll(tempMainNavData)
            receivedNotifyToServer()
            reloadCount++
        }
        return mainNavData
    }

    override fun getDataBS(currentVersionCode: Int): Flow<List<FeatureItemCard2DVM>> {
        this.currentVersionCode = currentVersionCode
        return callbackFlow {
            FirebaseFirestore.getInstance().collection("AppConfigs")
                .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid)
                .addSnapshotListener { value, error ->
                    value?.documents?.let {
                        if (it.isNotEmpty() && reloadCount < 2) {
                            val docData = it[0].data as? Map<String, Any>
                            docData?.let { docMapData ->
                                val versionCodeList = ArrayList<Int>()
                                docMapData.forEach { mapEntry ->
                                    mapEntry.key.toIntOrNull()?.let {
                                        versionCodeList.add(it)
                                    }
                                }
                                val sortedVersionCodeList = versionCodeList.sortedDescending()
                                var foundVersionMapping = false
                                sortedVersionCodeList.forEach { dbVersionCode ->
                                    if (currentVersionCode >= dbVersionCode) {
                                        var arrangedData = ArrayList<FeatureItemCard2DVM>()
                                        docMapData.get(dbVersionCode.toString())?.let { iconList ->
                                            arrangedData = arrangeDataAndSetObserverBS(iconList)
                                        }
                                        foundVersionMapping = true
                                        sendBlocking(arrangedData)
                                        return@let
                                    }
                                }
                                if (!foundVersionMapping) {
                                    var arrangedData = ArrayList<FeatureItemCard2DVM>()
                                    docMapData.get("data")?.let { iconList ->
                                        arrangedData = arrangeDataAndSetObserverBS(iconList)
                                    }
                                    sendBlocking(arrangedData)
                                }
                            }
                        } else {
                            reloadCount = 1
                            receivedNotifyToServer()
                        }
                    }
                }
            awaitClose{}
            }
        }
}