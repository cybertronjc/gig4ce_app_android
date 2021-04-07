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
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.*
import com.gigforce.core.utils.GlideApp
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_referrals.*
import java.io.File
import java.io.FileOutputStream


class ReferralsFragment : BaseFragment(),
    EnterPhoneNumberForReferralDialogFragment.EnterPhoneNumberForReferralDialogFragmentEventListener {
    val profileViewModel: ProfileViewModel by activityViewModels<ProfileViewModel>()
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ReferralFragmentViewModel(ModelReferralFragmentViewModel()))
    }
    private val viewModel: ReferralFragmentViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ReferralFragmentViewModel::class.java)
    }

    private var referralLink: String? = null
    private var referralLinkWithText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_referrals, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            referralLink = it.getString(INTENT_EXTRA_REFERRAL_LINK) ?: return@let
            referralLinkWithText = it.getString(INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT) ?: return@let
        }

        savedInstanceState?.let {
            referralLink = it.getString(INTENT_EXTRA_REFERRAL_LINK) ?: return@let
            referralLinkWithText = it.getString(INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT) ?: return@let
        }

        initUI();
        initObservers();
        initClicks();
        profileViewModel.getProfileData()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_REFERRAL_LINK, referralLink)
        outState.putString(INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT, referralLinkWithText)
    }

    private fun initClicks() {

        ll_top.apply {
            showTitle(getString(R.string.refer_your_friends))
            hideActionMenu()
            setBackButtonListener {
                popBackState()
            }
        }
    }

    private fun initObservers() {

        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profileData ->
            run {
                PushDownAnim.setPushDownAnimTo(send_direct_message_layout)
                    .setOnClickListener(View.OnClickListener {


                        if (referralLink != null) {
                            EnterPhoneNumberForReferralDialogFragment.launch(
                                referralLink!!,
                                this@ReferralsFragment,
                                childFragmentManager
                            )
                        } else {

                            pb_referrals_frag.visible()
                            Firebase.dynamicLinks.shortLinkAsync {
                                longLink =
                                    Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileData?.id)).toString())
                            }.addOnSuccessListener { result ->
                                // Short link created
                                if (context == null) return@addOnSuccessListener
                                val shortLink = result.shortLink
                                pb_referrals_frag.gone()

                                EnterPhoneNumberForReferralDialogFragment.launch(
                                    shortLink.toString(),
                                    this@ReferralsFragment,
                                    childFragmentManager
                                )

                            }.addOnFailureListener {
                                // Error
                                // ...
                                if (context == null) return@addOnFailureListener
                                showToast(it.message!!);
                                pb_referrals_frag.gone()


                            }
                        }

//                        val clipboard: ClipboardManager? =
//                            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
//                        val clip = ClipData.newPlainText(
//                            "token",
//                              FirebaseInstanceId.getInstance().getToken()
//                        )
//                        clipboard?.setPrimaryClip(clip)

                    })
                PushDownAnim.setPushDownAnimTo(send_via_whatsapp_layout)
                    .setOnClickListener(View.OnClickListener {

                        if (referralLinkWithText != null) {
                            shareViaWhatsApp(referralLinkWithText!!)
                        } else {

                            pb_referrals_frag.visible()

                            Firebase.dynamicLinks.shortLinkAsync {
                                longLink =
                                    Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileData?.id)).toString())
                            }.addOnSuccessListener { result ->
                                if (context == null) return@addOnSuccessListener
                                // Short link created
                                val shortLink = result.shortLink

                                shareViaWhatsApp("${getString(R.string.looking_for_dynamic_working_hours)} ${shortLink.toString()}")
                                pb_referrals_frag.gone()

                            }.addOnFailureListener {
                                if (context == null) return@addOnFailureListener
                                // Error
                                // ...
                                showToast(it.message!!)
                                pb_referrals_frag.gone()
                            }
                        }

                    })
                PushDownAnim.setPushDownAnimTo(send_via_other_apps)
                    .setOnClickListener(View.OnClickListener {

                        if (referralLinkWithText != null) {
                            shareToAnyApp(referralLinkWithText!!)

                        } else {

                            pb_referrals_frag.visible()

                            Firebase.dynamicLinks.shortLinkAsync {
                                longLink =
                                    Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?invite=" + profileData?.id)).toString())
                            }.addOnSuccessListener { result ->
                                // Short link created
                                if (context == null) return@addOnSuccessListener

                                val shortLink = result.shortLink
                                shareToAnyApp(shortLink.toString())


                            }.addOnFailureListener {
                                // Error
                                // ...
                                if (context == null) return@addOnFailureListener
                                showToast(it.message!!);

                            }
                        }
                    })



                viewModel.observableReferralErr.observe(viewLifecycleOwner, Observer {
                    showToast(it!!)
                })
                viewModel.observableReferredPeople.observe(viewLifecycleOwner, Observer {
                    iv_one_referrals_frag.visibility = View.GONE
                    iv_two_referrals_frag.visibility = View.GONE
                    tv_more_items_referrals_frag.visibility = View.GONE
                    rv_successful_recommendation_referrals_frag.isVisible = it?.size ?: 0 != 0
                    it?.elementAtOrNull(0)?.let { first ->
                        iv_one_referrals_frag.visibility = View.VISIBLE
                        displayImage(first.profileAvatarName, iv_one_referrals_frag)
                        tv_you_helped_referrals_frag.text = getString(R.string.you_helped) + " " +
                                first.name + " " + getString(R.string.and) + " " + (it.size - 1) + " " + getString(
                            R.string.more
                        ) +
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
                    if (iv_two_referrals_frag.visibility == View.GONE) {
                        val layoutParams: RelativeLayout.LayoutParams =
                            tv_you_helped_referrals_frag.layoutParams as RelativeLayout.LayoutParams
                        layoutParams.addRule(RelativeLayout.END_OF, iv_one_referrals_frag.id)
                        tv_more_items_referrals_frag.layoutParams = layoutParams
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
            .setDomainUriPrefix(BuildConfig.REFERRAL_BASE_URL)
            // Open links with this app on Android
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            // Open links with com.example.ios on iOS
            .setIosParameters(DynamicLink.IosParameters.Builder("com.gigforce.ios").build())
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle("Gigforce")
                    .setDescription("Flexible work and learning platform")
                    .setImageUrl(Uri.parse("https://firebasestorage.googleapis.com/v0/b/gig4ce-app.appspot.com/o/app_assets%2Fgigforce.jpg?alt=media&token=f7d4463b-47e4-4b8e-9b55-207594656161"))
                    .build()
            ).buildDynamicLink()

        return dynamicLink.uri;
    }

    fun shareViaWhatsApp(url: String) {

        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.type = "image/png"
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, url)
        val bitmap =
            BitmapFactory.decodeResource(requireContext().resources, R.drawable.bg_gig_type)

        //save bitmap to app cache folder

        //save bitmap to app cache folder
        val outputFile = File(requireContext().cacheDir, "share" + ".png")
        val outPutStream = FileOutputStream(outputFile)
        bitmap.compress(CompressFormat.PNG, 100, outPutStream)
        outPutStream.flush()
        outPutStream.close()
        outputFile.setReadable(true, false)
        whatsappIntent.putExtra(
            Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                outputFile
            )
        )

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
                getString(R.string.app_name)
            )
            val shareMessage = getString(R.string.looking_for_dynamic_working_hours) + " " + url
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            val bitmap =
                BitmapFactory.decodeResource(requireContext().resources, R.drawable.bg_gig_type)

            //save bitmap to app cache folder

            //save bitmap to app cache folder
            val outputFile = File(requireContext().cacheDir, "share" + ".png")
            val outPutStream = FileOutputStream(outputFile)
            bitmap.compress(CompressFormat.PNG, 100, outPutStream)
            outPutStream.flush()
            outPutStream.close()
            outputFile.setReadable(true, false)
            shareIntent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    outputFile
                )
            )
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
        pb_referrals_frag.gone()
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

    companion object {
        const val INTENT_EXTRA_REFERRAL_LINK_WITH_TEXT = "referral_link_with_text"
        const val INTENT_EXTRA_REFERRAL_LINK = "referral_link"
    }

    override fun linkSent() {

    }
}