package com.gigforce.common_ui.navigation.signature

import androidx.core.os.bundleOf
import com.gigforce.common_ui.CommonIntentExtras
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject

class SignatureNavigation @Inject constructor(
    private val navigation: INavigation
) {

    companion object {
        const val DESTINATION_CAPTURE_SIGNATURE = "signatureImageCaptureDialogFragment"
    }

    fun openCaptureSignatureFragment(
        userId: String? = null
    ) {
        navigation.navigateTo(
            DESTINATION_CAPTURE_SIGNATURE,
            bundleOf(CommonIntentExtras.INTENT_USER_ID to userId),
            NavigationOptions.getNavOptions()
        )
    }
}