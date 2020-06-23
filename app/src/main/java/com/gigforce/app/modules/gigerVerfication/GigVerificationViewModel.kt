package com.gigforce.app.modules.gigerVerfication

import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.verification.VeriFirebaseRepository

open class GigVerificationViewModel constructor(
    private val verificationFirebaseRepository: VeriFirebaseRepository = VeriFirebaseRepository()
) : ViewModel()