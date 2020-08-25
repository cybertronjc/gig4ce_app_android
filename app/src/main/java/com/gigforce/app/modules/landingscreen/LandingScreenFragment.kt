package com.gigforce.app.modules.landingscreen

import android.app.Dialog
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
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
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.AboutExpandedFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.landingscreen_fragment.*
import kotlinx.android.synthetic.main.landingscreen_fragment.chat_icon_iv
import java.util.ArrayList

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
    private val verificationViewModel : GigVerificationViewModel by viewModels()
    private val helpViewModel : HelpViewModel by viewModels()
    private val landingScreenViewModel : LandingScreenViewModel by viewModels()

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
        outState.putInt(INTENT_EXTRA_SCREEN,comingFromOrGoingToScreen)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LandingScreenViewModel::class.java)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
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

    private fun setTipsOnView(tips: List<Tip>) {

        if(tips.isEmpty()){
            gigforce_tip.gone()
        }else {
            gigforce_tip.visible()

            val recyclerGenericAdapter: RecyclerGenericAdapter<Tip> =
                RecyclerGenericAdapter<Tip>(
                    activity?.applicationContext,
                    PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                        val tip = (item as Tip)
                        navigate(
                            resId = tip.whereToRedirect,
                            args = tip.intentExtraMap.toBundle()
                        )


                    },
                    RecyclerGenericAdapter.ItemInterface<Tip?> { obj, viewHolder, position ->
                        var title = getTextView(viewHolder, R.id.gigtip_title)
                        var subtitle = getTextView(viewHolder, R.id.gigtip_subtitle)

                        val lp = title.layoutParams
                        lp.height = lp.height
                        lp.width = width
                        title.layoutParams = lp
                        title.text = obj?.title
                        subtitle.text = obj?.subTitle

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
                    Glide.with(requireContext()).load(obj?.getThumbNailUrl()).placeholder(getCircularProgressDrawable()).into(iconIV)

                    var titleTV = getTextView(viewHolder, R.id.titleTV)
                    titleTV.text = obj?.videoTitle

                    var timeTV = getTextView(viewHolder, R.id.time_text)
                    timeTV.text = if(obj!!.videoLength >= 60){
                            val minutes = obj!!.videoLength / 60
                            val secs = obj!!.videoLength % 60
                           "$minutes:$secs"
                     }else{
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

    fun playVideo( id : String){
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
            if(profile_image!=null)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }else{
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }

    class TitleSubtitleModel(var title: String, var subtitle: String, var imgIcon: Int = 0) {

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
            about_us_cl.visibility = View.GONE
        }
        chat_icon_iv.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }

        contact_us.setOnClickListener {
            navigate(R.id.fakeGigContactScreenFragment)
        }

        invite_contact.setOnClickListener {
            showToast("This is not functional, Please check later")
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
        help_topic.setOnClickListener{
            showToast("This is under development. Please check again in a few days.")
        }

        gigforce_video.setOnClickListener{
            playVideo("FbiyRe49wjY")
        }
    }

    private fun initializeLearningModule() {

        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()

        datalist.add(
            TitleSubtitleModel(
                "Retail Sales Executive",
                "Demonstrate products to customers", R.drawable.learning2
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Quick Service Restaurant",
                "Manage food displays",
                R.drawable.learning1
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Delivery",
                "Maintaining hygiene and safety",
                R.drawable.learning_bg
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Retail Sales executive",
                "Help customers choose the right products",
                R.drawable.learning2
            )
        )
        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    navigate(R.id.mainLearningFragment)
                    showToast("This is work in progress. Please check again in a few days")
                },
                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title_)
                    title.text = obj?.title

                    var subtitle = getTextView(viewHolder, R.id.title)
                    subtitle.text = obj?.subtitle

                    var img = getImageView(viewHolder, R.id.learning_img)
                    img.setImageResource(obj?.imgIcon!!)
                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        learning_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_rv.adapter = recyclerGenericAdapter
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
                R.drawable.industry
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Retail",
                "",
                R.drawable.industry3
            )
        )


        datalist.add(
            TitleSubtitleModel(
                "Quick Service Restuarant",
                "",
                R.drawable.industry1
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Telesales and Support",
                "",
                R.drawable.industry2
            )
        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
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

                    var img = getImageView(viewHolder, R.id.img_view)
                    img.setImageResource(obj?.imgIcon!!)
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
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB
//        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()


        datalist.add(
            TitleSubtitleModel(
                "Driver",
                "Welcome to Gigforce! Let's talk about what's a gig and how do you start working as a giger at Gigforce.",
                R.drawable.driver_img
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Delivery Executive",
                "Welcome to Gigforce! Let's talk about what's a gig and how do you start working as a giger at Gigforce.",
                R.drawable.delivery_executive_ls_img
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Retail Sales Executive",
                "Welcome to Gigforce! Let's talk about what's a gig and how do you start working as a giger at Gigforce.",
                R.drawable.retail_img_ls
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Barista",
                "Welcome to Gigforce! Let's talk about what's a gig and how do you start working as a giger at Gigforce.",
                R.drawable.brista_ls_img
            )
        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    showToast("This is under development. Please check again in a few days.")
//                    navigate(R.id.explore_by_role)
                },
                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title)
                    title.text = obj?.title

                    var img = getImageView(viewHolder, R.id.img_view)
                    img.setImageResource(obj?.imgIcon!!)

                })!!
        recyclerGenericAdapter.setList(datalist)
        recyclerGenericAdapter.setLayout(R.layout.explore_by_role_item)
        explore_by_role_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        explore_by_role_rv.adapter = recyclerGenericAdapter

    }
}