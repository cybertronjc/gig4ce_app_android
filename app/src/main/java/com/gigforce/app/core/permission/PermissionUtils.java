package com.gigforce.app.core.permission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

/**
 * @author Rohit
 * This class consists all permission related methods
 */
public final class PermissionUtils {
    private static int reqCodePerm;

    /**
     * method to check whether we need to show the permission dialog or not
     *
     * @return boolen whether to need to or not
     */
    public static boolean useRunTimePermissions() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * method to check whether the user has the permissions or not
     *
     * @param activity   activity from where the method will be invoked
     * @param permission permission need to be checked
     * @return boolean whether the user has the permission or not
     */
    public static boolean hasPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * replica of the above method with variable parameters
     *
     * @param activity   activity from the method need to be called
     * @param permission permissions to be checked
     * @return boolean whether all the permissions are been granted or not
     */
    public static boolean hasPermissions(Activity activity, String... permission) {
        boolean allPermissionsGranted = true;
        if (useRunTimePermissions()) {
            for (int i = 0; i < permission.length; i++) {
                if (activity.checkSelfPermission(permission[i]) != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

        }
        return allPermissionsGranted;
    }

    /**
     * method for requesting array of permissions
     *
     * @param activity    activity from where the method need to be called
     * @param permission  permission to asked
     * @param requestCode request code of the permission
     */
    public static void requestPermissions(Activity activity, String[] permission, int requestCode) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permission, requestCode);
        }
    }

    public static void requestPermissionsFragment(Fragment activity, String[] permission, int requestCode) {
        if (useRunTimePermissions()) {
            activity.requestPermissions(permission, requestCode);
        }
    }


    /**
     * @param activity
     * @param permission
     * @return
     */
    public static boolean shouldShowRational(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return activity.shouldShowRequestPermissionRationale(permission);
        }
        return false;
    }

    /**
     * method to check if the user has denied the permissions as if the
     * permission is important for the core features we can show an explanation here
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static boolean shouldShowRationals(Activity activity, String... permissions) {
        boolean allPermissions = false;
        if (useRunTimePermissions()) {
            for (String permission : permissions) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    allPermissions = true;
                    break;
                }
//                allPermissions =  activity.shouldShowRequestPermissionRationale(permission);
            }

        }
        return allPermissions;
    }

    public static boolean shouldAskForPermission(Activity activity, String permission) {
        if (useRunTimePermissions()) {
            return !hasPermission(activity, permission) &&
                    (!hasAskedForPermission(activity, permission) ||
                            shouldShowRational(activity, permission));
        }
        return false;
    }

    /**
     * method to redirect the user to the settings
     *
     * @param activity activity from where the method need to be invoked
     */
    public static void goToAppSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivityForResult(intent, reqCodePerm);
    }

    /**
     * this method tells whether the permission is been asked or not
     *
     * @param activity   activity from where the method will be invoked
     * @param permission permission to check
     * @return
     */

    public static boolean hasAskedForPermission(Activity activity, String permission) {
        return PreferenceManager
                .getDefaultSharedPreferences(activity)
                .getBoolean(permission, false);
    }

    /**
     * This method marks the permission as asked if a permission is been asked
     *
     * @param activity   activity from which the method will be invoked
     * @param permission permission which need to be marked
     */
    public static void markedPermissionAsAsked(Activity activity, String permission) {
        PreferenceManager
                .getDefaultSharedPreferences(activity)
                .edit()
                .putBoolean(permission, true)
                .apply();
    }

    /**
     * method which checks the permissions and displays permission dialog if it is required
     *
     * @param context     context of the activity where permission need to be checked
     * @param reqCode     reqcode by which the permissions result will be checked in
     *                    onRequestPermissionResult() method of activity
     * @param permissions variable parameter to handle many permissions together
     * @return if the given permissions are granted true else false
     */
    public static boolean checkForPermission(Activity context, int reqCode, String... permissions) {
        reqCodePerm = reqCode;
        if (PermissionUtils.hasPermissions(context, permissions)) {
            return true;
        } else {
            if (PermissionUtils.shouldShowRationals(context,
                    permissions)) {
                // Display UI and wait for user interaction
                PermissionUtils.requestPermissions(
                        context, permissions,
                        reqCode);
                for (String permission : permissions)
                    PermissionUtils.markedPermissionAsAsked(context, permission);
            } else {
                boolean allPermissions = true;
                for (String permission : permissions) {
                    if (PermissionUtils.hasAskedForPermission(context, permission)) {
                        Toast.makeText(context, "you need to provide permissions to proceed further", Toast.LENGTH_SHORT).show();
                        PermissionUtils.goToAppSettings(context);
                        allPermissions = false;
                        break;
                    }
                }
                if (allPermissions) {
                    PermissionUtils.requestPermissions(
                            context, permissions,
                            reqCode);
                }


            }

        }
        return false;
    }

    public static boolean checkForPermissionFragment(Fragment fragment, Activity context, int reqCode, String... permissions) {
        reqCodePerm = reqCode;
        if (PermissionUtils.hasPermissions(context, permissions)) {
            return true;
        } else {
            if (PermissionUtils.shouldShowRationals(context,
                    permissions)) {
                // Display UI and wait for user interaction
                requestPermissionsFragment(
                        fragment, permissions,
                        reqCode);
                for (String permission : permissions)
                    PermissionUtils.markedPermissionAsAsked(context, permission);
            } else {
                boolean allPermissions = true;
                for (String permission : permissions) {
                    if (PermissionUtils.hasAskedForPermission(context, permission)) {
                        Toast.makeText(context, "you need to provide permissions to proceed further", Toast.LENGTH_SHORT).show();
                        PermissionUtils.goToAppSettings(context);
                        allPermissions = false;
                        break;
                    }
                }
                if (allPermissions) {
                    PermissionUtils.requestPermissionsFragment(
                            fragment, permissions,
                            reqCode);
                }


            }

        }
        return false;
    }

    /**
     * method to check whether all the permissions in the grant results are granted or not
     *
     * @param permissions int array of all the permissions
     * @return true if all the permissions are granted false if not
     */
    public static boolean permissionsGrantedCheck(int[] permissions) {
        if (permissions.length < 1)
            return false;
        boolean allPermissionsGranted = true;
        for (int permission : permissions) {
            if (permission != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }

        }
        return allPermissionsGranted;
    }
}