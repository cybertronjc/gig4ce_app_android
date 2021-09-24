package com.gigforce.giger_app.repo

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

interface IFeatureIconsDataRepository {
    fun reload()
    fun getData(): LiveData<List<FeatureItemCard2DVM>>
}

class FeatureIconsDataRepository @Inject constructor(
    private val buildConfig: IBuildConfig,
    @ApplicationContext val context: Context
) :
    IFeatureIconsDataRepository {
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
                                if (getCurrentVersionCode() >= dbVersionCode) {
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
                try {
                    val title = item.get("title") as? String ?: "-"
                    val index = (item.get("index") as? Long) ?: 500
                    val icon_type = item.get("icon") as? String
                    val navPath = item.get("navPath") as? String
                    val active = item.get("active") as? Boolean ?: true
                    val type = item.get("type") as? String ?: ""
                    val subicons = item.get("subicons") as? List<Long> ?: null
                    var hi : HindiTranslationMapping? = null
                    item.get("hi")?.let {
                        try {
                            hi = Gson().fromJson(JSONObject(it as? Map<*, *>).toString(),HindiTranslationMapping::class.java)
                        }catch (e: Exception){}
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
                mainNavData.filter { it.active == true } as ArrayList<FeatureItemCard2DVM>
            tempMainNavData.sortBy { it.index }
            mainNavData.clear()
            mainNavData.addAll(tempMainNavData)
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
                getCurrentVersionCode()
            )
            appRenderingService.notifyToServer(buildConfig.getApiBaseURL(), jsonData)
        } catch (e: Exception) {

        }
    }

    override fun getData(): LiveData<List<FeatureItemCard2DVM>> {
        return data
    }

    fun getCurrentVersionCode(): Int {
        try {
            val pInfo: PackageInfo =
                context.packageManager.getPackageInfo(
                    context.getPackageName(),
                    0
                )

            return pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

}