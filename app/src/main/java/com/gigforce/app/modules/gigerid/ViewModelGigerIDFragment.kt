package com.gigforce.app.modules.gigerid

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage.models.GigAndGigOrder
import com.gigforce.app.modules.gigerid.models.URLQrCode
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.PermissionUtils
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

class ViewModelGigerIDFragment(private val gigerIDCallbacks: GigerIDCallbacks) : ViewModel(),
    GigerIDCallbacks.ResponseCallbacks {
    private val _observableGigDetails: SingleLiveEvent<GigAndGigOrder> by lazy {
        SingleLiveEvent<GigAndGigOrder>();
    }
    val observableGigDetails: SingleLiveEvent<GigAndGigOrder> get() = _observableGigDetails
    private val _observableURLS: SingleLiveEvent<URLQrCode> by lazy {
        SingleLiveEvent<URLQrCode>();
    }
    val observableURLS: SingleLiveEvent<URLQrCode> get() = _observableURLS
    private val _observableProfilePic: SingleLiveEvent<StorageReference> by lazy {
        SingleLiveEvent<StorageReference>();
    }
    val observableProfilePic: SingleLiveEvent<StorageReference> get() = _observableProfilePic

    private val _observablePermGranted: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observablePermGranted: SingleLiveEvent<String> get() = _observablePermGranted

    private val _observablePermResultsNotGranted: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observablePermResultsNotGranted: SingleLiveEvent<Boolean> get() = _observablePermResultsNotGranted

    private val _observableUserProfileDataSuccess: SingleLiveEvent<ProfileData> by lazy {
        SingleLiveEvent<ProfileData>();
    }
    val observableUserProfileDataSuccess: SingleLiveEvent<ProfileData> get() = _observableUserProfileDataSuccess

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError


    private val _observableProgress: SingleLiveEvent<Int> by lazy {
        SingleLiveEvent<Int>();
    }
    val observableProgress: SingleLiveEvent<Int> get() = _observableProgress

    private var fileNameToShare: String = ""

    fun getProfileData() {
        gigerIDCallbacks.getProfileData(this)
    }


    fun checkForPermissionsAndInitSharing(checkForPermission: Boolean) {
        if (checkForPermission) {
            observablePermGranted.value = fileNameToShare
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
            observablePermGranted.value = fileNameToShare
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
            observableError.value = error.message
        } else {
            val profileData = querySnapshot?.toObject(ProfileData::class.java)
            profileData?.id=querySnapshot?.id
            observableUserProfileDataSuccess.value = profileData
            fileNameToShare += profileData?.name + "_"
        }

    }

    override fun getProfilePic(reference: StorageReference) {
        observableProfilePic.value = reference
    }

    override fun getGigDetailsResponse(
        querySnapshot: DocumentSnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {
            observableError.value = error.message
        } else {
            val gigDetails = querySnapshot?.toObject(Gig::class.java)
//            observableGigDetails.value = gigDetails
//            fileNameToShare += gigDetails?.gigId
        }
    }

    override fun getUrlResponse(
        querySnapshot: DocumentSnapshot?,
        error: FirebaseFirestoreException?
    ) {
        if (error != null) {
        } else {
            _observableURLS.value = querySnapshot?.toObject(URLQrCode::class.java)
        }
    }

    fun getURl() {
        gigerIDCallbacks.getURls(this)
    }

    fun getGigDetails(string: String?) = viewModelScope.launch{

       try {
           val gigAndGigOrder =  gigerIDCallbacks.getGigAndGigOrderDetails(string!!)
           observableGigDetails.value = gigAndGigOrder
           fileNameToShare += gigAndGigOrder.gig.gigId
       } catch (e: Exception) {
           observableError.value = e.message
       }
    }

}