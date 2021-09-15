package com.gigforce.core.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gigforce.core.R

/**
 * @author Rohit
 * This class consists all permission related methods
 */
object PermissionUtils {
    var reqCodePerm = 1109

    /**
     * method to check whether we need to show the permission dialog or not
     *
     * @return boolen whether to need to or not
     */
    private fun useRunTimePermissions(): Boolean {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1
    }

    /**
     * method to check whether the user has the permissions or not
     *
     * @param activity   activity from where the method will be invoked
     * @param permission permission need to be checked
     * @return boolean whether the user has the permission or not
     */
    fun hasPermission(activity: Activity, permission: String?): Boolean {
        return if (useRunTimePermissions()) {
            activity.checkSelfPermission(permission!!) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    /**
     * replica of the above method with variable parameters
     *
     * @param activity   activity from the method need to be called
     * @param permission permissions to be checked
     * @return boolean whether all the permissions are been granted or not
     */
    fun hasPermissions(activity: Activity, vararg permission: String?): Boolean {
        var allPermissionsGranted = true
        if (useRunTimePermissions()) {
            for (element in permission) {
                if (activity.checkSelfPermission(element!!) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
        }
        return allPermissionsGranted
    }

    /**
     * method for requesting array of permissions
     *
     * @param activity    activity from where the method need to be called
     * @param permission  permission to asked
     * @param requestCode request code of the permission
     */
    fun requestPermissions(
        activity: Activity,
        permission: Array<out String?>,
        requestCode: Int
    ) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permission!!, requestCode)

        }
    }

    fun requestPermissionsFragment(
        fragment: Fragment,
        permission: Array<out String?>,
        requestCode: Int
    ) {
        if (useRunTimePermissions()) {
            fragment.requestPermissions(permission, requestCode)

        }
    }

    /**
     * @param activity
     * @param permission
     * @return
     */
    fun shouldShowRational(activity: Activity, permission: String?): Boolean {
        return if (useRunTimePermissions()) {
            activity.shouldShowRequestPermissionRationale(permission!!)
        } else false
    }

    /**
     * method to check if the user has denied the permissions as if the
     * permission is important for the core features we can show an explanation here
     *
     * @param activity
     * @param permissions
     * @return
     */
    fun shouldShowRationals(
        activity: Activity,
        vararg permissions: String?
    ): Boolean {
        var allPermissions = false
        if (useRunTimePermissions()) {
            for (permission in permissions) {
                if (activity.shouldShowRequestPermissionRationale(permission!!)) {
                    allPermissions = true
                    break
                }
                //                allPermissions =  activity.shouldShowRequestPermissionRationale(permission);
            }
        }
        return allPermissions
    }

    fun shouldAskForPermission(activity: Activity, permission: String?): Boolean {
        return if (useRunTimePermissions()) {
            !hasPermission(
                activity,
                permission
            ) &&
                    (!hasAskedForPermission(
                        activity,
                        permission
                    ) ||
                            shouldShowRational(
                                activity,
                                permission
                            ))
        } else false
    }

    /**
     * method to redirect the user to the settings
     *
     * @param activity activity from where the method need to be invoked
     */
    fun goToAppSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent,
            reqCodePerm
        )
    }

    /**
     * this method tells whether the permission is been asked or not
     *
     * @param activity   activity from where the method will be invoked
     * @param permission permission to check
     * @return
     */
    fun hasAskedForPermission(activity: Activity?, permission: String?): Boolean {
        return PreferenceManager
            .getDefaultSharedPreferences(activity)
            .getBoolean(permission, false)
    }

    /**
     * This method marks the permission as asked if a permission is been asked
     *
     * @param activity   activity from which the method will be invoked
     * @param permission permission which need to be marked
     */
    fun markedPermissionAsAsked(activity: Activity?, permission: String?) {
        PreferenceManager
            .getDefaultSharedPreferences(activity)
            .edit()
            .putBoolean(permission, true)
            .apply()
    }

    /**
     * method which checks the permissions and displays permission dialog if it is required
     *
     * @param context     context of the activity where permission need to be checked
     * @param reqCode     reqcode by which the permissions result will be checked in
     * onRequestPermissionResult() method of activity
     * @param permissions variable parameter to handle many permissions together
     * @return if the given permissions are granted true else false
     */
    fun checkForPermission(
        context: Activity,
        reqCode: Int,
        vararg permissions: String?
    ): Boolean {
        reqCodePerm = reqCode
        if (hasPermissions(
                context,
                *permissions
            )
        ) {
            return true
        } else {
            if (shouldShowRationals(
                    context,
                    *permissions
                )
            ) {
                // Display UI and wait for user interaction
                requestPermissions(
                    context, permissions,
                    reqCode
                )
                for (permission in permissions) markedPermissionAsAsked(
                    context,
                    permission
                )
            } else {
                var allPermissions = true
                for (permission in permissions) {
                    if (hasAskedForPermission(
                            context,
                            permission
                        )
                    ) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.provide_permission_core),
                            Toast.LENGTH_SHORT
                        ).show()
                        goToAppSettings(
                            context
                        )
                        allPermissions = false
                        break
                    }
                }
                if (allPermissions) {
                    requestPermissions(
                        context, permissions,
                        reqCode
                    )
                }
            }
        }
        return false
    }

    fun checkForPermissionFragment(
        context: Fragment,
        reqCode: Int,
        vararg permissions: String?
    ): Boolean {
        reqCodePerm = reqCode
        if (hasPermissions(
                context.requireActivity(),
                *permissions
            )
        ) {
            return true
        } else {
            if (shouldShowRationals(
                    context.requireActivity(),
                    *permissions
                )
            ) {
                // Display UI and wait for user interaction
                requestPermissionsFragment(
                    context, permissions,
                    reqCode
                )
                for (permission in permissions) markedPermissionAsAsked(
                    context.requireActivity(),
                    permission
                )
            } else {
                var allPermissions = true
                for (permission in permissions) {
                    if (hasAskedForPermission(
                            context.requireActivity(),
                            permission
                        )
                    ) {
                        Toast.makeText(
                            context.requireContext(),
                            context.getString(R.string.provide_permission_core),
                            Toast.LENGTH_SHORT
                        ).show()
                        goToAppSettings(
                            context.requireActivity()
                        )
                        allPermissions = false
                        break
                    }
                }
                if (allPermissions) {
                    requestPermissionsFragment(
                        context, permissions,
                        reqCode
                    )
                }
            }
        }
        return false
    }

    /**
     * method to check whether all the permissions in the grant results are granted or not
     *
     * @param permissions int array of all the permissions
     * @return true if all the permissions are granted false if not
     */
    fun permissionsGrantedCheck(permissions: IntArray): Boolean {
        if (permissions.isEmpty()) return false
        var allPermissionsGranted = true
        for (permission in permissions) {
            if (permission != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false
                break
            }
        }
        return allPermissionsGranted
    }
}