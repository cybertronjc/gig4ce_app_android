package com.gigforce.modules.feature_chat.screens

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.utils.Lse
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.gigforce.modules.feature_chat.databinding.FragmentBlockUserBottomSheetBinding
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.gigforce.modules.feature_chat.screens.vm.ChatPageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BlockUserBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentBlockUserBottomSheetBinding>(
    fragmentName = "BlockUserBottomSheetFragment",
    layoutId = R.layout.fragment_block_user_bottom_sheet
) {

    companion object {
        const val INTENT_EXTRA_BLOCK_UNBLOCK = "block"
        const val INTENT_EXTRA_USER_ID = "user_id"
        const val INTENT_EXTRA_CHAT_HEADER_ID = "header_id"
        const val TAG = "BlockUserBottomSheetFragment"

        fun launch(
            chatHeaderId: String,
            userUid: String,
            fragmentManager: FragmentManager
        ) {

            BlockUserBottomSheetFragment().apply {
                arguments = bundleOf(
                    INTENT_EXTRA_USER_ID to userUid,
                    INTENT_EXTRA_CHAT_HEADER_ID to chatHeaderId
                )
            }.show(fragmentManager,TAG)
        }
    }

    @Inject
    lateinit var eventTracker: IEventTracker

    private val viewModel: ChatPageViewModel by viewModels()

    private lateinit var chatHeaderId: String
    private lateinit var userUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun viewCreated(
        viewBinding: FragmentBlockUserBottomSheetBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFromIntent(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntent(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userUid = it.getString(ReportUserBottomSheetFragment.INTENT_EXTRA_USER_ID) ?: return@let
            chatHeaderId = it.getString(ReportUserBottomSheetFragment.INTENT_EXTRA_CHAT_HEADER_ID) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(ReportUserBottomSheetFragment.INTENT_EXTRA_USER_ID) ?: return@let
            chatHeaderId = it.getString(ReportUserBottomSheetFragment.INTENT_EXTRA_CHAT_HEADER_ID) ?: return@let
        }
    }

    private fun initViewModel() {
        viewModel.blockingOrUnblockingUser.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lse.Loading -> {
                    viewBinding.blockButton.showProgress{
                        buttonText = "Blocking..."
                        progressColor = Color.WHITE
                    }
                    viewBinding.blockButton.isEnabled = false
                }
                Lse.Success -> {
                    showToast("User blocked")
                    eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_BLOCKED_USER, null))
                    dismiss()
                }
                is Lse.Error -> {
                    viewBinding.blockButton.hideProgress("Block")
                    viewBinding.blockButton.isEnabled = true
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_chat))
                        .setMessage(getString(R.string.unable_to_block_user) + it.error)
                        .setPositiveButton(getString(R.string.okay_chat)) { _, _ -> }
                        .show()
                }
            }
        })
    }

    private fun initListeners() {
        viewBinding.blockButton.setOnClickListener {
            viewModel.blockOrUnBlockUser(
                chatHeaderId,
                userUid,
                false
            )
        }

        viewBinding.cancelButton.setOnClickListener {
            dismiss()
        }

    }

}