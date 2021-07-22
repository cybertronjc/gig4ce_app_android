package com.gigforce.common_ui.deviceInfo_permission

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

data class DeviceInfoAndPermissionInfo(
        val device: String,
        val model: String,
        val osVersionInt: Int,
        val osVersion: String,
        val permissionsOnApp: List<String>,
        val permissionsGrantedByUser: List<String>,
        val updatedOn: Timestamp,
        val currentVersionUserIsUsing: String
)

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

        val permissions = getListedAndGrantedPermissions(appContext)
        firebaseFirestore.collection(
                "Device_Version_info"
        ).document(
                currentUser.uid
        ).set(
                DeviceInfoAndPermissionInfo(
                        device = device,
                        model = model,
                        osVersionInt = osVersionInt,
                        osVersion = osVersion,
                        permissionsOnApp = permissions.first,
                        permissionsGrantedByUser = permissions.second,
                        updatedOn = Timestamp.now(),
                        currentVersionUserIsUsing = currentAppVersion
                )
        )
    }

    private fun getListedAndGrantedPermissions(
            appContext: Context
    ): Pair<List<String>, List<String>> {
        val packageInfo = appContext.packageManager.getPackageInfo(
                appContext.packageName,
                PackageManager.GET_PERMISSIONS
        )
        val permissionListedInManifest = packageInfo.requestedPermissions
        val grantedPermissions = mutableListOf<String>()

        permissionListedInManifest.forEach {

            if (ContextCompat.checkSelfPermission(
                            appContext,
                            it
                    ) == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(it)
            }
        }

        return permissionListedInManifest.toList() to grantedPermissions
    }

}