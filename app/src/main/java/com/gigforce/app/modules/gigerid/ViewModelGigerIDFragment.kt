package com.gigforce.app.modules.gigerid

import android.view.View
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.PermissionUtils
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageReference

class ViewModelGigerIDFragment(private val gigerIDCallbacks: GigerIDCallbacks) : ViewModel(),
    GigerIDCallbacks.ResponseCallbacks {
    private val _observableProfilePic: SingleLiveEvent<StorageReference> by lazy {
        SingleLiveEvent<StorageReference>();
    }
    val observableProfilePic: SingleLiveEvent<StorageReference> get() = _observableProfilePic

    private val _observablePermGranted: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observablePermGranted: SingleLiveEvent<Boolean> get() = _observablePermGranted

    private val _observablePermResultsNotGranted: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observablePermResultsNotGranted: SingleLiveEvent<Boolean> get() = _observablePermResultsNotGranted

    private val _observableUserProfileDataSuccess: SingleLiveEvent<ProfileData> by lazy {
        SingleLiveEvent<ProfileData>();
    }
    val observableUserProfileDataSuccess: SingleLiveEvent<ProfileData> get() = _observableUserProfileDataSuccess

    private val _observableUserProfileDataFailure: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableUserProfileDataFailure: SingleLiveEvent<String> get() = _observableUserProfileDataFailure


    private val _observableProgress: SingleLiveEvent<Int> by lazy {
        SingleLiveEvent<Int>();
    }
    val observableProgress: SingleLiveEvent<Int> get() = _observableProgress

    fun getProfileData() {
        gigerIDCallbacks.getProfileData(this)
    }


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

    fun getProfilePicture(avatarName: String) {
        gigerIDCallbacks.getProfilePicture(avatarName, this)
    }

    fun onActivityResultCalled(requestCode: Int) {
        if (requestCode == PermissionUtils.reqCodePerm) {
            observablePermResultsNotGranted.value = true
        }
    }

    fun showProgress(show: Boolean) {
        observableProgress.value = if (show) View.VISIBLE else View.GONE
    }

    override fun getProfileSuccess(
        querySnapshot: DocumentSnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {
            observableUserProfileDataFailure.value = error.message
        } else {
            observableUserProfileDataSuccess.value =
                querySnapshot?.toObject(ProfileData::class.java)
        }

    }

    override fun getProfilePic(reference: StorageReference) {
        observableProfilePic.value = reference
    }

}