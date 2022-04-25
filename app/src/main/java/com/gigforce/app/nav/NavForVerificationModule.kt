package com.gigforce.app.nav

import com.gigforce.app.R
import com.gigforce.common_ui.BaseNavigationImpl
import com.gigforce.common_ui.navigation.JoiningVerificationNavDestinations

class NavForVerificationModule(
    baseImplementation: BaseNavigationImpl
){
    init {
        val moduleName = "verification"
        baseImplementation.registerRoute("${moduleName}/main", R.id.myDocumentsFragment)
//        baseImplementation.registerRoute("${moduleName}/DLCA", R.id.fragment_upload_dl_cl_act) // need to check if require

        //verification new
        baseImplementation.registerRoute("${moduleName}/aadhaarOptionsFragment", R.id.aadhaarOptionsFragment)
        baseImplementation.registerRoute("${moduleName}/aadhaarcardimageupload",R.id.aadhaarcardimageupload)
        baseImplementation.registerRoute("${moduleName}/aadhaarcardphonenumber",R.id.aadhaarcardphonenumber)
        baseImplementation.registerRoute("${moduleName}/bank_account_fragment",R.id.bank_account_fragment)
        baseImplementation.registerRoute("${moduleName}/pancardimageupload",R.id.panCardFragment)
        baseImplementation.registerRoute("${moduleName}/drivinglicenseimageupload",R.id.drivingLicenseFragment)
        baseImplementation.registerRoute("${moduleName}/AadharDetailInfoFragment",R.id.adharDetailInfoFragment)
        baseImplementation.registerRoute("${moduleName}/bankdetailconfirmationbottomsheet", R.id.confirmBNBankBS)
        baseImplementation.registerRoute("${moduleName}/acknowledgeBankBS",R.id.acknowledgeBankBS)
        baseImplementation.registerRoute("${moduleName}/AskUserForVaccineBS",R.id.AskUserForVaccineBS)
        baseImplementation.registerRoute("${moduleName}/chooseYourVaccineFragment",R.id.chooseYourVaccineFragment)
        baseImplementation.registerRoute("${moduleName}/GetVaccinateFirstBS",R.id.GetVaccinateFirstBS)
        baseImplementation.registerRoute("${moduleName}/CovidVaccinationCertificateFragment",R.id.CovidVaccinationCertificateFragment)
        baseImplementation.registerRoute("${moduleName}/CovidCertificateStatusFragment",R.id.CovidCertificateStatusFragment)
        baseImplementation.registerRoute("${moduleName}/VaccineMainFragment",R.id.VaccineMainFragment)
        baseImplementation.registerRoute("${moduleName}/SizeWarningBottomSheet",R.id.SizeWarningBottomSheet)
        baseImplementation.registerRoute("${moduleName}/VaccineUploadSuccessfulBS",R.id.VaccineUploadSuccessfulBS)
        baseImplementation.registerRoute("${moduleName}/CertificateDownloadBS",R.id.CertificateDownloadBS)
        baseImplementation.registerRoute("${moduleName}/InvalidFormatBottomSheet",R.id.InvalidFormatBottomSheet)
        baseImplementation.registerRoute("${moduleName}/characterCertificate",R.id.characterCertificateFragment)

        //new main verification with compliance
        baseImplementation.registerRoute("${moduleName}/myDocuments", R.id.myDocumentsFragment)
        baseImplementation.registerRoute("${moduleName}/complianceFragment", R.id.complianceDocsFragment)
        baseImplementation.registerRoute("${moduleName}/AadharConfirmationBS", R.id.AadharConfirmationBS)


    }
}