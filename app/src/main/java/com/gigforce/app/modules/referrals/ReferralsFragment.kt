package com.gigforce.app.modules.referrals

import android.content.*
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
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.getViewWidth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
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
        PushDownAnim.setPushDownAnimTo(iv_copy_link_referrals_frag).setOnClickListener(View.OnClickListener {
            val dynamicLinkUri = buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileViewModel.getProfileData().value?.id))
            showToast(getString(R.string.link_copied));
            val clipboard: ClipboardManager? =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("url",
                    "${getString(R.string.looking_for_dynamic_working_hours)} $dynamicLinkUri"
            )
            clipboard?.setPrimaryClip(clip)
        })
        PushDownAnim.setPushDownAnimTo(iv_whatsapp_referrals_frag).setOnClickListener(View.OnClickListener {
            val dynamicLinkUri = buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileViewModel.getProfileData().value?.id))
            shareViaWhatsApp("${getString(R.string.looking_for_dynamic_working_hours)} $dynamicLinkUri")
        })
        PushDownAnim.setPushDownAnimTo(iv_more_referrals_frag).setOnClickListener(View.OnClickListener {
            val dynamicLinkUri = buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileViewModel.getProfileData().value?.id))
            shareToAnyApp(dynamicLinkUri.toString())
        })

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

    fun buildDeepLink(deepLink: Uri): Uri {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(deepLink.toString()))
                .setDomainUriPrefix("https://gigforce.page.link/")
                // Open links with this app on Android
                .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
                // Open links with com.example.ios on iOS
                .setIosParameters(DynamicLink.IosParameters.Builder("com.gigforce.ios").build())
                .buildDynamicLink()

        return dynamicLink.uri;
    }

    fun shareViaWhatsApp(url: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "text/plain"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, url)
        try {
            requireActivity().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.whatsapp")))
        }
    }

    fun shareToAnyApp(url: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.looking_for_dynamic_working_hours))
            var shareMessage = url
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }
}