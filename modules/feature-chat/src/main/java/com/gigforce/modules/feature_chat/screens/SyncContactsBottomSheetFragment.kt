package com.gigforce.modules.feature_chat.screens

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.navigation.fragment.findNavController
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.ChatNavigation
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.databinding.SyncContactsBottomSheetFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SyncContactsBottomSheetFragment : BaseBottomSheetDialogFragment<SyncContactsBottomSheetFragmentBinding>(
    fragmentName = "SyncContactsBottomSheetFragment",
    layoutId = R.layout.sync_contacts_bottom_sheet_fragment
) {


    companion object {
        fun newInstance() = SyncContactsBottomSheetFragment()
        const val TAG = "SyncContactsBottomSheetFragment"
        fun launch(
            childFragmentManager : FragmentManager
        ){
            SyncContactsBottomSheetFragment().apply {
            }.show(childFragmentManager,TAG)
        }
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: ChatHeadersViewModel by viewModels()

    private val chatNavigation: ChatNavigation by lazy {
        ChatNavigation(navigation)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()

    }

    private fun initListeners() = viewBinding.apply {
        syncContactsButton.setOnClickListener {
            setFragmentResult("sync", bundleOf("sync" to 1))
            dismiss()
        }

        cancelButton.setOnClickListener {
            setFragmentResult("sync", bundleOf("sync" to 0))
            dismiss()
        }

    }


    override fun viewCreated(
        viewBinding: SyncContactsBottomSheetFragmentBinding,
        savedInstanceState: Bundle?
    ) {

    }
}