package com.gigforce.giger_app.repo

import android.content.Context
import android.system.Os
import android.util.Log
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.common_ui.viewdatamodels.HindiTranslationMapping
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.giger_app.R
import com.gigforce.giger_app.service.APPRenderingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

interface IMainNavDataRepository {
    fun getData(): Flow<List<FeatureItemCard2DVM>>
    fun getDefaultData(context: Context): List<FeatureItemCard2DVM>
}

class MainNavDataRepository @Inject constructor(
    private val buildConfig: IBuildConfig
) : IMainNavDataRepository {
    private val appRenderingService = RetrofitFactory.createService(APPRenderingService::class.java)
    private var reloadCount = 0
    private val currentVersion = 174


    private fun arrangeDataAndSetObserver(iconList: Any): ArrayList<FeatureItemCard2DVM> {
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
                    Log.e("flowtest",e.toString())

                }

            }
            val tempMainNavData =
                mainNavData.filter { it.active == true }
                    .filter { (it.type != "folder" && it.type != "sub_folder") || !it.subicons.isNullOrEmpty() } as ArrayList<FeatureItemCard2DVM>
            tempMainNavData.sortBy { it.index }
            mainNavData.clear()
            mainNavData.addAll(tempMainNavData)
            Log.e("flowtest",mainNavData.size.toString())
            receivedNotifyToServer()
            reloadCount++
            return mainNavData
        }
        return mainNavData
    }

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
                currentVersion
            )
            appRenderingService.notifyToServer(buildConfig.getApiBaseURL(), jsonData)
        } catch (e: Exception) {

        }
    }
    var producerScope : ProducerScope<ArrayList<FeatureItemCard2DVM>>?=null
    override fun getData(): Flow<List<FeatureItemCard2DVM>> {

        return callbackFlow {
            producerScope = this
            FirebaseFirestore.getInstance().collection("AppConfigs")
                .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid)
                .addSnapshotListener { data, error ->
                    if (error != null) {
                        producerScope?.close(error)
                    } else {
                        data?.documents?.let {
                            if (it.isNotEmpty() && reloadCount < 2) {

                                val docData = it[0].data as? Map<String, Any>
                                docData?.let { docMapData ->
                                    val versionCodeList = ArrayList<Int>()
                                    docMapData.forEach { mapEntry ->
                                        mapEntry.key.toIntOrNull()?.let {
                                            versionCodeList.add(it)
                                        }
                                    }
                                    val sortedVersionCodeList =
                                        versionCodeList.sortedDescending()
                                    var foundVersionMapping = false
                                    sortedVersionCodeList.forEach { dbVersionCode ->
                                        if (currentVersion >= dbVersionCode) {
                                            var arrangedData = ArrayList<FeatureItemCard2DVM>()
                                            docMapData.get(dbVersionCode.toString())
                                                ?.let { iconList ->
                                                    arrangedData = arrangeDataAndSetObserver(iconList)
                                                }
                                            foundVersionMapping = true
                                            Log.e("flowtest", "$foundVersionMapping $currentVersion $dbVersionCode")
                                            sendBlocking(arrangedData)
                                            return@let
//                                            producerScope?.sendBlocking(arrangedData)
                                        }
                                    }
                                    if (!foundVersionMapping) {
                                        var arrangedData = ArrayList<FeatureItemCard2DVM>()
                                        docMapData.get("data")?.let { iconList ->
                                            arrangedData = arrangeDataAndSetObserver(iconList)
                                        }
                                        Log.e("flowtest"," data ")
                                        sendBlocking(arrangedData)
//                                        producerScope?.sendBlocking(arrangedData)
                                    }
                                }

                            } else {
                                reloadCount = 1
                                receivedNotifyToServer()
                            }
                        }
                    }
                }
            awaitClose{ }
        }

    }

    override fun getDefaultData(context: Context): List<FeatureItemCard2DVM> {
        val mainNavData = ArrayList<FeatureItemCard2DVM>()
        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.chat_app_giger),
                icon = "chat",
                navPath = "chats/chatList",
                index = 150
            )
        )
        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.my_gig_app_giger),
                icon = "mygig",
                navPath = "gig/mygig",
                index = 100
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.settings_app_giger),
                icon = "setting",
                navPath = "setting",
                index = 140
            )
        )
        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.learning_app_giger),
                icon = "learning",
                navPath = "learning/main",
                index = 130
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.profile_app_giger),
                icon = "profile",
                navPath = "profile",
                index = 120
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.wallet_app_giger),
                icon = "wallet",
                navPath = "payslipMonthlyFragment",
                index = 110
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.verification_app_giger),
                icon = "shield",
                navPath = "verification/main",
                index = 160
            )
        )
        mainNavData.add(
            FeatureItemCard2DVM(
                title = context.resources.getString(R.string.invoices_app_giger),
                icon = "wallet",
                navPath = "wallet/invoicesList",
                index = 170
            )
        )
        return mainNavData
    }

}