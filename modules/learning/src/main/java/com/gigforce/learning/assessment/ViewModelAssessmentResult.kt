package com.gigforce.learning.assessment

import android.content.Intent
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.utils.PermissionUtils


class ViewModelAssessmentResult : ViewModel() {
    internal val observableQuestionWiseSumList: MutableLiveData<List<Boolean>> by lazy {
        MutableLiveData<List<Boolean>>();
    }
    internal val observableIsUserPassed: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>();
    }
    internal val observablePermResultsGranted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>();
    }
    internal val observablePermResultsNotGranted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>();
    }
    internal val observablePermAlReadyGranted: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>();
    }



    fun checkIfUserPassed(passed: Boolean?) {
        observableIsUserPassed.value = if (passed!!) View.GONE else View.VISIBLE
    }

    fun checkIfPermGranted(
        requestCode: Int,
        grantResults: IntArray?
    ) {
        if (requestCode == PermissionUtils.reqCodePerm && PermissionUtils.permissionsGrantedCheck(
                grantResults!!
            )
        ) {
            observablePermResultsGranted.value = true
        } else {
            observablePermResultsNotGranted.value = true
        }

    }

    fun checkForPermissionsAndInitSharing(checkForPermission: Boolean) {
        if (checkForPermission) {
            observablePermAlReadyGranted.value = true
        }

    }

    fun onActivityResultCalled(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PermissionUtils.reqCodePerm) {
            observablePermResultsNotGranted.value = true
        }
    }

}