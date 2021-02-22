package com.gigforce.app.modules.client_activation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.Media
import com.gigforce.app.modules.explore_by_role.AdapterPreferredLocation
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.learningVideo.PlayVideoDialogFragment
import com.gigforce.app.modules.learning.models.LessonModel
import com.gigforce.app.utils.*
import com.gigforce.common_ui.MenuItem
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.decors.HorizontaltemDecoration
import com.gigforce.core.utils.GlideApp
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.skydoves.powermenu.CustomPowerMenu
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import kotlinx.android.synthetic.main.layout_fragment_client_activation.*
import kotlinx.android.synthetic.main.layout_role_description.view.*
import java.io.File
import java.io.FileOutputStream

class ClientActivationFragment : BaseFragment(),
        LocationUpdates.LocationUpdateCallbacks {
    private var mInviteUserID: String? = null
    private var mClientViaDeeplink: Boolean? = null
    private lateinit var mJobProfileId: String
    private var mRedirectToApplication: Boolean? = null
    private lateinit var viewModel: ClientActivationViewmodel
    private var adapterPreferredLocation: AdapterPreferredLocation? = null
    private lateinit var adapterBulletPoints: AdapterBulletPoints


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_client_activation, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
                ViewModelProvider(
                        this,
                        SavedStateViewModelFactory(requireActivity().application, this)
                ).get(ClientActivationViewmodel::class.java)
        viewModel.setRepository(if (FirebaseAuth.getInstance().currentUser?.uid == null) ClientActivationNewUserRepo() else ClientActivationRepository())
        getDataFromIntents(savedInstanceState)
        checkForApplicationRedirection()
        setupPreferredLocationRv()
        setupBulletPontsRv()
        initClicks()
        initObservers()

    }

    private fun checkForApplicationRedirection() {
        if (mRedirectToApplication == true) {
            sv_client_activation.invisible()
            tv_mark_as_interest_role_details.invisible()
            tb_overlay_cl_act.invisible()
            pb_client_activation.visible()
        }
    }


    private fun setupBulletPontsRv() {
        adapterBulletPoints = AdapterBulletPoints();

        rv_bullet_points.adapter = adapterBulletPoints
        rv_bullet_points.layoutManager =
                LinearLayoutManager(requireContext())


    }

    var customPowerMenu: CustomPowerMenu<*, *>? = null

    private fun initClicks() {

        iv_back_client_activation.setOnClickListener {
            popBackState()
        }

        iv_options_client_activation.setOnClickListener {

            customPowerMenu =
                    CustomPowerMenu.Builder(requireContext(), PopMenuAdapter())
                            .addItem(
                                MenuItem(getString(R.string.share))
                            )

                            .setShowBackground(false)
                            .setOnMenuItemClickListener(object :
                                    OnMenuItemClickListener<MenuItem> {
                                override fun onItemClick(
                                        position: Int,
                                        item: MenuItem?
                                ) {
                                    pb_client_activation.visible()
                                    Firebase.dynamicLinks.shortLinkAsync {
                                        longLink =
                                                Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?job_profile_id=$mJobProfileId&invite=${viewModel.getUID()}")).toString())
                                    }.addOnSuccessListener { result ->
                                        // Short link created
                                        val shortLink = result.shortLink
                                        shareToAnyApp(shortLink.toString())
                                    }.addOnFailureListener {
                                        // Error
                                        // ...
                                        showToast(it.message!!);
                                    }
                                    customPowerMenu?.dismiss()
                                }

                            })
                            .setAnimation(MenuAnimation.DROP_DOWN)
                            .setMenuRadius(
                                    resources.getDimensionPixelSize(R.dimen.size_4).toFloat()
                            )
                            .setMenuShadow(
                                    resources.getDimensionPixelSize(R.dimen.size_4).toFloat()
                            )

                            .build()
            customPowerMenu?.showAsDropDown(
                    it,
                    -(((customPowerMenu?.getContentViewWidth()
                            ?: 0) - (it.resources.getDimensionPixelSize(R.dimen.size_32))
                            )
                            ),
                    -(resources.getDimensionPixelSize(
                            R.dimen.size_24
                    )
                            )
            )
        }

    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mRedirectToApplication = it.getBoolean(StringConstants.AUTO_REDIRECT_TO_APPL.value, false)
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: ""
            mClientViaDeeplink =
                    it.getBoolean(StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value, false)
            mInviteUserID = it.getString(StringConstants.INVITE_USER_ID.value) ?: return@let


        }

        arguments?.let {
            mRedirectToApplication = it.getBoolean(StringConstants.AUTO_REDIRECT_TO_APPL.value, false)
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mClientViaDeeplink =
                    it.getBoolean(StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value, false)
            mInviteUserID = it.getString(StringConstants.INVITE_USER_ID.value) ?: return@let
        }
    }

    private fun initObservers() {
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
            pb_client_activation.gone()
        })
        viewModel.observableJobProfile.observe(viewLifecycleOwner, Observer { it ->
            if (it == null) return@Observer
            if (it.info == null) return@Observer

            Glide.with(this).load(it.coverImg).placeholder(
                com.gigforce.common_ui.utils.getCircularProgressDrawable(
                    requireContext()
                )
            ).into(iv_main_client_activation)
            tv_businessname_client_activation.text = (it?.title ?: "")
            tv_role_client_activation.text = (it?.subTitle ?: "")
            it?.locationList?.map { item -> item.location }?.let { locations ->
                adapterPreferredLocation?.addData(locations)
            }
            tv_earning_client_activation.text = Html.fromHtml(it?.payoutNote)
            ll_role_desc.removeAllViews()
            it?.queries?.forEach { element ->
                val viewRoleDesc = layoutInflater.inflate(R.layout.layout_role_description, null)
                viewRoleDesc.tv_what_client_activation.text = element.query
                viewRoleDesc.tv_what_value_client_activation.text = element.answer

                if (!element.icon.isNullOrEmpty()) {
                    GlideApp.with(requireContext())
                            .load(element.icon)
                            .placeholder(getCircularProgressDrawable())
                            .into(viewRoleDesc.iv_what)

                } else {
                    viewRoleDesc.iv_what.setImageResource(R.drawable.ic_play_gradient)
                }
                ll_role_desc.addView(viewRoleDesc)

            }

            adapterBulletPoints.addData(it?.info!!)


            learning_cl.visible()
            textView120.text = it?.requiredMedia?.title
            if (!it?.requiredMedia?.icon.isNullOrEmpty()) {
                GlideApp.with(requireContext())
                        .load(it?.requiredMedia?.icon)
                        .placeholder(getCircularProgressDrawable())
                        .into(imageView36)

            } else {
                imageView36.setImageResource(R.drawable.ic_play_gradient)
            }
            initializeLearningModule(it?.requiredMedia?.media ?: listOf())


            viewModel.getApplication(it?.profileId ?: "")

        })
        viewModel.observableAddInterest.observe(viewLifecycleOwner, Observer {
            pb_client_activation.gone()
            if (it == true) {
                navigate(
                        R.id.fragment_application_client_activation, bundleOf(
                        StringConstants.JOB_PROFILE_ID.value to viewModel.observableJobProfile.value?.profileId
                )
                )
            }
        })

        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer { jpApplication ->
            pb_client_activation.gone()
            tv_mark_as_interest_role_details.text = getString(R.string.apply_now)
            run {
                if (FirebaseAuth.getInstance().currentUser?.uid == null) {
                    iv_options_client_activation.gone()
                    tv_mark_as_interest_role_details.setOnClickListener {
                        navFragmentsData?.setData(
                                bundleOf(
                                        StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
                                        StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value to mClientViaDeeplink,
                                        StringConstants.INVITE_USER_ID.value to mInviteUserID,
                                        StringConstants.AUTO_REDIRECT_TO_APPL.value to true
                                )
                        )
                        navigate(R.id.Login)
                    }
                } else {
                    tv_mark_as_interest_role_details.setOnClickListener {

                        markAsInterestClick(jpApplication)
                    }

                    if (jpApplication == null) return@Observer
                    if (jpApplication.status == "")
                        tv_applied_client_activation.gone()
                    else
                        tv_applied_client_activation.visible()
                    tv_applied_client_activation.text =
                            if (jpApplication.status == "Interested" || jpApplication.status == "Inprocess") "Pending" else jpApplication.status
                    tv_applied_client_activation.setCompoundDrawablesWithIntrinsicBounds(
                            if (jpApplication.status == "Interested" || jpApplication.status == "Inprocess" || jpApplication.status == "Submitted") R.drawable.ic_status_pending else if (jpApplication.status == "Activated") R.drawable.ic_applied else R.drawable.ic_application_rejected,
                            0,
                            0,
                            0
                    )
                    setTextViewColor(
                            tv_applied_client_activation,
                            if (jpApplication.status == "Interested" || jpApplication.status == "Inprocess" || jpApplication.status == "Submitted") R.color.pending_color else if (jpApplication.status == "Activated") R.color.activated_color else R.color.rejected_color
                    )
                    var actionButtonText =
                            if (jpApplication.status == "Interested") getString(R.string.complete_application) else if (jpApplication.status == "Inprocess") getString(
                                    R.string.complete_activation
                            ) else if (jpApplication.status == "") getString(R.string.apply_now) else ""
                    if (actionButtonText == "")
                        tv_mark_as_interest_role_details.gone()
                    else
                        tv_mark_as_interest_role_details.text = actionButtonText

                }

            }


        })
        if (!viewModel.initialized)
            viewModel.getJobProfile(docID = mJobProfileId)


    }

    private fun setupPreferredLocationRv() {


        adapterPreferredLocation = AdapterPreferredLocation()
        rv_preferred_locations_client_activation.adapter = adapterPreferredLocation

        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.FLEX_START
        rv_preferred_locations_client_activation.layoutManager = layoutManager
        rv_preferred_locations_client_activation.addItemDecoration(
            HorizontaltemDecoration(
                requireContext(),
                R.dimen.size_11
            )
        )

    }


    private fun initializeLearningModule(lessons: List<Media>) {
        viewModel.observableCoursesLce.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> showLearningAsLoading()
                is Lce.Content -> showUserLearningCourses(it.content)
                is Lce.Error -> showErrorWhileLoadingCourse(it.error)
            }
        })
        viewModel.getCoursesList(lessons)


    }

    private fun showLearningAsLoading() {

        learning_cl.visible()
        learning_rv.gone()
        learning_learning_error.gone()
        learning_progress_bar.visible()
    }

    private fun showErrorWhileLoadingCourse(error: String) {

        learning_cl.visible()
        learning_progress_bar.gone()
        learning_rv.gone()
        learning_learning_error.visible()

        learning_learning_error.text = error
    }

    private fun showUserLearningCourses(content: List<LessonModel>) {

        learning_progress_bar.gone()
        learning_learning_error.gone()
        learning_rv.visible()

        if (content.isEmpty()) {
            learning_cl.gone()
        } else {
            learning_cl.visible()

            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val itemWidth = ((width / 3) * 2).toInt()
            // model will change when integrated with DB

            val recyclerGenericAdapter: RecyclerGenericAdapter<LessonModel> =
                    RecyclerGenericAdapter<LessonModel>(
                            activity?.applicationContext,
                            PFRecyclerViewAdapter.OnViewHolderClick<LessonModel> { view, position, item ->
                                if (item.type == "document") {
                                    val docIntent = Intent(
                                            requireContext(),
                                            DocViewerActivity::class.java
                                    )
                                    docIntent.putExtra(
                                            StringConstants.DOC_URL.value,
                                            item.url
                                    )
                                    startActivity(docIntent)
                                } else {
                                    if (FirebaseAuth.getInstance().currentUser?.uid == null) {
                                        PlayVideoDialogWithUrl.launch(
                                                childFragmentManager = childFragmentManager,
                                                lessonId = viewModel.observableJobProfile.value?.requiredMedia?.media?.get(
                                                        position
                                                )?.lessonId ?: "",
                                                moduleId = "",
                                                shouldShowFeedbackDialog = item.shouldShowFeedbackDialog
                                        )
                                    } else {
                                        PlayVideoDialogFragment.launch(
                                                childFragmentManager = childFragmentManager,
                                                lessonId = viewModel.observableJobProfile.value?.requiredMedia?.media?.get(
                                                        position
                                                )?.lessonId ?: "",
                                                moduleId = "",
                                                shouldShowFeedbackDialog = item.shouldShowFeedbackDialog,
                                                disableLessonCompleteAction = true
                                        )
                                    }


                                }

                            },
                            RecyclerGenericAdapter.ItemInterface<LessonModel?> { obj, viewHolder, position ->
                                val view = getView(viewHolder, R.id.card_view)
                                val lp = view.layoutParams
                                lp.height = lp.height
                                lp.width = itemWidth
                                view.layoutParams = lp

                                val title = getTextView(viewHolder, R.id.title_)
                                title.text = obj?.name

                                val subtitle = getTextView(viewHolder, R.id.title)
                                subtitle.text = obj?.description

                                val comImg = getImageView(viewHolder, R.id.completed_iv)
                                comImg.isVisible = obj?.completed ?: false

                                val img = getImageView(viewHolder, R.id.learning_img)

                                if (!obj!!.coverPicture.isNullOrBlank()) {
                                    if (obj.coverPicture!!.startsWith("http", true)) {

                                        GlideApp.with(requireContext())
                                                .load(obj.coverPicture!!)
                                                .placeholder(getCircularProgressDrawable())
                                                .error(R.drawable.ic_learning_default_back)
                                                .into(img)
                                    } else {
                                        FirebaseStorage.getInstance()
                                                .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                                                .child(obj.coverPicture!!)
                                                .downloadUrl
                                                .addOnSuccessListener { fileUri ->

                                                    GlideApp.with(requireContext())
                                                            .load(fileUri)
                                                            .placeholder(getCircularProgressDrawable())
                                                            .error(R.drawable.ic_learning_default_back)
                                                            .into(img)
                                                }
                                    }
                                } else {

                                    GlideApp.with(requireContext())
                                            .load(R.drawable.ic_learning_default_back)
                                            .into(img)
                                }

                                //img.setImageResource(obj?.imgIcon!!)
                            })!!
            recyclerGenericAdapter.list = content
            recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
            learning_rv.layoutManager = LinearLayoutManager(
                    activity?.applicationContext,
                    LinearLayoutManager.HORIZONTAL,
                    false
            )
            learning_rv.adapter = recyclerGenericAdapter

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
        outState.putBoolean(
                StringConstants.CLIENT_ACTIVATION_VIA_DEEP_LINK.value,
                mClientViaDeeplink ?: false
        )
        outState.putString(StringConstants.INVITE_USER_ID.value, mInviteUserID)
        outState.putBoolean(
                StringConstants.AUTO_REDIRECT_TO_APPL.value,
                mRedirectToApplication ?: false
        )


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
        pb_client_activation.gone()
    }

    var locationUpdates: LocationUpdates? = LocationUpdates()
    var location: Location? = null
    override fun onDestroy() {
        super.onDestroy()
        locationUpdates?.stopLocationUpdates(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        locationUpdates?.startUpdates(requireActivity() as AppCompatActivity)
        locationUpdates?.setLocationUpdateCallbacks(this)
        updateLocationCallbackIntervalIfIsInvited()

    }

    private fun updateLocationCallbackIntervalIfIsInvited() {
        if (mRedirectToApplication == true) {
            locationUpdates?.setIntervalInMillis(100)
        }
    }


    override fun locationReceiver(location: Location?) {
        this.location = location
        if (mRedirectToApplication == true) {
            tv_mark_as_interest_role_details
                    ?.let {
                        popAllBackStates()
                        it.performClick()
                        locationUpdates?.stopLocationUpdates(requireActivity())
                    }


        }
    }

    override fun lastLocationReceiver(location: Location?) {
    }

    fun markAsInterestClick(jpApplication: JpApplication?) {
        if (jpApplication == null || jpApplication.status == "" || jpApplication.status == "Interested") {
            if (mClientViaDeeplink == true) {
                if (location == null) {
                    showToast(getString(R.string.set_location_to_high_accuracy))
                    return

                }
                pb_client_activation.visible()
                viewModel.addInviteUserId(
                        mInviteUserID ?: "",
                        mJobProfileId,
                        location!!
                )


            } else {
                navigate(
                        R.id.fragment_application_client_activation, bundleOf(
                        StringConstants.JOB_PROFILE_ID.value to viewModel.observableJobProfile.value?.profileId
                )
                )
                viewModel.observableJpApplication.removeObservers(viewLifecycleOwner)
            }

        } else if (jpApplication.status == "Inprocess") {
            navigate(
                    R.id.fragment_gig_activation, bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to viewModel.observableJobProfile.value?.profileId,
                    StringConstants.NEXT_DEP.value to viewModel.observableJobProfile.value?.nextDependency
            )
            )
        }
    }

    override fun onBackPressed(): Boolean {
        if (customPowerMenu != null && customPowerMenu?.isShowing() == true) {
            customPowerMenu?.dismiss()
        }
        return super.onBackPressed()
    }


}