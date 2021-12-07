package com.gigforce.common_ui.navigation.signature

import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import javax.inject.Inject

class SignatureNavigation @Inject constructor(
    private val navigation: INavigation
) {

    companion object{

        const val DESTINATION_DRAW_SIGNATURE= "signatureDrawerDialogFragment"
        const val DESTINATION_CAPTURE_SIGNATURE = "signatureImageCaptureDialogFragment"
    }

    fun openDrawSignatureFragment() {
        navigation.navigateTo(
            DESTINATION_DRAW_SIGNATURE,
            null,
            NavigationOptions.getNavOptions()
        )
    }

    fun openCaptureSignatureFragment() {
        navigation.navigateTo(
            DESTINATION_CAPTURE_SIGNATURE,
            null,
            NavigationOptions.getNavOptions()
        )
    }
}