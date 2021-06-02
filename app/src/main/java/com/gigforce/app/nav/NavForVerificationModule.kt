package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl

class NavForVerificationModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName = "verification"
        baseImplementation.registerRoute("${moduleName}", R.id.verification)

        baseImplementation.registerRoute("${moduleName}/main", R.id.gigerVerificationFragment)
        baseImplementation.registerRoute("${moduleName}/DLCA", R.id.fragment_upload_dl_cl_act)
        baseImplementation.registerRoute("${moduleName}/AADHAR", R.id.uploadAadharCard)
        baseImplementation.registerRoute("${moduleName}/PAN", R.id.uploadPanCard)
        baseImplementation.registerRoute("${moduleName}/uploadDropDown", R.id.uploadDropDown)
        baseImplementation.registerRoute("${moduleName}/aadhaarUpload", R.id.aadhaarUpload)
        baseImplementation.registerRoute("${moduleName}/bankUpload2", R.id.bankUpload2)
        baseImplementation.registerRoute("${moduleName}/panUpload", R.id.panUpload)
        baseImplementation.registerRoute("${moduleName}/verificationDone", R.id.verificationDone)

        baseImplementation.registerRoute("${moduleName}/addDrivingLicenseInfoFragment", R.id.addDrivingLicenseInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addBankDetailsInfoFragment", R.id.addBankDetailsInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addSelfieVideoFragment", R.id.addSelfieVideoFragment)
        baseImplementation.registerRoute("${moduleName}/addPanCardInfoFragment", R.id.addPanCardInfoFragment)
        baseImplementation.registerRoute("${moduleName}/addAadharCardInfoFragment", R.id.addAadharCardInfoFragment)







    }
}