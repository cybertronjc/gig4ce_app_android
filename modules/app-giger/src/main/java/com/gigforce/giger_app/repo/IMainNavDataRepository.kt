package com.gigforce.giger_app.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gigforce.common_ui.viewdatamodels.FeatureItemCard2DVM
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.giger_app.service.APPRenderingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

interface IMainNavDataRepository {
    fun reload()
    fun getData(): LiveData<List<FeatureItemCard2DVM>>
    fun getDefaultData(): List<FeatureItemCard2DVM>
}

class MainNavDataRepository @Inject constructor(
    private val buildConfig: IBuildConfig,
    private val sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
) :
    IMainNavDataRepository {
    private val appRenderingService = RetrofitFactory.createService(APPRenderingService::class.java)
    private var data: MutableLiveData<List<FeatureItemCard2DVM>> = MutableLiveData()
    private var reloadCount = 0

    init {
        reload()
    }

    override fun reload() {

        FirebaseFirestore.getInstance().collection("AppConfigs")
            .whereEqualTo("uid", FirebaseAuth.getInstance().currentUser?.uid)
            .addSnapshotListener { value, error ->
                value?.documents?.let {
                    if (it.isNotEmpty() && reloadCount < 2) {
                        var docData = it[0].data as? Map<String, Any>
                        docData?.let { docMapData ->
                            var versionCodeList = ArrayList<Int>()
                            docMapData.forEach { mapEntry ->
                                mapEntry.key.toIntOrNull()?.let {
                                    versionCodeList.add(it)
                                }
                            }
                            var sortedVersionCodeList = versionCodeList.sortedDescending()
                            var foundVersionMapping = false
                            sortedVersionCodeList.forEach { dbVersionCode ->
                                if (sharedPreAndCommonUtilInterface.getCurrentVersionCode() >= dbVersionCode) {
                                    docMapData.get(dbVersionCode.toString())?.let { iconList ->
                                        arrangeDataAndSetObserver(iconList)
                                    }
                                    foundVersionMapping = true
                                    return@let
                                }
                            }
                            if (!foundVersionMapping) {
                                docMapData.get("data")?.let { iconList ->
                                    arrangeDataAndSetObserver(iconList)
                                }
                            }
                        }

                    } else {
                        reloadCount = 1
                        receivedNotifyToServer()
                    }
                }
            }
    }

    private fun arrangeDataAndSetObserver(iconList: Any) {
        val list = iconList as? List<Map<String, Any>>
        list?.let {
            val mainNavData = ArrayList<FeatureItemCard2DVM>()
            for (item in list) {
                val title = item.get("title") as? String ?: "-"
                val index = (item.get("index") as? Long) ?: 500
                val icon_type = item.get("icon") as? String
                val navPath = item.get("navPath") as? String
                val active = item.get("active") as? Boolean
                mainNavData.add(
                    FeatureItemCard2DVM(
                        active = active,
                        title = title,
                        icon = icon_type,
                        navPath = navPath,
                        index = index.toInt()
                    )
                )

            }
            val mainNavData = appConfigList.filter { it.active && (it.type == null || it.type == "") }.filter { it.active == true }
            mainNavData.sortBy { it.index }
//            var tempMainNavData = mainNavData.dup
            mainNavData.clear()
//            mainNavData.addAll(tempMainNavData)
            data.value = mainNavData
            receivedNotifyToServer()
            reloadCount++
        }
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
                sharedPreAndCommonUtilInterface.getCurrentVersionCode()
            )
            appRenderingService.notifyToServer(buildConfig.getApiBaseURL(), jsonData)
        } catch (e: Exception) {

        }
    }

    override fun getData(): LiveData<List<FeatureItemCard2DVM>> {
        return data
    }

    override fun getDefaultData(): List<FeatureItemCard2DVM> {
        val mainNavData = ArrayList<FeatureItemCard2DVM>()
        mainNavData.add(
            FeatureItemCard2DVM(
                title = "Chat",
                icon = "chat",
                navPath = "chats/chatList",
                index = 150
            )
        )
        mainNavData.add(
            FeatureItemCard2DVM(
                title = "My Gig",
                icon = "mygig",
                navPath = "gig/mygig",
                index = 100
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = "Setting",
                icon = "setting",
                navPath = "setting",
                index = 140
            )
        )
        mainNavData.add(
            FeatureItemCard2DVM(
                title = "Learning",
                icon = "learning",
                navPath = "learning/main",
                index = 130
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = "Profile",
                icon = "profile",
                navPath = "profile",
                index = 120
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = "Wallet",
                icon = "wallet",
                navPath = "payslipMonthlyFragment",
                index = 110
            )
        )

        mainNavData.add(
            FeatureItemCard2DVM(
                title = "Verification",
                icon = "shield",
                navPath = "verification/main",
                index = 160
            )
        )
        mainNavData.add(
            FeatureItemCard2DVM(
                title = "Invoices",
                icon = "wallet",
                navPath = "wallet/invoicesList",
                index = 170
            )
        )
        return mainNavData
    }

}

private fun arrangeDataAndSetObserver(iconList: Any) {
    val list = iconList as? List<Map<String, Any>>
    list?.let {
        val appConfigList = arrayListOf<FeatureItemCard2DVM>()

        for (item in list) {
            try {
                var appConfig = Gson().fromJson(JSONObject(item).toString(), FeatureItemCard2DVM::class.java)
                appConfigList.add(appConfig)
            }catch (e: Exception){
                FirebaseCrashlytics.getInstance().log("IMainNavDataRepo : $e")
            }
        }
        // defaultViewType is currently 0
        val mainNavData = appConfigList.filter { it.active && (it.type == null || it.type == "") }
        mainNavData.sortedBy { it.index }
        data.value = mainNavData
        receivedNotifyToServer()
        reloadCount++
    }
}