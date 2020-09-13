package com.gigforce.app.modules.referrals

import android.content.*
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.ViewModelProviderFactory
import com.gigforce.app.utils.getViewWidth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_referrals.*
import java.io.File
import java.io.FileOutputStream


class ReferralsFragment : BaseFragment() {
    val profileViewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ReferralFragmentViewModel(ModelReferralFragmentViewModel()))
    }
    private val viewModel: ReferralFragmentViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ReferralFragmentViewModel::class.java)
    }

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
        initObservers();
        profileViewModel.getProfileData()
    }

    private fun initObservers() {

        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profileData ->
            run {
                PushDownAnim.setPushDownAnimTo(iv_copy_link_referrals_frag)
                        .setOnClickListener(View.OnClickListener {
                            val dynamicLinkUri =
                                    buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileData?.id))
                            showToast(getString(R.string.link_copied));
                            val clipboard: ClipboardManager? =
                                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                            val clip = ClipData.newPlainText(
                                    "url",
                                    "${getString(R.string.looking_for_dynamic_working_hours)} $dynamicLinkUri"
                            )
                            clipboard?.setPrimaryClip(clip)
                        })
                PushDownAnim.setPushDownAnimTo(iv_whatsapp_referrals_frag)
                        .setOnClickListener(View.OnClickListener {
                            val dynamicLinkUri =
                                    buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileData.id))
                            shareViaWhatsApp("${getString(R.string.looking_for_dynamic_working_hours)} $dynamicLinkUri")
                        })
                PushDownAnim.setPushDownAnimTo(iv_more_referrals_frag)
                        .setOnClickListener(View.OnClickListener {
                            val dynamicLinkUri =
                                    buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileData.id))
                            shareToAnyApp(dynamicLinkUri.toString())
                        })
                viewModel.observableReferralErr.observe(viewLifecycleOwner, Observer {
                    showToast(it!!)
                })
                viewModel.observableReferredPeople.observe(viewLifecycleOwner, Observer {
                    iv_one_referrals_frag.visibility = View.GONE
                    iv_two_referrals_frag.visibility = View.GONE
                    tv_more_items_referrals_frag.visibility = View.GONE
                    rv_successful_recommendation_referrals_frag.visibility = View.VISIBLE
                    it?.elementAtOrNull(0)?.let { first ->
                        iv_one_referrals_frag.visibility = View.VISIBLE
                        displayImage(first.profileAvatarName, iv_one_referrals_frag)
                        tv_you_helped_referrals_frag.text = getString(R.string.you_helped) + " " +
                                first.name + " " + getString(R.string.and) + " " + (it.size - 1) + " " + getString(R.string.more_comma) +
                                " " + getString(R.string.find_gigs_on)

                    }
                    it?.elementAtOrNull(1)?.let { second ->
                        iv_two_referrals_frag.visibility = View.VISIBLE
                        displayImage(second.profileAvatarName, iv_two_referrals_frag)
                    }
                    it?.elementAtOrNull(2)?.let { third ->
                        tv_more_items_referrals_frag.visibility = View.VISIBLE
                        tv_more_items_referrals_frag.text = "+${it.size - 2}"
                    }
                })
                viewModel.getReferredPeople(profileData.invited?.map {
                    it.invite_id
                } ?: listOf())
            }


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
        whatsappIntent.type = "image/png"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, url)
        val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.gig4ce_logo)

        //save bitmap to app cache folder

        //save bitmap to app cache folder
        val outputFile = File(requireContext().cacheDir, "share" + ".png")
        val outPutStream = FileOutputStream(outputFile)
        bitmap.compress(CompressFormat.PNG, 100, outPutStream)
        outPutStream.flush()
        outPutStream.close()
        outputFile.setReadable(true, false)
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                outputFile
        ))

        try {
            requireActivity().startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            startActivity(
                    Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=com.whatsapp")
                    )
            )
        }
    }

    fun shareToAnyApp(url: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/png"
            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.looking_for_dynamic_working_hours)
            )
            var shareMessage = url
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            val bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.gig4ce_logo)

            //save bitmap to app cache folder

            //save bitmap to app cache folder
            val outputFile = File(requireContext().cacheDir, "share" + ".png")
            val outPutStream = FileOutputStream(outputFile)
            bitmap.compress(CompressFormat.PNG, 100, outPutStream)
            outPutStream.flush()
            outPutStream.close()
            outputFile.setReadable(true, false)
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    outputFile
            ))
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    private fun displayImage(profileImg: String, imageView: ImageView) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                    PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                    .load(profilePicRef)
                    .apply(RequestOptions().circleCrop())
                    .into(imageView)
        } else {
            GlideApp.with(this.requireContext())
                    .load(R.drawable.avatar)
                    .apply(RequestOptions().circleCrop())
                    .into(imageView)
        }
    }
}