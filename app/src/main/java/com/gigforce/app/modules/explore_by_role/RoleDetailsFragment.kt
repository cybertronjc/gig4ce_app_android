package com.gigforce.app.modules.explore_by_role

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.profile.models.RoleInterests
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.getScreenWidth
import com.gigforce.app.utils.openPopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_role_details_fragment.*
import java.io.File
import java.io.FileOutputStream


class RoleDetailsFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener {
//    private val viewModelFactory by lazy {
//        ViewModelProviderFactory(
//                if (FirebaseAuth.getInstance().currentUser?.uid == null) RoleDetailsVIewModel(
//                        RoleDetailsRepoNewUser()
//                ) else RoleDetailsVIewModel(RoleDetailsRepository())
//        )
//    }

    private val viewModel: RoleDetailsVIewModel by lazy {
        if (FirebaseAuth.getInstance().currentUser?.uid == null) RoleDetailsVIewModel(
                RoleDetailsRepoNewUser()
        ) else RoleDetailsVIewModel(RoleDetailsRepository())

    }
    private val viewModelProfile: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }
    private val exploreByRoleViewModel by lazy {
        ExploreByRoleViewModel(ExploreByRoleRepository())
    }

    private val adapterPreferredLocation: AdapterPreferredLocation by lazy {
        AdapterPreferredLocation()
    }
    private var mRoleID: String? = ""
    private var mIsRoleViaDeeplink: Boolean? = false;
    private var mInviteUserID: String? = ""
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_role_details_fragment, inflater, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromSavedState(savedInstanceState)
        setupPreferredLocationRv()
        initClicks()
        initAsPerLoginState()

    }

    fun initAsPerLoginState() {
        if (FirebaseAuth.getInstance().currentUser?.uid == null) {
            viewModel.setNewUser(true)
            tv_mark_as_interest_role_details.setOnClickListener {
                navFragmentsData?.setData(
                        bundleOf(
                                StringConstants.ROLE_ID.value to mRoleID,
                                StringConstants.ROLE_VIA_DEEPLINK.value to mIsRoleViaDeeplink,
                                StringConstants.INVITE_USER_ID.value to mInviteUserID
                        )
                )
//                popFragmentFromStack(R.id.fragment_role_details)
                navigate(R.id.Login)
            }
        } else {
            checkForMarkedAsInterest()
            checkForRedirection()
        }
        initObservers()

    }

    private fun initClicks() {

        iv_options_role_details.setOnClickListener {
            openPopupMenu(it, R.menu.menu_assessment_result, this, activity)
        }
    }

    private fun checkForRedirection() {
        if (navFragmentsData?.getData() != null) {
            if (navFragmentsData?.getData()
                            ?.getBoolean(StringConstants.BACK_PRESSED.value, false) == true
            ) {
                navFragmentsData?.getData()?.putBoolean(StringConstants.BACK_PRESSED.value, false)


            } else if (navFragmentsData?.getData()
                            ?.getBoolean(StringConstants.MOVE_TO_NEXT_STEP.value) == true
            ) {
                viewModel.checkForProfileCompletionAndVerification()
                navFragmentsData?.getData()
                        ?.putBoolean(StringConstants.MOVE_TO_NEXT_STEP.value, false)
            }

        }

    }

    fun applyOnClick(isOnBoardingDone: Boolean) {


        if (!isOnBoardingDone) {
            if (mIsRoleViaDeeplink == true) {
                navFragmentsData?.setData(
                        bundleOf(
                                StringConstants.ROLE_ID.value to mRoleID,
                                StringConstants.ROLE_VIA_DEEPLINK.value to mIsRoleViaDeeplink,
                                StringConstants.INVITE_USER_ID.value to mInviteUserID

                        )
                )
            }
            navigate(R.id.onboardingfragment)
            return
        }
        pb_role_details.visible()
        checkForProfileAndVerificationData()

    }

    private fun checkForMarkedAsInterest() {
        pb_role_details.visible()
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer {
            val isOnBoardingDone = it.isonboardingdone
            if (!it.role_interests.isNullOrEmpty()) {

                exploreByRoleViewModel.observerVerified.observe(
                        viewLifecycleOwner,
                        Observer { verified ->
                            pb_role_details.gone()
                            run {

                                val rolePresent = it.role_interests!!.contains(RoleInterests(mRoleID))

                                if (rolePresent && verified!!) {
                                    tv_mark_as_interest_role_details.visible()
                                    tv_mark_as_interest_role_details.setOnClickListener(null)
                                    tv_mark_as_interest_role_details.text =
                                            Html.fromHtml("&#x2713 " + getString(R.string.activated))
                                    tv_mark_as_interest_role_details.compoundDrawablePadding =
                                            resources.getDimensionPixelSize(R.dimen.size_10)


                                } else if (rolePresent) {
                                    tv_mark_as_interest_role_details.text =
                                            Html.fromHtml("&#x2713 " + getString(R.string.applied))


                                } else {
                                    tv_mark_as_interest_role_details.setOnClickListener {
                                        applyOnClick(isOnBoardingDone)
                                    }
                                }
                            }

                        })
                exploreByRoleViewModel.checkVerifiedDocs()
            } else {
                tv_mark_as_interest_role_details.text = getString(R.string.apply_now)
                tv_mark_as_interest_role_details.setOnClickListener {
                    applyOnClick(isOnBoardingDone)


                }

            }

        })


    }

    private fun checkForProfileAndVerificationData() {
        pb_role_details.visible()
        viewModel.checkForProfileCompletionAndVerification()

    }


    private fun initObservers() {

        viewModel.observerRole.observe(viewLifecycleOwner, Observer { role ->
            run {
                tv_role_role_details.text = role?.role_title
                tv_what_content_role_details.text = role?.about
                tv_what_read_more_details.text =
                        "${getString(R.string.what_does_a)} ${role?.role_title} ${
                        getString(
                                R.string.do_question_mark
                        )
                        }"
                adapterPreferredLocation.addData(role?.top_locations ?: mutableListOf())
                tv_earnings_role_details.setOnClickListener {
                    setOnExpandListener(
                            role?.payments_and_benefits,
                            tl_earnings_role_details,
                            tv_earnings_role_details
                    )

                }
                tv_requirements_role_details.setOnClickListener {
                    setOnExpandListener(
                            role?.requirements,
                            tl_requirements_role_details,
                            tv_requirements_role_details
                    )

                }
                tv_responsibilities_role_details.setOnClickListener {
                    setOnExpandListener(
                            role?.job_description,
                            tl_responsibilities_role_details,
                            tv_responsibilities_role_details
                    )

                }

            }
            pb_role_details.gone()

        })
        viewModel.observerError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observerMarkedAsInterest.observe(viewLifecycleOwner, Observer {
            navigate(R.id.fragment_marked_as_interest)
        })
        viewModel.observerDataToCheck.observe(viewLifecycleOwner, Observer {
            run {

                pb_role_details.gone()
                for (i in 0 until it?.size!!) {
                    val element = it[i]
                    if (navFragmentsData?.getData()
                                    ?.getBoolean(StringConstants.NAVIGATE_TO_MARK_AS_INTERESTED.value) == true
                    ) {
                        navFragmentsData?.getData()
                                ?.putBoolean(
                                        StringConstants.NAVIGATE_TO_MARK_AS_INTERESTED.value,
                                        false
                                )
                        navigate(
                                R.id.fragment_marked_as_interest,
                                bundleOf(
                                        StringConstants.ROLE_ID.value to mRoleID,
                                        StringConstants.INVITE_USER_ID.value to mInviteUserID
                                )
                        )
                        break
                    } else if (navFragmentsData?.getData()
                                    ?.getBoolean(StringConstants.NAV_TO_QUESTIONNARE.value) == true
                    ) {
                        navFragmentsData?.getData()
                                ?.putBoolean(StringConstants.NAV_TO_QUESTIONNARE.value, false)
                        navigate(R.id.fragment_questionnaire)
                        break
                    } else if (element is ProfileData) {
                        if (element.aboutMe == null || element.aboutMe.isEmpty()) {
                            navigate(R.id.fragment_add_bio)
                            break
                        } else if (element.languages == null || element.languages!!.isEmpty()) {
                            navigate(R.id.fragment_add_language)
                            break
                        } else if (viewModel.emailValidated == false && (element.contactEmail == null || element.contactEmail!!.isEmpty() || element.contactEmail!![0].email.isNullOrEmpty()
                                        )) {
                            navigate(R.id.fragment_add_contact)
                            viewModel.emailValidated = true
                            break
                        } else if (element.educations == null || element.educations!!.isEmpty()) {
                            navigate(R.id.fragment_new_education)
                            break
                        } else if (element.experiences == null || element.experiences!!.isEmpty()) {
                            navigate(R.id.fragment_add_experience)
                            break

                        }

                    } else if (element is VerificationBaseModel) {
                        if (element?.bank_details == null || element?.bank_details?.verified == false || element?.selfie_video == null || element?.selfie_video?.verified == false
                                || element?.pan_card == null || element?.pan_card?.verified == false || element?.aadhar_card == null || element?.aadhar_card?.verified == false
                                || element?.driving_license == null || element?.driving_license?.verified == false

                        ) {
                            navigate(
                                    R.id.gigerVerificationFragment, bundleOf(
                                    StringConstants.SHOW_ACTION_BUTTONS.value to true
                            )
                            )
                            break
                        } else {
                            navigate(R.id.fragment_questionnaire)
                        }


                    }
                }

            }

        })

        viewModel.getRoleDetails(mRoleID)
        pb_role_details.visible()
    }

    fun setOnExpandListener(role: List<String>?, layout: TableLayout, textView: TextView) {
        if (layout.childCount > 0) {
            layout.removeAllViews()
            textView.setCompoundDrawablesWithIntrinsicBounds(
                    textView.compoundDrawables[0],
                    null,
                    resources.getDrawable(R.drawable.ic_keyboard_arrow_down_c7c7cc),
                    null
            )

        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                    textView.compoundDrawables[0],
                    null,
                    resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_c7c7c7),
                    null
            )
            addBulletsTill(
                    0,
                    if (role?.size!! > 2) 1 else role.size!! - 1,
                    layout,
                    role,
                    true
            )
            if (role?.size!! > 2) {
                val moreTextView = AppCompatTextView(requireContext())
                moreTextView.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        14F
                )
                moreTextView.setTextColor(resources.getColor(R.color.lipstick))
                moreTextView.text = getString(R.string.plus_more)
                val face =
                        Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
                moreTextView.typeface = face
                moreTextView.setPadding(resources.getDimensionPixelSize(R.dimen.size_16), 0, 0, 0)

                layout.addView(moreTextView)
                moreTextView.setOnClickListener {
                    layout.removeViewInLayout(moreTextView)
                    addBulletsTill(
                            2,
                            role.size!! - 1,
                            layout,
                            role,
                            false
                    )
                }
            }
        }

    }

    fun addBulletsTill(
            from: Int,
            to: Int,
            layout: TableLayout,
            arr: List<String>?,
            removeAllViews: Boolean
    ) {
        if (removeAllViews)
            ll_earn_role_details.removeAllViews()
        for (i in from..to) {

            val iv = ImageView(requireContext())
            val layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(
                    0,
                    resources.getDimensionPixelSize(R.dimen.font_9),
                    resources.getDimensionPixelSize(R.dimen.size_8),
                    0
            )
            iv.layoutParams = layoutParams
            iv.setImageResource(R.drawable.shape_circle_lipstick)
            val textView = TextView(requireContext())
            val face =
                    Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
            textView.typeface = face
            textView.layoutParams = TableRow.LayoutParams(
                    getScreenWidth(requireActivity()).width - (resources.getDimensionPixelSize(R.dimen.size_66)),
                    TableRow.LayoutParams.WRAP_CONTENT
            )

            textView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    18F
            )
            textView.text = arr?.get(i)
            textView.setTextColor(resources.getColor(R.color.black))
            val tr = TableRow(requireContext())


            tr.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
            )

            tr.addView(iv)
            tr.addView(textView)
            layout.addView(
                    tr,
                    TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT
                    )
            )

        }
    }

    private fun setupPreferredLocationRv() {
        rv_preferred_locations_role_details.adapter = adapterPreferredLocation
        rv_preferred_locations_role_details.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv_preferred_locations_role_details.addItemDecoration(
                HorizontaltemDecoration(
                        requireContext(),
                        R.dimen.size_11
                )
        )

    }

    private fun getDataFromSavedState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
            mIsRoleViaDeeplink = it.getBoolean(StringConstants.ROLE_VIA_DEEPLINK.value, false)
            mInviteUserID = it.getString(StringConstants.INVITE_USER_ID.value, "")

        }

        arguments?.let {
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
            mIsRoleViaDeeplink = it.getBoolean(StringConstants.ROLE_VIA_DEEPLINK.value, false)
            mInviteUserID = it.getString(StringConstants.INVITE_USER_ID.value, "")

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.ROLE_ID.value, mRoleID)
        outState.putBoolean(StringConstants.ROLE_VIA_DEEPLINK.value, mIsRoleViaDeeplink ?: false)
        outState.putString(StringConstants.INVITE_USER_ID.value, mInviteUserID)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                pb_role_details.visible()
                Firebase.dynamicLinks.shortLinkAsync {
                    longLink =
                            Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?role_id=$mRoleID&invite=${viewModel.getUID()}")).toString())
                }.addOnSuccessListener { result ->
                    // Short link created
                    val shortLink = result.shortLink
                    shareToAnyApp(shortLink.toString())
                }.addOnFailureListener {
                    // Error
                    // ...
                    showToast(it.message!!);
                }
                return true
            }
        }
        return false;
    }

    fun buildDeepLink(deepLink: Uri): Uri {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(deepLink.toString()))
                .setDomainUriPrefix("https://gigforce.page.link/")
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
        pb_role_details.gone()
    }

    override fun onBackPressed(): Boolean {
        if (nav_fragment.requireActivity().supportFragmentManager.fragments.size == 1) {
            requireActivity().finish()
            return true
        }
        super.onBackPressed()
        return false

    }
}