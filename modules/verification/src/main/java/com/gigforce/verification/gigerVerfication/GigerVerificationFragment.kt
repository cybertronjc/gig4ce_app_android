package com.gigforce.verification.gigerVerfication

import android.Manifest
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.GigerVerificationStatus
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.PermissionUtils
import com.gigforce.common_ui.viewmodels.GigVerificationViewModel
import com.gigforce.verification.R
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_giger_verification.*
import kotlinx.android.synthetic.main.fragment_giger_verification_item.view.*
import kotlinx.android.synthetic.main.fragment_giger_verification_main.*
import kotlinx.android.synthetic.main.fragment_giger_verification_main.view.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class GigerVerificationFragment : Fragment(), IOnBackPressedOverride {

    private val viewModel: GigVerificationViewModel by viewModels()
    private var gigerVerificationStatus: GigerVerificationStatus? = null
    private var showActionButtons: Boolean = false
    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_giger_verification, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForBundleData(savedInstanceState)
        initView()
        setListeners()
        initViewModel()
        checkForContract()
    }

    private fun checkForBundleData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            showActionButtons = it.getBoolean(StringConstants.SHOW_ACTION_BUTTONS.value)

        }

        arguments?.let {
            showActionButtons = it.getBoolean(StringConstants.SHOW_ACTION_BUTTONS.value)

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.SHOW_ACTION_BUTTONS.value, showActionButtons)

    }

    private fun checkForContract() {
        viewModel.gigerContractStatus.observe(viewLifecycleOwner, Observer { url ->
            run {
                ll_contracts.visible()
                if (url != null) {
                    tv_contract_status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_check,
                        0,
                        0,
                        0
                    )
                    tv_contract_status.setTextColor(resources.getColor(R.color.green_dc3ab105))
                    tv_contract_status.setBackgroundResource(R.drawable.bg_capsule_53ba25)
                    tv_contract_status.text = getString(R.string.signed_veri)
                    iv_download_giger_verification.visible()
                    val layoutParams: RelativeLayout.LayoutParams =
                        tv_contract_status.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.addRule(RelativeLayout.START_OF, iv_download_giger_verification.id)
                    layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END)

                    tv_contract_status.layoutParams = layoutParams

                    PushDownAnim.setPushDownAnimTo(iv_download_giger_verification)
                        .setOnClickListener(View.OnClickListener {
                            if (PermissionUtils.checkForPermissionFragment(
                                    this,
                                    PermissionUtils.reqCodePerm,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            ) {
                                downloadCertificate(url)
                            }
                        })
                    PushDownAnim.setPushDownAnimTo(tv_contract_status)
                        .setOnClickListener(View.OnClickListener {
                            if (PermissionUtils.checkForPermissionFragment(
                                    this,
                                    PermissionUtils.reqCodePerm,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            ) {
//                                val docIntent = Intent(
//                                    requireContext(),
//                                    DocViewerActivity::class.java
//                                )
//                                docIntent.putExtra(
//                                    StringConstants.DOC_URL.value,
//                                    url
//                                )
//                                startActivity(docIntent)
                                navigation.navigateToDocViewerActivity(requireActivity(),url)
                            }


                        })


                } else {
                    tv_contract_status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_time_fa6400,
                        0,
                        0,
                        0
                    )
                    tv_contract_status.setTextColor(resources.getColor(R.color.fa6400))
                    tv_contract_status.setBackgroundResource(R.drawable.bg_capsule_border_fa6400)
                    tv_contract_status.text = getString(R.string.unsigned_veri)
                    iv_download_giger_verification.setOnClickListener(null)
                    tv_contract_status.setOnClickListener(null)
                    iv_download_giger_verification.gone()
                    val layoutParams: RelativeLayout.LayoutParams =
                        tv_contract_status.layoutParams as RelativeLayout.LayoutParams
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, tv_contract_status.id)
                    tv_contract_status.layoutParams = layoutParams
                }
            }

        })
        viewModel.checkForSignedContract()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtils.reqCodePerm && PermissionUtils.permissionsGrantedCheck(
                grantResults
            )
        ) {
            tv_contract_status.performClick()
        } else {
            showToast(getString(R.string.perm_not_granted_veri))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionUtils.reqCodePerm) {
            PermissionUtils.checkForPermissionFragment(
                this,
                PermissionUtils.reqCodePerm,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun initView() {
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(resources, R.color.lipstick_2,null))
        toolbar.apply {
            hideActionMenu()
            showTitle(getString(R.string.giger_verification_veri))
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })

        }

        appBarComp.apply {
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }

//        appBarComp.apply {
//            setAppBarTitle(getString(R.string.giger_verification))
//            setBackButtonListener(View.OnClickListener {
//                activity?.onBackPressed()
//            })
//        }


        Glide.with(requireContext())
            .load(R.drawable.ic_video_round)
            .into(selfieVideoLayout.optionIconIV)

        selfieVideoLayout.optionTitleTV.text = getString(R.string.selfie_video_veri)
        selfieVideoLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)


        Glide.with(requireContext())
            .load(R.drawable.ic_dl)
            .into(panLayout.optionIconIV)

        panLayout.optionTitleTV.text = getString(R.string.pan_card_veri)
        panLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)

        Glide.with(requireContext())
            .load(R.drawable.ic_bank)
            .into(aadharLayout.optionIconIV)

        aadharLayout.optionTitleTV.text = getString(R.string.aadhar_card_veri)
        aadharLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)


        Glide.with(requireContext())
            .load(R.drawable.ic_dl)
            .into(drivingLayout.optionIconIV)

        drivingLayout.optionTitleTV.text = getString(R.string.driving_license_veri)
        drivingLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)


        Glide.with(requireContext())
            .load(R.drawable.ic_bank)
            .into(bankDetailsLayout.optionIconIV)

        bankDetailsLayout.optionTitleTV.text = getString(R.string.bank_details_veri)
        bankDetailsLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)
        if (showActionButtons) {
            tv_action_giger_verification.visible()

            PushDownAnim.setPushDownAnimTo(

                tv_action_giger_verification
            ).setOnClickListener(
                View.OnClickListener {
                    var navFragmentsData = activity as NavFragmentsData
                    navFragmentsData.setData(
                        bundleOf(
                            StringConstants.NAV_TO_QUESTIONNARE.value to true,
                            StringConstants.MOVE_TO_NEXT_STEP.value to true

                        )
                    )
                    navigation.popBackStack()
                })
        }


    }

    override fun onBackPressed(): Boolean {
        var navFragmentsData = activity as NavFragmentsData
        navFragmentsData.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true
            )
        )
        return false

    }


    private fun setListeners() {
        verificationMainLayout.panLayout.setOnClickListener {
            navigation.navigateTo("verification/addPanCardInfoFragment")

        }

        verificationMainLayout.drivingLayout.setOnClickListener {
            navigation.navigateTo("verification/addDrivingLicenseInfoFragment")
        }

        verificationMainLayout.aadharLayout.setOnClickListener {
            navigation.navigateTo("verification/addAadharCardInfoFragment")
        }

        verificationMainLayout.bankDetailsLayout.setOnClickListener {
            navigation.navigateTo("verification/addBankDetailsInfoFragment")
        }

        verificationMainLayout.selfieVideoLayout.setOnClickListener {
            navigation.navigateTo("verification/addSelfieVideoFragment")
        }
    }

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {
                verificationMainLayout.visible()
                this.gigerVerificationStatus = it

                if (it.selfieVideoUploaded) {
                    selfieVideoLayout.descTitleTV.text = getString(R.string.uploaded_veri)
                    selfieVideoLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.green,
                            null
                        )
                    )
                } else {
                    selfieVideoLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)
                    selfieVideoLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.battle_ship_grey,
                            null
                        )
                    )
                }
                val userHasPan = it.panCardDataModel?.userHasPanCard ?: false
                if (userHasPan) {
                    panLayout.descTitleTV.text = it.panCardDataModel!!.verifiedString

                    panLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(it.panCardDataModel!!.state),
                            null
                        )
                    )
                } else {
                    panLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)
                    panLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.battle_ship_grey,
                            null
                        )
                    )
                }


                val userhasPassBook = it.bankUploadDetailsDataModel?.userHasPassBook ?: false
                if (userhasPassBook) {
                    bankDetailsLayout.descTitleTV.text =
                        it.bankUploadDetailsDataModel!!.verifiedString
                    bankDetailsLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(
                                it.bankUploadDetailsDataModel!!.state
                            ),
                            null
                        )
                    )
                } else {
                    bankDetailsLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)
                    bankDetailsLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.battle_ship_grey,
                            null
                        )
                    )
                }


                val userHasAadharCard = it.aadharCardDataModel?.userHasAadharCard ?: false
                if (userHasAadharCard) {
                    aadharLayout.descTitleTV.text = it.aadharCardDataModel!!.verifiedString
                    aadharLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(it.aadharCardDataModel!!.state),
                            null
                        )
                    )
                } else {
                    aadharLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)
                    aadharLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.battle_ship_grey,
                            null
                        )
                    )
                }


                val userHasDL = it.drivingLicenseDataModel?.userHasDL ?: false
                if (userHasDL) {
                    drivingLayout.descTitleTV.text = it.drivingLicenseDataModel!!.verifiedString
                    drivingLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(it.drivingLicenseDataModel!!.state),
                            null
                        )
                    )
                } else {
                    drivingLayout.descTitleTV.text = getString(R.string.tap_to_upload_veri)
                    drivingLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.battle_ship_grey,
                            null
                        )
                    )
                }
            })

        viewModel.startListeningForGigerVerificationStatusChanges()
    }

    private fun downloadCertificate(url: String) {
        if (File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "GigForceContract.pdf"
            ).exists()
        ) {
            showToast(getString(R.string.download_contracts_exists_veri))
            return
        }

//        val url = URLEncoder.encode(downloadUrl, "UTF-8")
        val downloadmanager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setTitle(getString(R.string.contract_veri))
        request.setDescription(getString(R.string.downloading_contract_veri))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setVisibleInDownloadsUi(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOCUMENTS,
                "GigForceContract.pdf"
            )
        } else {
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "GigForceContract.pdf"
            )
        }
        if (downloadmanager != null) {
            try {
                Toast.makeText(
                    context,
                    getString(R.string.contract_is_downloading_veri),
                    Toast.LENGTH_SHORT
                ).show()
                downloadmanager.enqueue(request)
            } catch (e: Exception) {
                Toast.makeText(context, getString(R.string.network_error_retry_veri), Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(context, getString(R.string.network_error_retry_veri), Toast.LENGTH_SHORT).show()
        }
        val progressBarDialog = ProgressDialog(context)
        progressBarDialog.setTitle(getString(R.string.downloading_contract_wait_veri))
        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressBarDialog.setButton(
            DialogInterface.BUTTON_POSITIVE, "OK"
        ) { dialog: DialogInterface?, whichButton: Int ->

        }
        progressBarDialog.progress = 0
        Thread {
            var downloading = true
            while (downloading) {
                val q = DownloadManager.Query()
                q.setFilterById() //filter by id which you have receieved when reqesting download from download manager
                var cursor: Cursor? = null
                if (downloadmanager != null) {
                    cursor = downloadmanager.query(q)
                }
                cursor?.moveToFirst()
                var bytes_downloaded = 0
                if (cursor != null) {
                    bytes_downloaded = cursor.getInt(
                        cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    )
                }
                var bytes_total = 0
                if (cursor != null) {
                    bytes_total =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                }
                if (cursor != null && cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val dl_progress = (bytes_downloaded * 100L / bytes_total).toInt()
                requireActivity().runOnUiThread { progressBarDialog.progress = dl_progress }
                cursor!!.close()
            }
        }.start()
        progressBarDialog.show()
    }
}