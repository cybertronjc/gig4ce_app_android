package com.gigforce.common_ui.deviceInfo_permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.gigforce.core.extensions.setOrThrow
import com.gigforce.core.extensions.setOrUpdateOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

object DeviceInfoGatherer {

    private val firebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val currentUser: FirebaseUser by lazy {
        FirebaseAuthStateListener.getInstance().getCurrentSignInUserInfoOrThrow()
    }

    fun updateDeviceInfoAndPermissionGranted(
        appContext: Context
    ) {

        val device = Build.DEVICE
        val model = Build.MODEL
        val osVersionInt = Build.VERSION.SDK_INT
        val osVersion = Build.VERSION.RELEASE
        val currentAppVersion = try {
            val pInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0);
            pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }

        val finalMap = mutableMapOf(
            "device" to device,
            "model" to model,
            "osVersionInt" to osVersionInt,
            "osVersion" to osVersion,
            "updatedOn" to Timestamp.now(),
            "currentVersionUserIsUsing" to currentAppVersion
        ) + getListedAndGrantedPermissions(appContext)

        firebaseFirestore.collection(
            "Device_Version_info"
        ).document(
            currentUser.uid
        ).set(finalMap)
    }

    private fun getListedAndGrantedPermissions(
        appContext: Context
    ): Map<String, Any> {
        val packageInfo = appContext.packageManager.getPackageInfo(
            appContext.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val permissionListedInManifest = packageInfo.requestedPermissions
        val permissions = mutableMapOf<String,Boolean>()

        permissionListedInManifest.forEach {
            if(it.isNullOrBlank()) return@forEach

            val permissionGranted = ContextCompat.checkSelfPermission(
                appContext,
                it
            ) == PackageManager.PERMISSION_GRANTED

            permissions["permissions.${formatPermissionName(it)}"] = permissionGranted
        }

        return permissions
    }

    private fun formatPermissionName(
        it: String
    ): String = if(it.contains('.')){
        it.substringAfterLast('.')
    } else{
        it
    }
}