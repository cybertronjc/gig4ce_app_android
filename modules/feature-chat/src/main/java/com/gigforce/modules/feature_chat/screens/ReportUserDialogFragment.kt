package com.gigforce.modules.feature_chat.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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

    private val viewModel: ChatPageViewModel by viewModels()

    private lateinit var headerId: String
    private lateinit var userUid: String

    //View
    private lateinit var reportUserMainLayout: View
    private lateinit var progressBar: View
    private lateinit var reason_radio_group: RadioGroup
    private lateinit var reason_label: TextView
    private lateinit var reason_et: EditText
    private lateinit var confirm_decline_cb: CheckBox
    private lateinit var submitBtn: Button

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
        initView(view)
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_USER_ID, userUid)
        outState.putString(INTENT_EXTRA_CHAT_HEADER_ID, headerId)
    }


    private fun initViewModel() {
        viewModel.blockingOrUnblockingUser.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lse.Loading -> {
                    reportUserMainLayout.invisible()
                    progressBar.visible()
                }
                Lse.Success -> {
                    Toast.makeText(requireContext(), getString(R.string.user_reported_chat), Toast.LENGTH_LONG)
                            .show()
                    dismiss()
                }
                is Lse.Error -> {
                    progressBar.gone()
                    reportUserMainLayout.visible()
                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_chat))
                            .setMessage(getString(R.string.unable_to_report_user_chat) + it.error)
                            .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                            .show()
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            //setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    private fun initView(view: View) {

        reportUserMainLayout = view.findViewById(R.id.reportUserMainLayout)
        progressBar = view.findViewById(R.id.progressBar)
        reason_radio_group = view.findViewById(R.id.reason_radio_group)
        reason_label = view.findViewById(R.id.reason_label)
        reason_et = view.findViewById(R.id.reason_et)
        confirm_decline_cb = view.findViewById(R.id.confirm_decline_cb)
        submitBtn = view.findViewById(R.id.submitBtn)

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
                        .setTitle(getString(R.string.alert_chat))
                        .setMessage(getString(R.string.select_a_reason_chat))
                        .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                        .show()

                return@setOnClickListener
            } else if (checkedRadioButtonId == R.id.reason_others
                    && reason_et.text.isNullOrBlank()
            ) {

                confirm_decline_cb.isChecked = false
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_chat))
                        .setMessage(getString(R.string.type_reason_chat))
                        .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                        .show()

                return@setOnClickListener
            }

            val reason = if (checkedRadioButtonId == R.id.reason_others) {
                reason_et.text.toString()
            } else {
                reason_radio_group.findViewById<RadioButton>(checkedRadioButtonId).text.toString()
            }

            viewModel.reportAndBlockUser(headerId, userUid, reason)
        }
    }
}