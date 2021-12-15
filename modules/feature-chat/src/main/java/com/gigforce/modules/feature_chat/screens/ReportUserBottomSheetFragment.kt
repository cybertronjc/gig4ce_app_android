package com.gigforce.modules.feature_chat.screens

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.R
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gigforce.modules.feature_chat.databinding.ReportUserBottomSheetFragmentBinding
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ReportUserBottomSheetFragment : BaseBottomSheetDialogFragment<ReportUserBottomSheetFragmentBinding>(
    fragmentName = "SyncContactsBottomSheetFragment",
    layoutId = R.layout.report_user_bottom_sheet_fragment
) {

    companion object {
        const val INTENT_EXTRA_USER_ID = "user_id"
        const val INTENT_EXTRA_CHAT_HEADER_ID = "header_id"
        const val TAG = "ReportUserBottomSheetFragment"

        fun launch(
            chatHeaderId: String,
            userUid: String,
            fragmentManager: FragmentManager
        ) {

            ReportUserBottomSheetFragment().apply {
                arguments = bundleOf(
                    INTENT_EXTRA_USER_ID to userUid,
                    INTENT_EXTRA_CHAT_HEADER_ID to chatHeaderId
                    )
            }.show(fragmentManager,TAG)
        }
    }

    private val viewModel: ChatPageViewModel by viewModels()

    private lateinit var chatHeaderId: String
    private lateinit var userUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun viewCreated(
        viewBinding: ReportUserBottomSheetFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFromIntent(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntent(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userUid = it.getString(INTENT_EXTRA_USER_ID) ?: return@let
            chatHeaderId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(INTENT_EXTRA_USER_ID) ?: return@let
            chatHeaderId = it.getString(INTENT_EXTRA_CHAT_HEADER_ID) ?: return@let
        }
    }

    private fun initViewModel() {
        viewModel.blockingOrUnblockingUser.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lse.Loading -> {
                    viewBinding.reportButton.showProgress{
                        buttonText = "Dropping..."
                        progressColor = Color.WHITE
                    }
                    viewBinding.reportButton.isEnabled = false
                }
                Lse.Success -> {
                    showToast(getString(R.string.user_reported_chat))
                    dismiss()
                }
                is Lse.Error -> {
                    viewBinding.reportButton.hideProgress("Report")
                    viewBinding.reportButton.isEnabled = true
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_chat))
                        .setMessage(getString(R.string.unable_to_report_user_chat) + it.error)
                        .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                        .show()
                }
            }
        })
    }

    private fun initListeners() = viewBinding.apply{

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            reportButton.isEnabled = true
            when(checkedId) {
                R.id.reason_others -> {
                    otherReason.visible()
                }
                else -> {
                    otherReason.gone()
                }
            }
        }

        reportButton.setOnClickListener {
            val checkedRadioButtonId = radioGroup.checkedRadioButtonId
            if (checkedRadioButtonId == -1) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert_chat))
                    .setMessage(getString(R.string.select_a_reason_chat))
                    .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                    .show()

                return@setOnClickListener
            } else if (checkedRadioButtonId == R.id.reason_others
                && otherReason.text.isNullOrBlank()
            ) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert_chat))
                    .setMessage(getString(R.string.type_reason_chat))
                    .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                    .show()

                return@setOnClickListener
            }

            val reason = if (checkedRadioButtonId == R.id.other_reason) {
                otherReason.text.toString()
            } else {
                radioGroup.findViewById<RadioButton>(checkedRadioButtonId).text.toString()
            }

            viewModel.reportAndBlockUser(chatHeaderId, userUid, reason)

        }

    }

}