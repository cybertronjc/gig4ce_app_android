package com.gigforce.app.modules.gigerid

import android.view.View
import androidx.lifecycle.ViewModel
import com.gigforce.app.utils.PermissionUtils
import com.gigforce.app.utils.SingleLiveEvent

class ViewModelGigerIDFragment : ViewModel() {

    private val _observablePermGranted: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observablePermGranted: SingleLiveEvent<Boolean> get() = _observablePermGranted

    private val _observablePermResultsNotGranted: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observablePermResultsNotGranted: SingleLiveEvent<Boolean> get() = _observablePermResultsNotGranted

    private val _observableProgress: SingleLiveEvent<Int> by lazy {
        SingleLiveEvent<Int>();
    }
    val observableProgress: SingleLiveEvent<Int> get() = _observableProgress


    fun checkForPermissionsAndInitSharing(checkForPermission: Boolean) {
        if (checkForPermission) {
            observablePermGranted.value = true
        }

    }

    fun checkIfPermGranted(
        requestCode: Int,
        grantResults: IntArray?
    ) {
        if (requestCode == PermissionUtils.reqCodePerm && PermissionUtils.permissionsGrantedCheck(
                grantResults!!
            )
        ) {
            observablePermGranted.value = true
        } else {
            observablePermResultsNotGranted.value = true
        }

    }

    fun onActivityResultCalled(requestCode: Int) {
        if (requestCode == PermissionUtils.reqCodePerm) {
            observablePermResultsNotGranted.value = true
        }
    }

    fun showProgress(show: Boolean) {
        observableProgress.value = if (show) View.VISIBLE else View.GONE
    }

}