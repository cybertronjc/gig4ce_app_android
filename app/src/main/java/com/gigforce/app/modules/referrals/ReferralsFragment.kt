package com.gigforce.app.modules.referrals

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.activityViewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.getViewWidth
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_referrals.*


class ReferralsFragment : BaseFragment() {
    val profileViewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_referrals, inflater, container)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI();
        initClicks();
        profileViewModel.getProfileData()
    }

    private fun initClicks() {
        iv_copy_link_referrals_frag.setOnClickListener {

            val dynamicLink = Firebase.dynamicLinks.dynamicLink {
                link = Uri.parse("http://www.gig4ce.com/?invite="+profileViewModel.getProfileData().value?.id)
                domainUriPrefix = "https://gigforce.page.link/invite"
                androidParameters {
                    "check" to true
                }


                // Open links with this app on Android
            }

            val dynamicLinkUri = dynamicLink.uri
            showToast(getString(R.string.link_copied));
            val clipboard: ClipboardManager? =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("url",
                "Looking for flexible working hours? Join me at Gigforce : $dynamicLinkUri"
            )
            clipboard?.setPrimaryClip(clip)
        }
    }

    private fun initUI() {
        tv_more_items_referrals_frag.setStrokeWidth(1);
        tv_more_items_referrals_frag.setStrokeColor("#ffffff");
        tv_more_items_referrals_frag.setSolidColor("#d72467");
        val params: RelativeLayout.LayoutParams =
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        params.setMargins(-(getViewWidth(tv_more_items_referrals_frag) / 2), 0, 0, 0)
        params.addRule(RelativeLayout.END_OF, R.id.iv_two_referrals_frag)
        params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.iv_two_referrals_frag)
        tv_more_items_referrals_frag.layoutParams = params
    }
}