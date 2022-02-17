package com.gigforce.giger_app.calendarscreen.maincalendarscreen


import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.gigforce.common_ui.AppDialogsInterface
import com.gigforce.common_ui.ConfirmationDialogOnClickListener
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.chat.ChatHeadersViewModel
import com.gigforce.common_ui.configrepository.ConfigRepository
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.utils.BsBackgroundAndLocationAccess
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.common_ui.viewmodels.custom_gig_preferences.CustomPreferencesViewModel
import com.gigforce.common_ui.viewmodels.custom_gig_preferences.ParamCustPreferViewModel
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.datamodels.custom_gig_preferences.UnavailableDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.giger_app.R
import com.gigforce.giger_app.calendarscreen.maincalendarscreen.verticalcalendar.CalendarRecyclerItemTouchHelper
import com.gigforce.giger_app.calendarscreen.maincalendarscreen.verticalcalendar.VerticalCalendarDataItemModel
import com.gigforce.giger_app.components.CalendarView
import com.gigforce.giger_app.roster.RosterDayFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jaeger.library.StatusBarUtil
import com.riningan.widget.ExtendedBottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.calendar_home_screen.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CalendarHomeScreen : Fragment(),
    CalendarRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private val locationAccessDialog: BsBackgroundAndLocationAccess by lazy {
        BsBackgroundAndLocationAccess()
    }

    companion object {
        fun newInstance() =
            CalendarHomeScreen()

        lateinit var temporaryData: VerticalCalendarDataItemModel
        var fistvisibleItemOnclick = -1
    }

    var swipedToupdateGig = false

    @Inject
    lateinit var eventTracker: IEventTracker


    lateinit var selectedMonthModel: CalendarView.MonthModel

    private val gigViewModel: GigViewModel by viewModels()
    private val chatHeadersViewModel: ChatHeadersViewModel by viewModels()

    lateinit var arrCalendarDependent: Array<View>
    private var mExtendedBottomSheetBehavior: ExtendedBottomSheetBehavior<*>? = null
    private val viewModel: CalendarHomeScreenViewModel by viewModels()
    lateinit var viewModelProfile: ProfileViewModel
    lateinit var viewModelCustomPreference: CustomPreferencesViewModel
    var width: Int = 0

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    @Inject
    lateinit var appDialogsInterface: AppDialogsInterface

    @Inject
    lateinit var leadManagementRepository: LeadManagementRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.calendar_home_screen, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // checkForLocationPermission()
        checkForPendingJoining()

    }
    private fun verificationObserver() {
        viewModel.bankDetailedObject.observe(viewLifecycleOwner, Observer {
            it.status?.let {
                when(it){
                    "verification_pending" -> {
                        navigation.navigateTo("verification/bankdetailconfirmationbottomsheet")
                    }
                }
            }


        })
    }
    private fun checkForPendingJoining() = lifecycleScope.launch {
        try {
            leadManagementRepository.getPendingJoinings().apply {
                if (isNotEmpty()) {

                    first().let {
                        navigation.navigateTo(
                            "LeadMgmt/PendingJoiningDetails",
                            bundleOf(
                                "joining_id" to it.joiningId
                            ),
                            NavigationOptions.getNavOptions()
                        )
                    }
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verificationObserver()
        checkForDeepLink()
        viewModelProfile = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModelCustomPreference =
            ViewModelProvider(this, ParamCustPreferViewModel(viewLifecycleOwner)).get(
                CustomPreferencesViewModel::class.java
            )

        ConfigRepository().getForceUpdateCurrentVersion(object :
            ConfigRepository.LatestAPPUpdateListener {
            override fun getCurrentAPPVersion(latestAPPUpdateModel: ConfigRepository.LatestAPPUpdateModel) {
                if (latestAPPUpdateModel.active && isNotLatestVersion(latestAPPUpdateModel))
                    appDialogsInterface.showConfirmationDialogType3(
                        getString(R.string.new_version_available_app_giger),
                        getString(R.string.new_version_available_detail_app_giger),
                        getString(R.string.update_now_app_giger),
                        getString(R.string.cancel_update_app_giger),
                        object :
                            ConfirmationDialogOnClickListener {
                            override fun clickedOnYes(dialog: Dialog?) {
                                redirectToStore("https://play.google.com/store/apps/details?id=com.gigforce.app")
                            }

                            override fun clickedOnNo(dialog: Dialog?) {
                                if (latestAPPUpdateModel.force_update_required)
                                    activity?.finish()
                                dialog?.dismiss()
                            }

                        })
            }
        })
        arrCalendarDependent =
            arrayOf(calendar_dependent, calendar_cv, bottom_sheet_top_shadow, oval_gradient_iv1)
//            arrayOf(calendar_dependent, calendar_cv, bottom_sheet_top_shadow)

        selectedMonthModel = CalendarView.MonthModel(Calendar.getInstance().get(Calendar.MONTH))
        initializeViews()
        listener()
        observers()

    }

    private fun checkForLocationPermission() {
        val locationPermissionGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }

        if (!locationPermissionGranted) {
            showLocationDialog()
        }

    }

    private fun checkForDeepLink() {
        try {

            when {
                sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_login") ?: false -> {
                    navigation.navigateTo(
                        "gig/tlLoginDetails", bundleOf(
                            StringConstants.CAME_FROM_LOGIN_SUMMARY_DEEPLINK.value to true
                        )
                    )
                }
                sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_onboarding") ?: false -> {
                    navigation.navigateTo(
                        "LeadMgmt/joiningListFragment", bundleOf(
                            StringConstants.CAME_FROM_ONBOARDING_FORM_DEEPLINK.value to true
                        )
                    )
                }

                sharedPreAndCommonUtilInterface.getDataBoolean(StringConstants.BANK_DETAIL_SP.value)
                    ?: false -> {
                    navigation.navigateTo("verification/bank_account_fragment")
                }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                    StringConstants.PAN_CARD_SP.value
                ) ?: false -> {
                    navigation.navigateTo("verification/pancardimageupload")

                }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                    StringConstants.AADHAR_DETAIL_SP.value
                ) ?: false -> {
                    navigation.navigateTo("verification/AadharDetailInfoFragment")

                }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                    StringConstants.DRIVING_LICENCE_SP.value
                ) ?: false -> {
                    navigation.navigateTo("verification/drivinglicenseimageupload")
                }

                sharedPreAndCommonUtilInterface.getDataBoolean(
                    StringConstants.VERIFICATION_SP.value
                ) ?: false -> {
                    navigation.navigateTo("verification/main")

                }
            }

//            val cameFromLoginDeepLink = sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_login")
//            val cameFromOnboardingDeepLink = sharedPreAndCommonUtilInterface.getDataBoolean("deeplink_onboarding")
//            if (cameFromLoginDeepLink == true){
//                Log.d("deepLink", "here")
//                navigation.navigateTo("gig/tlLoginDetails", bundleOf(
//                    StringConstants.CAME_FROM_LOGIN_SUMMARY_DEEPLINK.value to true
//                )
//                )
//            }else if (cameFromOnboardingDeepLink == true){
//                Log.d("deepLink", "onboarding")
//                navigation.navigateTo("LeadMgmt/joiningListFragment", bundleOf(
//                    StringConstants.CAME_FROM_ONBOARDING_FORM_DEEPLINK.value to true
//                )
//                )
//            }


        } catch (e: Exception) {

        }
    }

    private fun showLocationDialog() {
        if (locationAccessDialog.dialog == null || locationAccessDialog.dialog?.isShowing == false) {
            locationAccessDialog.isCancelable = false
            locationAccessDialog.show(
                childFragmentManager,
                BsBackgroundAndLocationAccess::class.simpleName
            )
        }
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.white, null)
        )
        trySyncingUnsyncedFirebaseData()
        viewModel.state?.let { parcelable ->
            rv_?.layoutManager?.onRestoreInstanceState(parcelable)

        }
    }
    override fun onStop() {
        super.onStop()
        viewModel.state =
            rv_?.layoutManager?.onSaveInstanceState()
    }
    private fun trySyncingUnsyncedFirebaseData() {
        FirebaseFirestore
            .getInstance()
            .waitForPendingWrites()
            .addOnSuccessListener {
                Log.d(
                    "CalendarHomeScreen",
                    "Success no pending writes found"
                )
                CrashlyticsLogger.d(
                    "CalendarHomeScreen",
                    "Success no pending writes found"
                )
            }.addOnFailureListener {
                Log.e(
                    "CalendarHomeScreen",
                    "while syncning data to server",
                    it
                )
                CrashlyticsLogger.e(
                    "CalendarHomeScreen",
                    "while syncning data to server",
                    it
                )
            }
    }


    private fun isNotLatestVersion(latestAPPUpdateModel: ConfigRepository.LatestAPPUpdateModel): Boolean {
        try {
            var currentAppVersion = getAppVersion()
            if (currentAppVersion.contains("Dev")) {
                currentAppVersion = currentAppVersion.split("-")[0]
            }
            val appVersion = currentAppVersion.split(".").toTypedArray()
            val serverAPPVersion =
                latestAPPUpdateModel.force_update_current_version.split(".").toTypedArray()
            if (appVersion.size == 0 || serverAPPVersion.size == 0) {
                FirebaseCrashlytics.getInstance()
                    .log("isNotLatestVersion method : appVersion or serverAPPVersion has zero size!!")
                return false
            } else {
                if (appVersion.get(0).toInt() < serverAPPVersion.get(0).toInt()) {
                    return true
                } else if (appVersion.get(0).toInt() == serverAPPVersion.get(0)
                        .toInt() && appVersion.get(1).toInt() < serverAPPVersion.get(1).toInt()
                ) {
                    return true
                } else return appVersion.get(0).toInt() == serverAPPVersion.get(0)
                    .toInt() && appVersion.get(1).toInt() == serverAPPVersion.get(1)
                    .toInt() && appVersion.get(2).toInt() < serverAPPVersion.get(2).toInt()

            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("isNotLatestVersion Method Exception")

            return false
        }
    }

    fun getAppVersion(): String {
        var result = ""

        try {
            result = context?.packageManager
                ?.getPackageInfo(context?.packageName!!, 0)
                ?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {

        }

        return result
    }

    fun redirectToStore(playStoreUrl: String) {
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun initializeViews() {
        initializeExtendedBottomSheet()
        initializeMonthTV(Calendar.getInstance(), true)
        initializeVerticalCalendarRV()

    }

    private fun initializeExtendedBottomSheet() {
        mExtendedBottomSheetBehavior = ExtendedBottomSheetBehavior.from(nsv)

        Log.d("BottomSheetState ", "init  : ${viewModel.currentBottomSheetState}")
        mExtendedBottomSheetBehavior?.setBottomSheetCallback(BottomSheetExpansionListener())
        mExtendedBottomSheetBehavior?.state = viewModel.currentBottomSheetState
        mExtendedBottomSheetBehavior?.isAllowUserDragging = true
    }

    private fun listener() {
        cardView.setOnClickListener(View.OnClickListener { navigation.navigateTo("profile")/*navigate(R.id.profileFragment)*/ })
//        tv_hs1bs_alert.setOnClickListener(View.OnClickListener { navigate(R.id.verification) })
        chat_icon_iv.setOnClickListener {
            navigation.navigateTo("chats/chatList")
//            navigate(R.id.chatListFragment)
        }
        month_year.setOnClickListener(View.OnClickListener {
            changeVisibilityCalendarView()
        })
        bottom_sheet_shadow_view.setOnClickListener {
            changeVisibilityCalendarView()
        }
        calendar_dependent.setOnClickListener {
            changeVisibilityCalendarView()
        }
        month_selector_arrow.setOnClickListener {
            changeVisibilityCalendarView()
        }
        date_container.setOnClickListener {
            changeVisibilityCalendarView()
        }
        oval_gradient_iv.setOnClickListener {
            changeVisibilityCalendarView()
        }
        calendarView.setMonthChangeListener(object :
            CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
                selectedMonthModel = monthModel
                var calendar = Calendar.getInstance()
                calendar.set(Calendar.MONTH, monthModel.currentMonth)
                calendar.set(Calendar.YEAR, monthModel.year)
                calendar.set(Calendar.DATE, 1)
                initializeMonthTV(calendar, false)
                if (!isLoading) {
                    recyclerGenericAdapter.list.addAll(
                        viewModel.getVerticalCalendarData(
                            recyclerGenericAdapter.list.get(recyclerGenericAdapter.list.size - 1),
                            false
                        )
                    )
                }
            }

        })
        calendarView.setOnDateClickListner(object : CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
                selectedMonthModel = monthModel
                println(" date data1 " + selectedMonthModel.toString())
                changeVisibilityCalendarView()
            }
        })

    }

    private fun changeVisibilityCalendarView() {
        var extendedBottomSheetBehavior: ExtendedBottomSheetBehavior<NestedScrollView> =
            ExtendedBottomSheetBehavior.from(nsv)

        if (extendedBottomSheetBehavior.isAllowUserDragging) {
            hideDependentViews(false)
            Log.d(
                "BottomSheetState ",
                "changeVisibilityCalendarView  : ${viewModel.currentBottomSheetState}"
            )
            extendedBottomSheetBehavior.state =
                ExtendedBottomSheetBehavior.STATE_COLLAPSED//viewModel.currentBottomSheetState
            extendedBottomSheetBehavior.isAllowUserDragging = false
        } else {
            if (selectedMonthModel.days != null && selectedMonthModel.days.size == 1) {
                scrollVerticalCalendarToSelectedDate()
            } else {
                scrollVerticalCalendarToSelectedMonth()
            }
            hideDependentViews(true)
            extendedBottomSheetBehavior.isAllowUserDragging = true
        }
    }

    private fun scrollVerticalCalendarToSelectedDate() {
        var layoutManager: LinearLayoutManager? = null
        if (layoutManager == null) {
            layoutManager = rv_.layoutManager as LinearLayoutManager
        }
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
        var dayModel = selectedMonthModel.days.get(0)
        if (recyclerGenericAdapter.list.get(firstVisibleItem).year < dayModel.year || (recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).year == dayModel.year && recyclerGenericAdapter.list.get(firstVisibleItem).month < dayModel.month) || (recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).year == dayModel.year && recyclerGenericAdapter.list.get(firstVisibleItem).month == dayModel.month && recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).date < dayModel.date)
        ) {
            for (index in firstVisibleItem..recyclerGenericAdapter.list.size) {
                if (recyclerGenericAdapter.list.get(index).year == dayModel.year && recyclerGenericAdapter.list.get(
                        index
                    ).month == dayModel.month && recyclerGenericAdapter.list.get(
                        index
                    ).date == dayModel.date
                ) {
                    (rv_.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        index,
                        0
                    )
                    break
                }
            }
        } else {
            for (index in 0..firstVisibleItem) {
                if (recyclerGenericAdapter.list.get(index).year == dayModel.year && recyclerGenericAdapter.list.get(
                        index
                    ).month == dayModel.month && recyclerGenericAdapter.list.get(index).date == dayModel.date
                ) {
                    (rv_.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        index,
                        0
                    )
                    break
                }
            }
        }
    }


    private fun hideDependentViews(hide: Boolean) {
        for (view in arrCalendarDependent) {
            if (hide)
                view.gone()
            else view.visible()
        }

    }

    private fun observers() {
        viewModel.mainHomeLiveDataModel.observe(viewLifecycleOwner, Observer { homeDataModel ->
            if (homeDataModel != null) {
//                viewModel.setDataModel(homeDataModel.all_gigs)
                initializeViews()
                calendarView.setGigData(viewModel.arrMainHomeDataModel!!)
            }
        })

        chatHeadersViewModel.unreadMessageCount
            .observe(viewLifecycleOwner, Observer {

                if (it == 0) {
                    unread_message_count_tv.setImageDrawable(null)
                } else {
                    val drawable = TextDrawable.builder().buildRound(
                        it.toString(),
                        ResourcesCompat.getColor(requireContext().resources, R.color.lipstick, null)
                    )
                    unread_message_count_tv.setImageDrawable(drawable)
                }
            })
        chatHeadersViewModel.startWatchingChatHeaders()


        // load user data
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile.profileAvatarName)
            if (profile.name != null && !profile.name.equals("")) {
                tv1HS1.text = profile.name

                //setting user's name to mixpanel
                var props = HashMap<String, Any>()
                props.put("name", profile.name)
                eventTracker.setProfileProperty(ProfilePropArgs("\$name", profile.name))
                eventTracker.setUserName(profile.name)
                Log.d("name", profile.name)
            }
        })
        viewModelCustomPreference.customPreferencesLiveDataModel.observe(
            viewLifecycleOwner,
            Observer { data ->

                viewModel.customPreferenceUnavailableData = data.unavailable
                viewModelCustomPreference.getCustomPreferenceData()?.let {
                    //viewModel.setCustomPreferenceData(it)
                    viewModel.customPreferenceUnavailableData = data.unavailable
                    if (swipedToupdateGig) {
//                    swipedToupdateGig = false
                    } else
                        initializeViews()
                }

            })

        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            if (preferenceData != null) {
                viewModel.setPreferenceDataModel(preferenceData)
                initializeViews()
            }
        })

        gigViewModel.todaysGigs.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer

            when (it) {
                Lce.Loading -> {
                }
                is Lce.Content -> {
                    showTodaysGigDialog(it.content.size)
                }
                is Lce.Error -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_app_giger))
                        .setMessage(getString(R.string.unable_to_fetch_todays_gig_app_giger) + it.error)
                        .setPositiveButton(getString(R.string.okay_app_giger)) { _, _ -> }
                        .show()
                }
            }
        })
    }

    private fun displayImage(profileImg: String) {


        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                FirebaseStorage.getInstance().reference.child("profile_pics").child(profileImg)
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


    private fun initializeMonthTV(calendar: Calendar, needaction: Boolean) {
        val pattern = "MMMM yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val date: String = simpleDateFormat.format(calendar.time)
        month_year.text = date
        if (needaction)
            calendarView.setVerticalMonthChanged(calendar)
    }

    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<VerticalCalendarDataItemModel>
    private val visibleThreshold = 20
    var isLoading: Boolean = false
    private fun initializeVerticalCalendarRV() {
        recyclerGenericAdapter =
            RecyclerGenericAdapter<VerticalCalendarDataItemModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<VerticalCalendarDataItemModel?> { view, position, item ->
                    //item?.year, item?.month, item?.date
                    var layoutManager = rv_.layoutManager as LinearLayoutManager
                    fistvisibleItemOnclick = layoutManager.findFirstVisibleItemPosition()

                    val activeDateTime =
                        LocalDateTime.of(item?.year!!, item.month + 1, item.date, 0, 0, 0)

                    RosterDayFragment.arrMainHomeDataModel = viewModel.arrMainHomeDataModel!!
                    val bundle = Bundle()
                    bundle.putSerializable("active_date", activeDateTime)
                    navigation.navigateTo("rosterDayFragment", bundle)
//                            findNavController().navigate(R.id.rosterDayFragment, bundle)
                },
                RecyclerGenericAdapter.ItemInterface<VerticalCalendarDataItemModel?> { obj, viewHolder, position ->
                    if (obj!!.isMonth) {
                        showMonthLayout(true, viewHolder)
//                                getTextView(viewHolder, R.id.month_year).text =
//                                        obj.monthStr + " " + obj.year
                        (viewHolder.getView(R.id.month_year) as TextView).text =
                            obj.monthStr + " " + obj.year
                    } else {
                        (viewHolder.getView(R.id.title) as TextView).setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            14F
                        )
                        (viewHolder.getView(R.id.subtitle) as TextView).setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            12F
                        )

//                        setTextViewSize(getTextView(viewHolder, R.id.title), 14F)
//                        setTextViewSize(getTextView(viewHolder, R.id.subtitle), 12F)
                        viewHolder.getView(R.id.coloredsideline).visibility = View.GONE
                        viewHolder.getView(R.id.graysideline).visibility = View.VISIBLE

//                        getView(viewHolder, R.id.coloredsideline).visibility = View.GONE
//                        getView(viewHolder, R.id.graysideline).visibility = View.VISIBLE
                        showMonthLayout(false, viewHolder)
                        if (obj.title == "No gigs assigned") {
                            (viewHolder.getView(R.id.title) as TextView).text =
                                getString(R.string.no_gig_assigned_giger)
                        } else
                            (viewHolder.getView(R.id.title) as TextView).text = obj.title
//                        getTextView(viewHolder, R.id.title).text = obj.title
                        if (obj.subTitle != null && !obj.subTitle.equals("")) {
                            viewHolder.getView(R.id.subtitle).visibility = View.VISIBLE
                            (viewHolder.getView(R.id.subtitle) as TextView).text = obj.subTitle
//                            getTextView(viewHolder, R.id.subtitle).visibility = View.VISIBLE
//                            getTextView(viewHolder, R.id.subtitle).text = obj.subTitle
                        } else {
                            viewHolder.getView(R.id.subtitle).visibility = View.GONE
//                            getTextView(viewHolder, R.id.subtitle).visibility = View.GONE
                        }
                        (viewHolder.getView(R.id.day) as TextView).text = obj.day
                        (viewHolder.getView(R.id.date) as TextView).text = obj.date.toString()

//                        getTextView(viewHolder, R.id.day).text = obj.day
//                        getTextView(viewHolder, R.id.date).text = obj.date.toString()
                        if (obj.isToday) {
                            viewHolder.getView(R.id.coloredsideline).visibility = View.VISIBLE
                            viewHolder.getView(R.id.graysideline).visibility = View.GONE
//                            getView(viewHolder, R.id.coloredsideline).visibility = View.VISIBLE
//                            getView(viewHolder, R.id.graysideline).visibility = View.GONE

                            activity?.let {
                                viewHolder.getView(R.id.daydatecard).setBackgroundColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.vertical_calendar_today
                                    )
                                )
                                (viewHolder.getView(R.id.title) as TextView).setTextColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.vertical_calendar_today1
                                    )
                                )
                                (viewHolder.getView(R.id.subtitle) as TextView).setTextColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.vertical_calendar_today1
                                    )
                                )
                                (viewHolder.getView(R.id.day) as TextView).setTextColor(
                                    ContextCompat.getColor(it.applicationContext, R.color.white)
                                )
                                (viewHolder.getView(R.id.date) as TextView).setTextColor(
                                    ContextCompat.getColor(it.applicationContext, R.color.white)
                                )
                            }

//                            setViewBackgroundColor(
//                                getView(viewHolder, R.id.daydatecard),
//                                R.color.vertical_calendar_today
//                            )
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.title),
//                                R.color.vertical_calendar_today1
//                            )
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.subtitle),
//                                R.color.vertical_calendar_today1
//                            )
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.day),
//                                R.color.white
//                            )
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.date),
//                                R.color.white
//                            )
                            viewHolder.getView(R.id.daydatecard).alpha = 1.0F
//                            getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                            (viewHolder.getView(R.id.day) as TextView).setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                12F
                            )
                            (viewHolder.getView(R.id.date) as TextView).setTextSize(
                                TypedValue.COMPLEX_UNIT_SP,
                                14F
                            )

//                            setTextViewSize(getTextView(viewHolder, R.id.day), 12F)
//                            setTextViewSize(getTextView(viewHolder, R.id.date), 14F)
                        } else if (obj.isPreviousDate) {
                            activity?.let {
                                viewHolder.getView(R.id.daydatecard).setBackgroundColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.gray_color_calendar_previous_date
                                    )
                                )

                                (viewHolder.getView(R.id.title) as TextView).setTextColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.gray_color_calendar
                                    )
                                )
                                (viewHolder.getView(R.id.subtitle) as TextView).setTextColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.gray_color_calendar
                                    )
                                )
                                (viewHolder.getView(R.id.day) as TextView).setTextColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.gray_color
                                    )
                                )
                                (viewHolder.getView(R.id.date) as TextView).setTextColor(
                                    ContextCompat.getColor(
                                        it.applicationContext,
                                        R.color.gray_color
                                    )
                                )
                            }
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.title),
//                                R.color.gray_color_calendar
//                            )
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.subtitle),
//                                R.color.gray_color_calendar
//                            )
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.day),
//                                R.color.gray_color
//                            )
//                            setTextViewColor(
//                                getTextView(viewHolder, R.id.date),
//                                R.color.gray_color
//                            )
//                            setViewBackgroundColor(
//                                getView(viewHolder, R.id.daydatecard),
//                                R.color.gray_color_calendar_previous_date
//                            )


                            if (obj.isGigAssign) {
                                viewHolder.getView(R.id.daydatecard).alpha = 1.0F
//                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                            } else {
                                viewHolder.getView(R.id.daydatecard).alpha = 1.0F
                                viewHolder.getView(R.id.daydatecard).alpha = 0.5F
                                (viewHolder.getView(R.id.title) as TextView).text =
                                    getString(R.string.no_gig_assigned_giger)
//                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
//                                getView(viewHolder, R.id.daydatecard).alpha = 0.5F
                            }
                        } else {
                            if (obj.isUnavailable) {
                                (viewHolder.getView(R.id.title) as TextView).text =
                                    getString(R.string.not_working_app_giger)
                                viewHolder.getView(R.id.subtitle).gone()

                                activity?.let {
                                    viewHolder.getView(R.id.daydatecard).setBackgroundColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.date_day_unavailable_color
                                        )
                                    )

                                    (viewHolder.getView(R.id.title) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.gray_color_calendar
                                        )
                                    )
                                    (viewHolder.getView(R.id.day) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.gray_color_day_date_calendar
                                        )
                                    )
                                    (viewHolder.getView(R.id.date) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.gray_color_day_date_calendar
                                        )
                                    )
                                }
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.title),
//                                    R.color.gray_color_day_date_calendar
////                                )
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.day),
//                                    R.color.gray_color_day_date_calendar
//                                )
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.date),
//                                    R.color.gray_color_day_date_calendar
//                                )
//                                setViewBackgroundColor(
//                                    getView(viewHolder, R.id.daydatecard),
//                                    R.color.date_day_unavailable_color
//                                )
                                viewHolder.getView(R.id.daydatecard).alpha = 1.0F
//                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
                                setBackgroundStateAvailable(viewHolder)
                            } else if (obj.isGigAssign) {

                                activity?.let {
                                    viewHolder.getView(R.id.daydatecard).setBackgroundColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.vertical_calendar_today1
                                        )
                                    )

                                    (viewHolder.getView(R.id.title) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.black_color_future_date
                                        )
                                    )
                                    (viewHolder.getView(R.id.subtitle) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.black_color_future_date
                                        )
                                    )

                                    (viewHolder.getView(R.id.day) as TextView).setTextColor(
                                        ContextCompat.getColor(it.applicationContext, R.color.black)
                                    )
                                    (viewHolder.getView(R.id.date) as TextView).setTextColor(
                                        ContextCompat.getColor(it.applicationContext, R.color.black)
                                    )
                                }

//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.title),
//                                    R.color.black_color_future_date
//                                )
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.subtitle),
//                                    R.color.black_color_future_date
//                                )
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.day),
//                                    R.color.black
//                                )
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.date),
//                                    R.color.black
//                                )
//                                setViewBackgroundColor(
//                                    getView(viewHolder, R.id.daydatecard),
//                                    R.color.vertical_calendar_today1
//                                )
                                viewHolder.getView(R.id.daydatecard).alpha = 1.0F
                                viewHolder.getView(R.id.daydatecard).alpha = 0.7F
//                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
//                                getView(viewHolder, R.id.daydatecard).alpha = 0.7F
                            } else {

                                activity?.let {
                                    (viewHolder.getView(R.id.title) as TextView).text =
                                        getString(R.string.no_gig_assigned_giger)
//                                viewHolder.getView(R.id.subtitle).gone()

                                    viewHolder.getView(R.id.daydatecard).setBackgroundColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.vertical_calendar_today1
                                        )
                                    )

                                    (viewHolder.getView(R.id.title) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.gray_color_day_date_calendar
                                        )
                                    )
                                    (viewHolder.getView(R.id.day) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.gray_color_day_date_calendar
                                        )
                                    )
                                    (viewHolder.getView(R.id.date) as TextView).setTextColor(
                                        ContextCompat.getColor(
                                            it.applicationContext,
                                            R.color.gray_color_day_date_calendar
                                        )
                                    )
                                }

//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.title),
//                                    R.color.gray_color_day_date_calendar
//                                )
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.day),
//                                    R.color.gray_color_day_date_calendar
//                                )
//                                setTextViewColor(
//                                    getTextView(viewHolder, R.id.date),
//                                    R.color.gray_color_day_date_calendar
//                                )
//                                setViewBackgroundColor(
//                                    getView(viewHolder, R.id.daydatecard),
//                                    R.color.vertical_calendar_today1
//                                )
                                viewHolder.getView(R.id.daydatecard).alpha = 1.0F
                                viewHolder.getView(R.id.daydatecard).alpha = 0.4F
//                                getView(viewHolder, R.id.daydatecard).alpha = 1.0F
//                                getView(viewHolder, R.id.daydatecard).alpha = 0.4F
                            }
                        }
                    }
                })

        recyclerGenericAdapter.list = viewModel.getAllCalendarData()
        recyclerGenericAdapter.setLayout(R.layout.vertical_calendar_item)
        rv_.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        rv_.adapter = recyclerGenericAdapter
        if (fistvisibleItemOnclick == -1) {
            rv_.scrollToPosition((recyclerGenericAdapter.list.size / 2) - 2)
        } else {
            rv_.scrollToPosition(fistvisibleItemOnclick)
        }

        var scrollListener = object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                var layoutManager: LinearLayoutManager? = null
                if (layoutManager == null) {
                    layoutManager = recyclerView.layoutManager as LinearLayoutManager
                }
//                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
//                var calendar = Calendar.getInstance()
//                calendar.set(
//                    Calendar.MONTH,
//                    recyclerGenericAdapter.list.get(firstVisibleItem).month
//                )
//                calendar.set(Calendar.YEAR, recyclerGenericAdapter.list.get(firstVisibleItem).year)
//                initializeMonthTV(calendar)
                if (!isLoading) {
                    val totalItemCount = recyclerView.layoutManager?.itemCount


                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    if (totalItemCount!! <= (lastVisibleItem + visibleThreshold)) {
                        isLoading = true
                        recyclerGenericAdapter.list.addAll(
                            viewModel.getVerticalCalendarData(
                                recyclerGenericAdapter.list.get(recyclerGenericAdapter.list.size - 1),
                                false
                            )
                        )
                        recyclerGenericAdapter.notifyDataSetChanged()
                        isLoading = false
                    }

                    // below commented code will require later
//                    else if ((firstVisibleItem - visibleThreshold)<=0) {
//                        isLoading = true;
//                        recyclerGenericAdapter.list.addAll(0,viewModel.getVerticalCalendarData(
//                            recyclerGenericAdapter.list.get(0),true
//                        ))
//                        recyclerGenericAdapter.notifyDataSetChanged()
//                        isLoading = false
//                    }
                }


            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                var layoutManager: LinearLayoutManager? = null
                if (layoutManager == null) {
                    layoutManager = recyclerView.layoutManager as LinearLayoutManager
                }
                val firstVisibleItem = layoutManager!!.findFirstVisibleItemPosition()
                if (firstVisibleItem <= 0) return
                var calendar = Calendar.getInstance()

                calendar.set(
                    Calendar.MONTH,
                    recyclerGenericAdapter.list.get(firstVisibleItem - 1).month
                )
                calendar.set(Calendar.YEAR, recyclerGenericAdapter.list.get(firstVisibleItem).year)
                initializeMonthTV(calendar, true)
            }
        }
        rv_.addOnScrollListener(scrollListener)

        var itemTouchListener =
            CalendarRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, this)
        ItemTouchHelper(itemTouchListener).attachToRecyclerView(rv_)

    }

    private fun scrollVerticalCalendarToSelectedMonth() {
        var layoutManager: LinearLayoutManager? = null
        if (layoutManager == null) {
            layoutManager = rv_.layoutManager as LinearLayoutManager
        }
        val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

        if (firstVisibleItem == -1)
            return

        if (recyclerGenericAdapter.list.get(firstVisibleItem).year < selectedMonthModel.year || (recyclerGenericAdapter.list.get(
                firstVisibleItem
            ).year == selectedMonthModel.year && recyclerGenericAdapter.list.get(firstVisibleItem).month < selectedMonthModel.currentMonth)
        ) {
            for (index in firstVisibleItem..recyclerGenericAdapter.list.size) {
                if (recyclerGenericAdapter.list.get(index).year == selectedMonthModel.year && recyclerGenericAdapter.list.get(
                        index
                    ).month == selectedMonthModel.currentMonth
                ) {
                    (rv_.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        index + 1,
                        0
                    )
                    break
                }
            }
        } else {
            for (index in 0..firstVisibleItem) {
                if (recyclerGenericAdapter.list.get(index).year == selectedMonthModel.year && recyclerGenericAdapter.list.get(
                        index
                    ).month == selectedMonthModel.currentMonth
                ) {
                    (rv_.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                        index + 1,
                        0
                    )
                    break
                }
            }
        }
    }

    private fun setBackgroundStateAvailable(viewHolder: PFRecyclerViewAdapter<*>.ViewHolder) {

        activity?.let {
            viewHolder.getView(R.id.action_layout).setBackgroundColor(
                ContextCompat.getColor(
                    it.applicationContext,
                    R.color.action_layout_available
                )
            )
            viewHolder.getView(R.id.border_top).setBackgroundColor(
                ContextCompat.getColor(
                    it.applicationContext,
                    R.color.action_layout_available_border
                )
            )
            viewHolder.getView(R.id.border_bottom).setBackgroundColor(
                ContextCompat.getColor(
                    it.applicationContext,
                    R.color.action_layout_available_border
                )
            )

            (viewHolder.getView(R.id.title_calendar_action_item) as TextView).setTextColor(
                ContextCompat.getColor(it.applicationContext, R.color.action_layout_available_title)
            )
            (viewHolder.getView(R.id.title_calendar_action_item) as TextView).text =
                getString(R.string.marked_working_app_giger)

            (viewHolder.getView(R.id.flash_icon) as ImageView).setImageResource(R.drawable.ic_flash_green)
        }


//        setViewBackgroundColor(
//            getView(viewHolder, R.id.action_layout),
//            R.color.action_layout_available
//        )
//        setViewBackgroundColor(
//            getView(viewHolder, R.id.border_top),
//            R.color.action_layout_available_border
//        )
//        setViewBackgroundColor(
//            getView(viewHolder, R.id.border_bottom),
//            R.color.action_layout_available_border
//        )
//        setTextViewColor(
//            getTextView(viewHolder, R.id.title_calendar_action_item),
//            R.color.action_layout_available_title
//        )
//        getTextView(viewHolder, R.id.title_calendar_action_item).text =
//            getString(R.string.marked_working)
//        getImageView(viewHolder, R.id.flash_icon).setImageResource(R.drawable.ic_flash_green)
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        temporaryData = recyclerGenericAdapter.list.get(position)
        //event
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            val map = mapOf("Giger ID" to it)
            eventTracker.pushEvent(TrackingEventArgs("giger_attempted_decline",map))
        }

        if (temporaryData.isGigAssign) {
            if (temporaryData.isUnavailable) {
                temporaryData.isUnavailable = false
                swipedToupdateGig = true
                var data =
                    UnavailableDataModel(
                        temporaryData.getDateObj()
                    )
                data.dayUnavailable = false
                viewModelCustomPreference.updateCustomPreference(
                    data
                )

                recyclerGenericAdapter.notifyItemChanged(position)
            } else {

                calPosition = position
                gigViewModel.getTodaysUpcomingGig(temporaryData.getLocalDate())


//                showConfirmationDialogType1(
//                    getString(R.string.sure_working_on_this_day),
//                    object : ConfirmationDialogOnClickListener {
//                        override fun clickedOnYes(dialog: Dialog?) {
//                            var title =
//                                getString(R.string.alright_no_new_gigs_assigned_on_this_day) + temporaryData.title + getString(
//                                    R.string.want_to_decline
//                                )
//                            showConfirmationDialogType3(
//                                title,
//                                "Sub title",
//                                "yes",
//                                "No",
//                                object : ConfirmationDialogOnClickListener {
//                                    override fun clickedOnYes(dialog: Dialog?) {
//                                        val date = temporaryData.getLocalDate()
//                                        navigate(R.id.gigsListForDeclineBottomSheet, bundleOf(
//                                            GigsListForDeclineBottomSheet.INTEN_EXTRA_DATE to date
//                                        ))
//
//                                        makeChangesToCalendarItem(position, true)
//                                        dialog?.dismiss()
//                                    }
//
//                                    override fun clickedOnNo(dialog: Dialog?) {
//                                        makeChangesToCalendarItem(position, true)
//                                        dialog?.dismiss()
//                                    }
//
//                                })
//                            dialog?.dismiss()
//                        }
//
//                        override fun clickedOnNo(dialog: Dialog?) {
//                            recyclerGenericAdapter.notifyItemChanged(position)
//                            dialog?.dismiss()
//                        }
//
//                    })
            }
        } else if (temporaryData.isUnavailable) {
            makeChangesToCalendarItem(position, false)
        } else {
            makeChangesToCalendarItem(position, true)
            showSnackbar(position)
        }
    }

    var calPosition = -1
    private fun showTodaysGigDialog(gigOnDay: Int) {
        if (gigOnDay == 0) {
            gigListForDeclineBS()
            return
        }
        val view =
            layoutInflater.inflate(R.layout.dialog_confirm_gig_denial, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .show()

        view.findViewById<TextView>(R.id.dialog_message_tv)
            .text =
            getString(R.string.you_have_app_giger) + gigOnDay + getString(R.string.active_gig_on_this_date_app_giger)

        view.findViewById<View>(R.id.yesBtn)
            .setOnClickListener {
                gigListForDeclineBS()
                dialog?.dismiss()
            }

        view.findViewById<View>(R.id.noBtn)
            .setOnClickListener {
                makeChangesToCalendarItem(calPosition, true)
                dialog?.dismiss()
            }
    }


    private fun gigListForDeclineBS() {
        val date = temporaryData.getLocalDate()
        navigation.navigateTo(
            "gigsListForDeclineBottomSheet", bundleOf(
                AppConstants.INTEN_EXTRA_DATE to date
            )
        )

        makeChangesToCalendarItem(calPosition, true)

    }

    fun makeChangesToCalendarItem(position: Int, status: Boolean) {
        temporaryData.isUnavailable = status
        var data =
            UnavailableDataModel(
                temporaryData.getDateObj()
            )
        data.dayUnavailable = status
        swipedToupdateGig = true
        viewModelCustomPreference.updateCustomPreference(
            data
        )
        recyclerGenericAdapter.notifyItemChanged(position)
    }

    class OnSnackBarUndoClickListener(
        var position: Int,
        var recyclerGenericAdapter: RecyclerGenericAdapter<VerticalCalendarDataItemModel>,
        var snackbar: Snackbar,
        var viewModelCustomPreference: CustomPreferencesViewModel
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            temporaryData.isUnavailable = false
            var data =
                UnavailableDataModel(
                    temporaryData.getDateObj()
                )
            data.dayUnavailable = false
            viewModelCustomPreference.updateCustomPreference(
                data
            )
            recyclerGenericAdapter.notifyItemChanged(position)
            snackbar.dismiss()
        }
    }

    private fun showSnackbar(position: Int) {
        nsv.visibility = View.GONE
        val snackbar = Snackbar.make(coodinate_layout, "", Snackbar.LENGTH_LONG)
        // Get the Snackbar's layout view
        var layout = snackbar.view as Snackbar.SnackbarLayout
        // Hide the text
        var textView =
            layout.findViewById<TextView>(R.id.snackbar_text)
        textView.visibility = View.INVISIBLE

        // Inflate our custom view
        var snackView = layoutInflater.inflate(R.layout.snackbar_layout, null)
        snackView.setOnClickListener(
            OnSnackBarUndoClickListener(
                position,
                recyclerGenericAdapter,
                snackbar,
                viewModelCustomPreference
            )
        )
        //If the view is not covering the whole snackbar layout, add this line
        layout.setPadding(0, 0, 0, 0)
        // Add the view to the Snackbar's layout
        layout.addView(snackView, 0)
        // Show the Snackbar
        snackbar.show()
        Handler().postDelayed({
            if (nsv != null)
                nsv.visibility = View.VISIBLE
        }, SNACKBAR_TIMEOUT)

    }

    private val SNACKBAR_TIMEOUT: Long = 2000 // 1 sec
    private fun showMonthLayout(show: Boolean, viewHolder: PFRecyclerViewAdapter<*>.ViewHolder) {
        if (show) {
            viewHolder.getView(R.id.calendar_month_cl).visibility = View.VISIBLE
            viewHolder.getView(R.id.calendar_detail_item_cl).visibility = View.GONE

//            getView(viewHolder, R.id.calendar_month_cl).visibility = View.VISIBLE
//            getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.GONE
        } else {
            viewHolder.getView(R.id.calendar_month_cl).visibility = View.GONE
            viewHolder.getView(R.id.calendar_detail_item_cl).visibility = View.VISIBLE

//            getView(viewHolder, R.id.calendar_month_cl).visibility = View.GONE
//            getView(viewHolder, R.id.calendar_detail_item_cl).visibility = View.VISIBLE
        }
    }

    inner class BottomSheetExpansionListener : ExtendedBottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            viewModel.currentBottomSheetState = newState
            Log.d("BottomSheetState ", "Change State : $newState")
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

}