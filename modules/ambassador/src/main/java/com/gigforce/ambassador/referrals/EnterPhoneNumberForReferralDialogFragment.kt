package com.gigforce.ambassador.referrals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.gigforce.ambassador.R
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.common_ui.viewmodels.common.SendSmsViewModel
import com.gigforce.core.extensions.invisible
import com.gigforce.core.utils.Lse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_invite_direct_message.*
import kotlinx.android.synthetic.main.fragment_invite_direct_message_main.*


class EnterPhoneNumberForReferralDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val INTENT_EXTRA_INVITE_LINK = "invite_link"
        const val TAG = "EnterPhoneNumberForReferralDialogFragment"

        fun launch(
            inviteLink: String,
            inviteLinkSentListener: EnterPhoneNumberForReferralDialogFragmentEventListener,
            fragmentManager: FragmentManager
        ) {
            val frag = EnterPhoneNumberForReferralDialogFragment()
            frag.arguments = bundleOf(
                INTENT_EXTRA_INVITE_LINK to inviteLink
            )
            frag.inviteLinkSentListener = inviteLinkSentListener

            try {
                frag.show(fragmentManager, TAG)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var inviteLink: String = ""
    private var inviteLinkSentListener: EnterPhoneNumberForReferralDialogFragmentEventListener? =
        null
    private val sendSmsViewModel: SendSmsViewModel by activityViewModels()

    //View
    private lateinit var createGroupMainLayout: View
    private lateinit var progressBar: View
    private lateinit var groupNameET: EditText
    private lateinit var submitBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_invite_direct_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            inviteLink = it.getString(INTENT_EXTRA_INVITE_LINK) ?: return@let
        }

        arguments?.let {
            inviteLink = it.getString(INTENT_EXTRA_INVITE_LINK) ?: return@let
        }
        initView(view)
        initViewModel()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_INVITE_LINK, inviteLink)
    }


    private fun initViewModel() {
        sendSmsViewModel.sendSms
            .observe(viewLifecycleOwner, {
                when (it) {
                    Lse.Loading -> {
                        invite_direct_main.invisible()
                        progressBar.visible()
                    }
                    Lse.Success -> {
                        Toast.makeText(requireContext(), getString(R.string.sms_sent_amb), Toast.LENGTH_SHORT).show()
                        inviteLinkSentListener?.linkSent()
                        dismiss()
                    }
                    is Lse.Error -> {
                        progressBar.gone()
                        invite_direct_main.visible()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.error_amb))
                            .setMessage(getString(R.string.unable_to_send_message_amb) + it.error)
                            .setPositiveButton(getString(R.string.error_amb)) { _, _ -> }
                            .show()
                    }
                }
            })
    }

    private fun initView(view: View) {

        createGroupMainLayout = view.findViewById(R.id.invite_direct_main)
        progressBar = view.findViewById(R.id.progressBar)
        groupNameET = view.findViewById(R.id.phone_number_et)

        submitBtn = view.findViewById(R.id.create_button)

        submitBtn.setOnClickListener {

            val phoneNumber = phone_number_et.text.toString()

            if (phoneNumber.length != 10) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.error_amb))
                    .setMessage(getString(R.string.valid_10_digits_amb))
                    .setPositiveButton(getString(R.string.okay_amb)) { _, _ -> }
                    .show()
            }

            sendSmsViewModel.sendAmbassadorInviteLink(phoneNumber, inviteLink)
        }
    }


    interface EnterPhoneNumberForReferralDialogFragmentEventListener {

        fun linkSent()
    }

}