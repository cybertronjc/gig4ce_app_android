package com.gigforce.app.modules.client_activation

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.gigforce.app.core.base.basefirestore.BaseFirestoreDBRepository
import com.gigforce.app.modules.client_activation.models.DrivingCertificate
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.setOrThrow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UploadDrivingCertificateRepository : BaseFirestoreDBRepository() {
    override fun getCollectionName(): String {
        return "JP_Applications"
    }



}