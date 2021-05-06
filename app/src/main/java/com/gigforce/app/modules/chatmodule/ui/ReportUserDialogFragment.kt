package com.gigforce.app.modules.chatmodule.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.visible
//import com.gigforce.app.modules.chatmodule.viewModels.ChatMessagesViewModel
import com.gigforce.core.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_report_user.*
import kotlinx.android.synthetic.main.fragment_report_user_main.*


class ReportUserDialogFragment : DialogFragment() {

    companion object {
        const val INTENT_EXTRA_USER_ID = "user_id"
        const val INTENT_EXTRA_CHAT_HEADER_ID = "header_id"
        const val TAG = "ReportUserDialogFragment"

        fun launch(
            chatHeaderId: String,
            userUid: String,
            fragmentManager: FragmentManager
        ) {
            val frag = ReportUserDialogFragment()
            frag.arguments = bundleOf(
                INTENT_EXTRA_USER_ID to userUid,
                INTENT_EXTRA_CHAT_HEADER_ID to chatHeaderId
            )
            frag.show(fragmentManager, TAG)
        }

    }

//    private val viewModel: ChatMessagesViewModel by viewModels()

    private lateinit var headerId: String
    private lateinit var userUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_report_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            userUid = it.getString(INTENT_EXTRA_USER_ID)!!
            headerId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID)!!
        }

        arguments?.let {
            userUid = it.getString(INTENT_EXTRA_USER_ID)!!
            headerId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID)!!
        }
        initView()
//        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_USER_ID, userUid)
        outState.putString(INTENT_EXTRA_CHAT_HEADER_ID, headerId)
    }


//    private fun initViewModel() {
//        viewModel.blockingOrUnblockingUser.observe(viewLifecycleOwner, Observer {
//
//            when (it) {
//                Lse.Loading -> {
//                    reportUserMainLayout.invisible()
//                    progressBar.visible()
//                }
//                Lse.Success -> {
//                    Toast.makeText(requireContext(), "User reported", Toast.LENGTH_LONG)
//                        .show()
//                    dismiss()
//                }
//                is Lse.Error -> {
//                    progressBar.gone()
//                    reportUserMainLayout.visible()
//                    MaterialAlertDialogBuilder(requireContext())
//                        .setTitle("Alert")
//                        .setMessage("Unable to report user, ${it.error}")
//                        .setPositiveButton("Okay") { _, _ -> }
//                        .show()
//                }
//            }
//        })
//    }

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

        reason_radio_group.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == R.id.reason_others) {
                reason_label.visible()
                reason_et.visible()
            } else {
                reason_label.gone()
                reason_et.gone()
            }
        }

        confirm_decline_cb.setOnCheckedChangeListener { _, isChecked ->
            submitBtn.isEnabled = isChecked
        }

        submitBtn.setOnClickListener {

            val checkedRadioButtonId = reason_radio_group.checkedRadioButtonId
            if (checkedRadioButtonId == -1) {

                confirm_decline_cb.isChecked = false
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Please select the reason")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()

                return@setOnClickListener
            } else if (checkedRadioButtonId == R.id.reason_others
                && reason_et.text.isNullOrBlank()
            ) {

                confirm_decline_cb.isChecked = false
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Alert")
                    .setMessage("Please type the reason")
                    .setPositiveButton("Okay") { _, _ -> }
                    .show()

                return@setOnClickListener
            }

            val reason = if (checkedRadioButtonId == R.id.reason_others) {
                reason_et.text.toString()
            } else {
                reason_radio_group.findViewById<RadioButton>(checkedRadioButtonId).text.toString()
            }

//            viewModel.reportAndBlockUser(headerId, userUid, reason)
        }
    }
}