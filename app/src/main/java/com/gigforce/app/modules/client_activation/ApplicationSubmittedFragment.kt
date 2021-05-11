package com.gigforce.app.modules.client_activation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.utils.StringConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_application_submitted.*
import java.io.File
import java.io.FileOutputStream


class ApplicationSubmittedFragment : BaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_application_submitted, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getDataFromIntents(savedInstanceState)
        pending_tv.text = Html.fromHtml("Application check : <b>Pending</b>")
        thanks_msg.text = Html.fromHtml("Thanks for applying at ${bussinessName}. We will contact you soon.")

        listener()
    }

    var bussinessName: String? = null
    var mProfileId:String?=null
    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            bussinessName = it.getString(StringConstants.BUSSINESS_NAME.value) ?: return@let
            mProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value)?:return@let

        }
        arguments?.let {
            bussinessName = it.getString(StringConstants.BUSSINESS_NAME.value) ?: return@let
            mProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value)?:return@let
        }
    }

    private fun listener() {
        ic_back_iv?.setOnClickListener {
            activity?.onBackPressed()
        }

        refer_gig.setOnClickListener {
            pb_client_activation.visible()
            Firebase.dynamicLinks.shortLinkAsync {
                longLink =
                        Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?job_profile_id=$mProfileId&invite=${FirebaseAuth.getInstance().currentUser?.uid!!}")).toString())
            }.addOnSuccessListener { result ->
                // Short link created
                val shortLink = result.shortLink
                shareToAnyApp(shortLink.toString())
            }.addOnFailureListener {
                // Error
                // ...
                showToast(it.message!!)
            }
        }
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

        return dynamicLink.uri
    }


    fun shareToAnyApp(url: String) {
        try {
            pb_client_activation.gone()
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
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

    }
}