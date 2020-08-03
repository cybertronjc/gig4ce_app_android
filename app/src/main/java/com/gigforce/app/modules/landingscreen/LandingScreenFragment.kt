package com.gigforce.app.modules.landingscreen

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet.UpcomingGigModel
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
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
        initializeGigforceTip()
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
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile.profileAvatarName)
            if (profile.name != null && !profile.name.equals(""))
                profile_name.text = profile.name
        })

        verificationViewModel
            .gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                if(it.everyDocumentUploaded){
                    complete_now.text = getString(R.string.completed)
                    title.text = getString(R.string.verification)
                }else{
                    complete_now.text = getString(R.string.complete_now)
                    title.text = getString(R.string.complete_your_verification)
                }

        })
    }

    private fun displayImage(profileImg: String) {
        if (profileImg != null && !profileImg.equals("")) {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image)
        }
    }

    class TitleSubtitleModel(var title: String, var subtitle: String, var imgIcon: Int = 0) {

    }

    private val SPLASH_TIME_OUT: Long = 2000 // 1 sec
    var forward = true
    private fun initializeGigforceTip() {
        // model will change when integrated with DB
        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()

        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Your education details help build your profile."
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Your work experience helps find similar gigs for you."
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Adding your skills helps recommend suitable gigs."
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Adding a profile photo shows off your personality."
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "How many languages can you speak in?"
            )
        )


        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Tell me 2 lines that best describe your."
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Sharing your past achievements highlights your profile."
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Add your permanent address to complete verification?"
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Have you shared where you are looking for work?"
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "What are your preferred areas to work from?"
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "How far are you willing to travel for work daily?"
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "What is your daily earning expectation?"
            )
        )


        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "How many days during the week are you willing to work?"
            )
        )
        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Are you willing to work during the weekends?"
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Gigforce Tip ",
                "Would you want to work from home?"
            )
        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigate(R.id.explore_by_role)
                },
                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel?> { obj, viewHolder, position ->
                    var title = getTextView(viewHolder, R.id.gigtip_title)
                    var subtitle = getTextView(viewHolder, R.id.gigtip_subtitle)

                    val lp = title.layoutParams
                    lp.height = lp.height
                    lp.width = width
                    title.layoutParams = lp
                    title.text = obj?.title
                    subtitle.text = obj?.subtitle

//                    getTextView(viewHolder, R.id.skip).setOnClickListener{
//                        datalist.removeAt(position)
//                        gigforce_tip.adapter?.notifyItemChanged(position+1)
//                    }
                })!!
        recyclerGenericAdapter.setList(datalist)
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

    private fun listener() {
        complete_now.setOnClickListener {
            comingFromOrGoingToScreen = SCREEN_VERIFICATION
            navigate(R.id.gigerVerificationFragment)
        }
        mygigs_cl.setOnClickListener {
            comingFromOrGoingToScreen = SCREEN_GIG
            navigate(R.id.mainHomeScreen)
        }
        skip_about_intro.setOnClickListener {
            about_us_cl.visibility = View.GONE
        }
        chat_icon_iv.setOnClickListener {
            navigate(R.id.contactScreenFragment)
        }

        profile_image.setOnClickListener {
            navigate(R.id.profileFragment)
        }

        textView119.setOnClickListener {
            navigate(R.id.settingFragment)
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
                    navigate(R.id.mainLearningFragment)
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
                    navigate(R.id.explore_by_role)
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