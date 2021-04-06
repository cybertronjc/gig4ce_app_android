package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details_filled_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.common.SendSmsViewModel
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.utils.Lse
import kotlinx.android.synthetic.main.fragment_user_enrolled_dialog.*

interface UserDetailsFilledDialogFragmentResultListener {

    fun onOkayClicked()

    fun onReUploadDocumentsClicked()
}

class UserDetailsFilledDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "DeclineGigDialogFragment"

        fun launch(
            userId: String,
            userName: String,
            fragmentManager: FragmentManager,
            okayClickListener: UserDetailsFilledDialogFragmentResultListener
        ) {
            val frag = UserDetailsFilledDialogFragment()
            frag.arguments = bundleOf(
                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName
            )
            frag.mOkayResultListener = okayClickListener
            frag.show(fragmentManager, TAG)
        }

    }

    private val gigerVerificationViewModel: GigVerificationViewModel by viewModels()
    private val sendSmsViewModel: SendSmsViewModel by viewModels()

    private lateinit var userId: String
    private lateinit var userName: String

    private lateinit var mOkayResultListener: UserDetailsFilledDialogFragmentResultListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_enrolled_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }

        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
        }
        initView()
        initViewModel()
        getDocumentsUploadedByGigerDetails(userId)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
    }

    override fun isCancelable(): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    private fun initView() {

        congrats_text.text = buildSpannedString {
            append(getString(R.string.you_have) + " ")
            bold {
                append(getString(R.string.successfully) + " ")
            }
            append(getString(R.string.submitted))
            bold {
                append(" $userName's ")
            }
            append(
                getString(R.string.details_exclamation) + "\n" +
                        "\n"
            )
            append("Invite link will be shared via SMS on registered number.")
        }

        submitBtn.setOnClickListener {
            mOkayResultListener.onOkayClicked()
            dismiss()
        }

        will_do_later_btn.setOnClickListener {
            mOkayResultListener.onOkayClicked()
            dismiss()
        }

        upload_doc_btn.setOnClickListener {
            mOkayResultListener.onReUploadDocumentsClicked()
            dismiss()
        }
    }

    private fun initViewModel() {
        gigerVerificationViewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                if (it.aadharCardDataModel?.aadharCardNo != null ||
                    it.drivingLicenseDataModel?.frontImage != null ||
                    it.panCardDetails?.panCardImagePath != null
                ) {
                    sendSmsToEnrolledUser()
                } else {
                    showAtLeastOneDocumentNecessaryCard()
                }
            })

        sendSmsViewModel.sendSms
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lse.Loading -> {
                    }
                    Lse.Success -> {
                        showDocumentUploadedCard()
                    }
                    is Lse.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Unable to send invite link to user",
                            Toast.LENGTH_SHORT
                        ).show()
                        showDocumentUploadedCard()
                    }
                }
            })
    }

    private fun sendSmsToEnrolledUser() {
        sendSmsViewModel.sendSmsByUid(userId, "") //todo  :
    }

    private fun showAtLeastOneDocumentNecessaryCard() {

        loading_progresbar.gone()
        document_uploaded_layout.gone()
        upload_at_least_one_document_layout.visible()
    }

    private fun showDocumentUploadedCard() {
        loading_progresbar.gone()
        upload_at_least_one_document_layout.gone()
        document_uploaded_layout.visible()
    }

    private fun getDocumentsUploadedByGigerDetails(userId: String) {
        gigerVerificationViewModel.getVerificationStatus(userId)
    }
}