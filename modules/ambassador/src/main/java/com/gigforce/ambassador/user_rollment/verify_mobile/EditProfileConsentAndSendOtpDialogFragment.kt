package com.gigforce.ambassador.user_rollment.verify_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.ambassador.AmbassadorEnrollViewModel
import com.gigforce.ambassador.R
import com.gigforce.ambassador.SendOtpResponseData
import com.gigforce.core.datamodels.ambassador.EnrolledUser
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_sending_otp_for_ambassador_edit.*


@AndroidEntryPoint
class EditProfileConsentAndSendOtpDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "UserDetailsFilledDialogFragment"
        private const val INTENT_EXTRA_ENROLLED_USER = "enrolled_user"

        fun launch(
            enrolledUser: EnrolledUser,
            fragmentManager: FragmentManager,
            okayClickListener: UserDetailsFilledDialogFragmentResultListener
        ) {
            val frag = EditProfileConsentAndSendOtpDialogFragment()
            frag.arguments = bundleOf(
                INTENT_EXTRA_ENROLLED_USER to enrolledUser
            )
            frag.mOkayResultListener = okayClickListener
            frag.show(fragmentManager, TAG)
        }

    }

    private val viewModel: AmbassadorEnrollViewModel by viewModels()

    private lateinit var enrolledUser: EnrolledUser
    private lateinit var mOkayResultListener: UserDetailsFilledDialogFragmentResultListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sending_otp_for_ambassador_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            enrolledUser = it.getParcelable(INTENT_EXTRA_ENROLLED_USER) ?: return@let
        }

        arguments?.let {
            enrolledUser = it.getParcelable(INTENT_EXTRA_ENROLLED_USER) ?: return@let
        }
        initView()
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INTENT_EXTRA_ENROLLED_USER, enrolledUser)
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

        congrats_text.text = getString(R.string.verification_code_sent_to_mobile_amb)
        submitBtn.setOnClickListener {
            viewModel.getMobileNumberAndSendOtpInfo(enrolledUser)
        }

        cancel_btn.setOnClickListener {
            dismiss()
        }
    }

    private fun initViewModel() {
        viewModel
            .sendOtpToPhoneNumber
            .observe(viewLifecycleOwner, Observer {
                it ?: return@Observer

                when (it) {
                    Lce.Loading -> {
                        send_otp_layout.invisible()
                        loading_progresbar.visible()
                    }
                    is Lce.Content -> {

                        Toast.makeText(requireContext(), getString(R.string.otp_sent_amb), Toast.LENGTH_SHORT).show()
                        mOkayResultListener.onOtpSent(it.content)
                        dismiss()
                    }
                    is Lce.Error -> {
                        loading_progresbar.invisible()
                        send_otp_layout.visible()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.error_amb))
                            .setMessage(it.error)
                            .setPositiveButton(getString(R.string.okay_amb)) { _, _ -> }
                            .show()
                    }
                }
            })
    }


}
interface UserDetailsFilledDialogFragmentResultListener {

    fun onOtpSent(sendOtpResponseData: SendOtpResponseData)
}