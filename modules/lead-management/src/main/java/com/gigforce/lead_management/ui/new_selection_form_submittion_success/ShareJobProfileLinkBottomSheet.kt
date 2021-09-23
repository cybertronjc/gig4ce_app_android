package com.gigforce.lead_management.ui.new_selection_form_submittion_success

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.databinding.FragmentNewSelectionFormSuccessBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareJobProfileLinkBottomSheet : BottomSheetDialogFragment() {

    companion object {

        private const val INTENT_EXTRA_SHARE_LINK = "share_link"
    }

    @Inject
    lateinit var navigation : INavigation
    private lateinit var viewbinding: FragmentNewSelectionFormSuccessBinding
    private lateinit var shareLink: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            shareLink = it.getString(INTENT_EXTRA_SHARE_LINK) ?: return@let
        }

        savedInstanceState?.let {
            shareLink = it.getString(INTENT_EXTRA_SHARE_LINK) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            INTENT_EXTRA_SHARE_LINK,
            shareLink
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewbinding = FragmentNewSelectionFormSuccessBinding.inflate(
            inflater,
            container,
            false
        )

        return viewbinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() = viewbinding.apply {

        nextButton.setOnClickListener {
            navigation.popBackStack(
                LeadManagementNavDestinations.FRAGMENT_JOINING,
                false
            )
        }
    }

}