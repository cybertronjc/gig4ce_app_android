package com.gigforce.app.modules.landingscreen

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.toBundle
import com.gigforce.app.core.visible
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.CalendarHomeScreen
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.gigerVerfication.GigerVerificationStatus.Companion.STATUS_VERIFIED
import com.gigforce.app.modules.help.HelpVideo
import com.gigforce.app.modules.help.HelpViewModel
import com.gigforce.app.modules.landingscreen.models.Tip
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.LearningViewModel
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.AppConstants
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.StringConstants
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.landingscreen_fragment.*
import java.util.*

class LandingScreenFragment : BaseFragment() {

    companion object {
        fun newInstance() = LandingScreenFragment()
        private const val INTENT_EXTRA_SCREEN = "scrren"

        private const val SCREEN_VERIFICATION = 10
        private const val SCREEN_GIG = 11
    }

    private lateinit var viewModel: LandingScreenViewModel
    var width: Int = 0
    private var comingFromOrGoingToScreen = -1
    private val verificationViewModel: GigVerificationViewModel by viewModels()
    private val helpViewModel: HelpViewModel by viewModels()
    private val landingScreenViewModel: LandingScreenViewModel by viewModels()
    private val learningViewModel: LearningViewModel by viewModels()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        savedInstanceState?.let {
            comingFromOrGoingToScreen = it.getInt(INTENT_EXTRA_SCREEN)
        }
        return inflateView(R.layout.landingscreen_fragment, inflater, container)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(INTENT_EXTRA_SCREEN, comingFromOrGoingToScreen)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val notificationToken = FirebaseInstanceId.getInstance().getToken()
        viewModel = ViewModelProvider(this).get(LandingScreenViewModel::class.java)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        initUI()
        initializeExploreByRole()
        initializeExploreByIndustry()
        initializeLearningModule()
        listener()
        observers()
        broadcastReceiverForLanguageCahnge()
//        checkforLanguagedSelectedForLastLogin()

        when (comingFromOrGoingToScreen) {
            SCREEN_VERIFICATION -> landingScrollView.post {
                landingScrollView.scrollTo(0, exploreByIndustryLayout.y.toInt())
            }
            SCREEN_GIG -> landingScrollView.post {
                landingScrollView.scrollTo(0, exploreByIndustryLayout.y.toInt())
            }
            else -> {
            }
        }
    }

    private fun initUI() {
        about_us_cl.visibility =
            if (sharedDataInterface.getDataBoolean(StringConstants.SKIPPED_ABOUT_INTRO.value) == true
            ) View.GONE else View.VISIBLE
    }

    override fun onDetach() {
        super.onDetach()
//        LocalBroadcastManager.getInstance(activity?.applicationContext!!)
//            .unregisterReceiver(broadCastReceiver)
    }

    private fun broadcastReceiverForLanguageCahnge() {
        LocalBroadcastManager.getInstance(activity?.applicationContext!!)
            .registerReceiver(broadCastReceiver, IntentFilter(Intent.ACTION_LOCALE_CHANGED))
    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            showToast("working")
        }
    }
//    private fun checkforLanguagedSelectedForLastLogin() {
//        if (preferencesRepositoryForBaseFragment != null)
//            preferencesRepositoryForBaseFragment.getDBCollection()
//                .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
//                    var preferencesDataModel: PreferencesDataModel? =
//                        value!!.toObject(PreferencesDataModel::class.java)
//                    if (preferencesDataModel != null) {
//                        var languageCode = getAppLanguageCode()
//                        if (preferencesDataModel.languageCode == null || preferencesDataModel.languageCode.equals(
//                                ""
//                            )
//                        ) {
//                            if (languageCode != null && !languageCode.equals("")) {
//                                var languageName = getLanguageCodeToName(languageCode)
//                                preferencesRepositoryForBaseFragment.setDataAsKeyValue(
//                                    "languageName",
//                                    languageName
//                                )
//                                preferencesRepositoryForBaseFragment.setDataAsKeyValue(
//                                    "languageCode",
//                                    languageCode
//                                )
//                            }
//                        } else if (!languageCode.equals(preferencesDataModel.languageCode)) {
//                            lastLoginSelectedLanguage(
//                                preferencesDataModel.languageCode,
//                                preferencesDataModel.languageName
//                            )
//                        }
//                    }
//                })
//    }

//    private fun lastLoginSelectedLanguage(
//        lastLoginLanguageCode: String,
//        lastLoginLanguageName: String
//    ) {
//        val languageSelectionDialog = activity?.let { Dialog(it) }
//        languageSelectionDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        languageSelectionDialog?.setCancelable(false)
//        languageSelectionDialog?.setContentView(R.layout.confirmation_custom_alert_type1)
//        val titleDialog = languageSelectionDialog?.findViewById(R.id.title) as TextView
//        titleDialog.text =
//            "Your last login selected language was " + lastLoginLanguageName + ". Do you want to continue with this language?"
//        val yesBtn = languageSelectionDialog?.findViewById(R.id.yes) as TextView
//        val noBtn = languageSelectionDialog?.findViewById(R.id.cancel) as TextView
//        yesBtn.setOnClickListener {
//            saveAppLanuageCode(lastLoginLanguageCode)
//            saveAppLanguageName(lastLoginLanguageName)
//            updateResources(lastLoginLanguageCode)
//            languageSelectionDialog?.dismiss()
//        }
//        noBtn.setOnClickListener {
//            var currentLanguageCode = getAppLanguageCode()
//            if (currentLanguageCode != null) {
//                preferencesRepositoryForBaseFragment.setDataAsKeyValue(
//                    "languageName",
//                    getLanguageCodeToName(currentLanguageCode)
//                )
//
//                preferencesRepositoryForBaseFragment.setDataAsKeyValue(
//                    "languageCode",
//                    currentLanguageCode
//                )
//            }
//            languageSelectionDialog!!.dismiss()
//        }
//        languageSelectionDialog?.show()
//    }

    lateinit var viewModelProfile: ProfileViewModel
    private fun observers() {
        // load user data
        viewModelProfile = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profileObs ->
            val profile: ProfileData = profileObs!!
            displayImage(profile.profileAvatarName)
            if (profile.name != null && !profile.name.equals(""))
                profile_name.text = profile.name
        })

        verificationViewModel
            .gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                val requiredDocsVerified = it.selfieVideoDataModel?.videoPath != null
                        && it.panCardDetails?.state == STATUS_VERIFIED
                        && it.bankUploadDetailsDataModel?.state == STATUS_VERIFIED
                        && (it.aadharCardDataModel?.state == STATUS_VERIFIED || it.drivingLicenseDataModel?.state == STATUS_VERIFIED)

                val requiredDocsUploaded = it.selfieVideoDataModel?.videoPath != null
                        && it.panCardDetails?.panCardImagePath != null
                        && it.bankUploadDetailsDataModel?.passbookImagePath != null
                        && (it.aadharCardDataModel?.frontImage != null || it.drivingLicenseDataModel?.backImage != null)

                if (requiredDocsVerified) {
                    verificationTitleTV.text = getString(R.string.verification)
                    complete_now.text = getString(R.string.completed)
                } else if (requiredDocsUploaded) {
                    verificationTitleTV.text = getString(R.string.verification)
                    complete_now.text = getString(R.string.under_verification)
                } else {
                    verificationTitleTV.text = getString(R.string.complete_your_verification)
                    complete_now.text = getString(R.string.complete_now)
                }

            })

        verificationViewModel.startListeningForGigerVerificationStatusChanges()


        landingScreenViewModel
            .tips
            .observe(viewLifecycleOwner, Observer {
                setTipsOnView(it)
            })

        helpViewModel
            .helpVideos
            .observe(viewLifecycleOwner, Observer {
                setHelpVideosOnView(it)
            })

        helpViewModel.getTopHelpVideos()
    }

    private fun setTipsOnView(tips_: List<Tip>) {
        val tips = tips_.filter { item ->
            !sharedDataInterface.getDataBoolean(item.tip_id.toString())!!
        }.toMutableList()
        if (tips.isEmpty()) {
            gigforce_tip.gone()
        } else {
            gigforce_tip.visible()

            var recyclerGenericAdapter: RecyclerGenericAdapter<Tip>? = null
            recyclerGenericAdapter =
                RecyclerGenericAdapter<Tip>(
                    activity?.applicationContext,
                    null,
                    RecyclerGenericAdapter.ItemInterface<Tip?> { obj, viewHolder, position ->
                        var title = getTextView(viewHolder, R.id.gigtip_title)
                        var subtitle = getTextView(viewHolder, R.id.gigtip_subtitle)

                        val lp = title.layoutParams
                        lp.height = lp.height
                        lp.width = width
                        title.layoutParams = lp
                        title.text = obj?.title
                        subtitle.text = obj?.subTitle
                        getView(viewHolder, R.id.textView102).setOnClickListener {
                            val tip = tips.get(viewHolder.adapterPosition)
                            navigate(
                                resId = tip.whereToRedirect,
                                args = tip.intentExtraMap.toBundle()
                            )
                        }

                        getView(viewHolder, R.id.skip).setOnClickListener {
                            if (viewHolder.adapterPosition == -1) return@setOnClickListener
                            sharedDataInterface.saveDataBoolean(obj?.tip_id.toString(), true)
                            tips.removeAt(viewHolder.adapterPosition)
                            recyclerGenericAdapter?.notifyItemRemoved(viewHolder.adapterPosition)
                            if (tips.isEmpty()) {
                                gigforce_tip.gone()
                            }

                        }


//                    getTextView(viewHolder, R.id.skip).setOnClickListener{
//                        datalist.removeAt(position)
//                        gigforce_tip.adapter?.notifyItemChanged(position+1)
//                    }
                    })!!
            recyclerGenericAdapter.setList(tips)
            recyclerGenericAdapter.setLayout(R.layout.gigforce_tips_item)
            gigforce_tip.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            gigforce_tip.adapter = recyclerGenericAdapter
            var pagerHelper = PagerSnapHelper()
            pagerHelper.attachToRecyclerView(gigforce_tip)
            var handler = Handler()
//        val runnable = Runnable {
//            var currentVisiblePosition = (gigforce_tip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
//            gigforce_tip.scrollToPosition(currentVisiblePosition+1)
//            handler.postDelayed(runnable,SPLASH_TIME_OUT)
//        }

            val runnableCode = object : Runnable {
                override fun run() {
                    try {
                        var currentVisiblePosition =
                            (gigforce_tip.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                        if ((gigforce_tip.adapter as RecyclerGenericAdapter<TitleSubtitleModel>).list.size == currentVisiblePosition + 1) {
                            forward = false
                        }
                        if (currentVisiblePosition == 0) {
                            forward = true
                        }
                        if (!forward) {
                            gigforce_tip.smoothScrollToPosition(currentVisiblePosition - 1)
                        } else
                            gigforce_tip.smoothScrollToPosition(currentVisiblePosition + 1)


                        handler.postDelayed(this, SPLASH_TIME_OUT)
                    } catch (e: Exception) {

                    }

                }
            }
            handler.postDelayed(runnableCode, SPLASH_TIME_OUT)
        }
    }

    inner class SkipClickListener(val rv: RecyclerView, var position: Int) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            gigforce_tip.gone()
        }
    }

    private fun setHelpVideosOnView(helpVideos: List<HelpVideo>?) {

        val recyclerGenericAdapter: RecyclerGenericAdapter<HelpVideo> =
            RecyclerGenericAdapter<HelpVideo>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    val id = (item as HelpVideo).videoYoutubeId
                    playVideo(id)
                },
                RecyclerGenericAdapter.ItemInterface<HelpVideo?> { obj, viewHolder, position ->

                    var iconIV = getImageView(viewHolder, R.id.help_first_card_img)
                    Glide.with(requireContext()).load(obj?.getThumbNailUrl())
                        .placeholder(getCircularProgressDrawable()).into(iconIV)

                    var titleTV = getTextView(viewHolder, R.id.titleTV)
                    titleTV.text = obj?.videoTitle

                    var timeTV = getTextView(viewHolder, R.id.time_text)
                    timeTV.text = if (obj!!.videoLength >= 60) {
                        val minutes = obj!!.videoLength / 60
                        val secs = obj!!.videoLength % 60
                        "$minutes:$secs"
                    } else {
                        "00:${obj.videoLength}"
                    }


//                    var img = getImageView(viewHolder, R.id.learning_img)
//                    img.setImageResource(obj?.imgIcon!!)
                })!!
        recyclerGenericAdapter.setList(helpVideos)
        recyclerGenericAdapter.setLayout(R.layout.item_help_video)
        helpVideoRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        helpVideoRV.adapter = recyclerGenericAdapter
    }

    fun playVideo(id: String) {
        val appIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$id")
        )
        try {
            requireContext().startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            requireContext().startActivity(webIntent)
        }
    }

    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            if (profile_image != null)
                GlideApp.with(this.requireContext())
                    .load(profilePicRef)
                    .apply(RequestOptions().circleCrop())
                    .into(profile_image)
        } else {
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }

    class TitleSubtitleModel(var title: String, var subtitle: String, var imgStr: String) {

    }

    private val SPLASH_TIME_OUT: Long = 10_000 // 10 sec
    var forward = true


    private fun listener() {
        complete_now.setOnClickListener {
            comingFromOrGoingToScreen = SCREEN_VERIFICATION
            navigate(R.id.gigerVerificationFragment)
        }
        mygigs_cl.setOnClickListener {
            comingFromOrGoingToScreen = SCREEN_GIG
            CalendarHomeScreen.fistvisibleItemOnclick = -1
            navigate(R.id.mainHomeScreen)
        }
        skip_about_intro.setOnClickListener {
            sharedDataInterface.saveDataBoolean(StringConstants.SKIPPED_ABOUT_INTRO.value, true)
            about_us_cl.visibility = View.GONE
        }
        chat_icon_iv.setOnClickListener {
//            navigate(R.id.fakeGigContactScreenFragment)
        }

        contact_us.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }

        invite_contact.setOnClickListener {
            navigate(R.id.referrals_fragment)
        }

        profile_image.setOnClickListener {
            navigate(R.id.profileFragment)
        }

        textView119.setOnClickListener {
            navigate(R.id.settingFragment)
        }

        seeMoreBtn.setOnClickListener {
            navigate(R.id.helpVideosFragment)
        }
        help_topic.setOnClickListener {
            navigate(R.id.helpVideosFragment)
        }

        gigforce_video.setOnClickListener {
            playVideo("FbiyRe49wjY")
        }
        ll_search_role.setOnClickListener {
            if (AppConstants.UNLOCK_FEATURE) {
                navigate(R.id.fragment_explore_by_role)
            } else {
                showToast("This is under development. Please check again in a few days.")
            }
        }
    }

    private fun initializeLearningModule() {

        learningViewModel
            .roleBasedCourses
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showLearningAsLoading()
                    is Lce.Content -> showUserLearningCourses(it.content)
                    is Lce.Error -> showErrorWhileLoadingCourse(it.error)
                }
            })


        learningViewModel.getRoleBasedCourses()
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

    private fun showUserLearningCourses(content: List<Course>) {

        learning_progress_bar.gone()
        learning_learning_error.gone()
        learning_rv.visible()

        if (content.isEmpty()) {
            learning_cl.gone()
        } else {
            learning_cl.visible()

            val itemWidth = ((width / 3) * 2).toInt()
            // model will change when integrated with DB

            val recyclerGenericAdapter: RecyclerGenericAdapter<Course> =
                RecyclerGenericAdapter<Course>(
                    activity?.applicationContext,
                    PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                        navigate(R.id.mainLearningFragment)
                    },
                    RecyclerGenericAdapter.ItemInterface<Course?> { obj, viewHolder, position ->
                        var view = getView(viewHolder, R.id.card_view)
                        val lp = view.layoutParams
                        lp.height = lp.height
                        lp.width = itemWidth
                        view.layoutParams = lp

                        var title = getTextView(viewHolder, R.id.title_)
                        title.text = obj?.name

                        var subtitle = getTextView(viewHolder, R.id.title)
                        subtitle.text = obj?.level

                        var comImg = getImageView(viewHolder, R.id.completed_iv)
                        comImg.isVisible = obj?.completed ?: false

                        var img = getImageView(viewHolder, R.id.learning_img)

                        if (!obj!!.coverPicture.isNullOrBlank()) {
                            if (obj!!.coverPicture!!.startsWith("http", true)) {

                                GlideApp.with(requireContext())
                                    .load(obj!!.coverPicture!!)
                                    .placeholder(getCircularProgressDrawable())
                                    .error(R.drawable.ic_learning_default_back)
                                    .into(img)
                            } else {
                                FirebaseStorage.getInstance()
                                    .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                                    .child(obj!!.coverPicture!!)
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
            recyclerGenericAdapter.setList(content)
            recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
            learning_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            learning_rv.adapter = recyclerGenericAdapter

        }
    }

    private fun showGlideImage(url: String, imgview: ImageView) {
        GlideApp.with(requireContext())
            .load(url)
            .placeholder(getCircularProgressDrawable())
            .into(imgview)
    }

    private fun initializeExploreByIndustry() {

        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
//        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()

        datalist.add(
            TitleSubtitleModel(
                "Delivery",
                "",
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry.jpg?alt=media&token=039ddf50-9597-4ee4-bc12-0abdea74fd16"
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Retail",
                "",
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry3.jpg?alt=media&token=1813f5dd-5596-4a04-a0e1-3c8400a3d82d"
            )
        )


        datalist.add(
            TitleSubtitleModel(
                "Quick Service Restuarant",
                "",
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry1.jpg?alt=media&token=2634019b-9777-4dbb-9103-1d63eb44df97"
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Telesales and Support",
                "",
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry2.jpg?alt=media&token=00412b0a-fbbe-4790-9a9b-050fefaf5d02"
            )
        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    //                    if(AppConstants.UNLOCK_FEATURE){
//                    }else
                    showToast("This is under development. Please check again in a few days.")
                },
                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title)
                    title.text = obj?.title
                    obj?.imgStr?.let {
                        var img = getImageView(viewHolder, R.id.img_view)
                        showGlideImage(it, img)
                    }
//                    img.setImageResource(obj?.imgIcon!!)
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.explore_by_industry_item)
        explore_by_industry.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        explore_by_industry.adapter = recyclerGenericAdapter
    }

    private fun initializeExploreByRole() {
        landingScreenViewModel.observerRole.observe(viewLifecycleOwner, Observer { gig ->
            run {
                showGlideImage(gig?.role_image ?: "", iv_role)
                tv_title_role.text = gig?.role_title
                if (!gig?.job_description.isNullOrEmpty()) {
                    tv_subtitle_role.visible()
                    tv_subtitle_role.text = gig?.job_description?.get(0)
                }
                cv_role.setOnClickListener {
                    if (AppConstants.UNLOCK_FEATURE) {
                        findNavController().navigate(
                            LandingScreenFragmentDirections.openRoleDetailsHome(
                                gig?.id!!
                            )
                        )
                    } else {
                        showToast("This is under development. Please check again in a few days.")
                    }
                }
            }


        })
        landingScreenViewModel.getRoles()
        val itemWidth = ((width / 3) * 2).toInt()
        val lp = cv_role.layoutParams
        lp.height = lp.height
        lp.width = itemWidth

        cv_role.layoutParams = lp
    }


}