package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.gigforce.core.base.BaseFragment2
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ShareApplicationLinkFragmentBinding

class ShareApplicationLinkFragment : BaseFragment2<ShareApplicationLinkFragmentBinding>(
    fragmentName = "ShareApplicationLinkFragment",
    layoutId = R.layout.share_application_link_fragment,
    statusBarColor = R.color.colorAccent
) {
    private val viewModel: ShareApplicationLinkViewModel by viewModels()

    override fun viewCreated(
        viewBinding: ShareApplicationLinkFragmentBinding,
        savedInstanceState: Bundle?
    ) {


    }
}