package com.gigforce.app.modules.calendarscreen.maincalendarscreen.bottomsheet

import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.landingscreen.LandingScreenViewModel
import com.gigforce.app.utils.GigNavigation
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewmodels.LearningViewModel
import com.gigforce.core.AppConstants
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.datamodels.gigpage.ContactPerson
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.datamodels.learning.CourseContent
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.viewModels.GigViewModel
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_screen_bottom_sheet_fragment.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BSCalendarScreenFragment : Fragment() {

    companion object {
        fun newInstance() = BSCalendarScreenFragment()
    }

    private lateinit var viewModel: BSCalendarScreenViewModel
    private val gigViewModel: GigViewModel by viewModels()
    private val learningViewModel: LearningViewModel by viewModels()
    private val landingScreenViewModel: LandingScreenViewModel by viewModels()
    @Inject
    lateinit var navigation: INavigation
    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_screen_bottom_sheet_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BSCalendarScreenViewModel::class.java)
        initializeBottomSheet()
        initGigViewModel()
        initLearningViewModel()
        initializeClientActivation()
        initProfileViewModel()
    }

    private fun initProfileViewModel() {
//        profileViewModel.getProfileData().observe(viewLifecycleOwner, Observer { profileObs ->
//            val profile: ProfileData = profileObs!!
//
//            ambassador_layout.visible()
//            if (profile.isUserAmbassador) {
//                join_as_amb_label.text = getString(R.string.ambassador_program)
//                amb_join_open_btn.text = getString(R.string.open)
//            } else {
//                join_as_amb_label.text = getString(R.string.join_us_as_an_ambassador)
//                amb_join_open_btn.text = getString(R.string.join_now)
//            }
//        })

        amb_join_open_btn.setOnClickListener {

//            if (amb_join_open_btn.text == getString(R.string.open)) {
            navigation.navigateTo("ambassador/users_enrolled")
//            navigate(R.id.ambassadorEnrolledUsersListFragment)


//            } else {
//                navigate(R.id.ambassadorProgramDetailsFragment)
//            }
        }
    }

    private fun initLearningViewModel() {
        learningViewModel
            .roleBasedCourses
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showLearningAsLoading()
                    is Lce.Content -> showUserLearningCourses(it.content)
                    is Lce.Error -> showErrorWhileLoadingCourse(it.error)
                }
            })

//        mainLearningViewModel
//                .allAssessments
//                .observe(viewLifecycleOwner, Observer {
//
//                    when (it) {
//                        Lce.Loading -> showAssessmentProgress()
//                        is Lce.Content -> showAssessments(it.content)
//                        is Lce.Error -> showAssessmentError(it.error)
//                    }
//                })
//
//
//        mainLearningViewModel.getAssessmentsFromAllAssignedCourses()
        learningViewModel.getRoleBasedCourses()

    }

    private fun initGigViewModel() {
        gigViewModel.upcomingGigs
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lce.Loading -> {
                    }
                    is Lce.Content -> initializeUpcomingGigBottomSheet(it.content)
                    is Lce.Error -> {
                    }
                }
            })

        gigViewModel.watchUpcomingGigs()
    }

    private fun showLearningAsLoading() {

        learning_rv.gone()
        learning_learning_error.gone()
        learning_progress_bar.visible()
    }

    private fun showErrorWhileLoadingCourse(error: String) {

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
            learning_tv.gone()
            learning_container.gone()
        } else {
            learning_tv.visible()
            learning_container.visible()


            val itemWidth = ((width / 3) * 2).toInt()
//            val recyclerGenericAdapter: RecyclerGenericAdapter<Course> =
//                RecyclerGenericAdapter<Course>(
//                    activity?.applicationContext,
//                    PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                        navigation.navigateTo("learning/main")
////                        navigate(R.id.mainLearningFragment)
//                    },
//                    RecyclerGenericAdapter.ItemInterface<Course?> { obj, viewHolder, position ->
//                        var view = getView(viewHolder, R.id.card_view)
//                        val lp = view.layoutParams
//                        lp.height = lp.height
//                        lp.width = itemWidth
//                        view.layoutParams = lp
//
//                        var title = getTextView(viewHolder, R.id.title_)
//                        title.text = obj?.name
//
//                        var subtitle = getTextView(viewHolder, R.id.title)
//                        subtitle.text = obj?.level
//
//                        var comImg = getImageView(viewHolder, R.id.completed_iv)
//                        comImg.isVisible = obj?.completed ?: false
//
//
//                        var img = getImageView(viewHolder, R.id.learning_img)
//
//                        if (!obj!!.coverPicture.isNullOrBlank()) {
//                            if (obj.coverPicture!!.startsWith("http", true)) {
//
//                                GlideApp.with(requireContext())
//                                    .load(obj.coverPicture!!)
//                                    .placeholder(getCircularProgressDrawable())
//                                    .error(R.drawable.ic_learning_default_back)
//                                    .into(img)
//                            } else {
//                                FirebaseStorage.getInstance()
//                                    .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
//                                    .child(obj.coverPicture!!)
//                                    .downloadUrl
//                                    .addOnSuccessListener { fileUri ->
//
//                                        GlideApp.with(requireContext())
//                                            .load(fileUri)
//                                            .placeholder(getCircularProgressDrawable())
//                                            .error(R.drawable.ic_learning_default_back)
//                                            .into(img)
//                                    }
//                            }
//                        } else {
//                            GlideApp.with(requireContext())
//                                .load(R.drawable.ic_learning_default_back)
//                                .into(img)
//                        }
//
//                        //img.setImageResource(obj?.imgIcon!!)
//                    })
//            recyclerGenericAdapter.setList(content)
//            recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
            var userLearningAdater = UserLearningAdater(requireContext())
            userLearningAdater.data = content
            userLearningAdater.setOnclickListener(object : AdapterClickListener<Course> {
                override fun onItemClick(view: View, obj: Course, position: Int) {
                    navigation.navigateTo("learning/main")
////                        navigate(R.id.mainLearningFragment)
                }
            })

            learning_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            learning_rv.adapter = userLearningAdater

        }
    }

    private fun showAssessmentProgress() {
        assessment_rv.gone()
        learning_assessment_error.gone()
        learning_assessment_progress_bar.visible()
    }

    private fun showAssessmentError(error: String) {

        assessment_rv.gone()
        learning_assessment_progress_bar.gone()
        learning_assessment_error.visible()

        learning_assessment_error.text = error
    }


    private fun showAssessments(content: List<CourseContent>) {

        learning_assessment_progress_bar.gone()
        learning_assessment_error.gone()

        if (content.isEmpty()) {
            assessment_tv.gone()
            assessment_layout.gone()
        } else {

            assessment_tv.visible()
            assessment_layout.visible()
            assessment_rv.visible()

            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val itemWidth = ((width / 5) * 3.5).toInt()


//            val recyclerGenericAdapter: RecyclerGenericAdapter<CourseContent> =
//                RecyclerGenericAdapter<CourseContent>(
//                    activity?.applicationContext,
//                    PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                        val assessment = item as CourseContent
//
//                        showToast("Disabled ,will be enabled soon")
////
////                        navigate(R.id.assessment_fragment,  bundleOf(
////                            AssessmentFragment.INTENT_LESSON_ID to assessment.id
////                        )
////                        )
//                    },
//                    RecyclerGenericAdapter.ItemInterface<CourseContent> { obj, viewHolder, position ->
//                        val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
//                        lp.height = lp.height
//                        lp.width = itemWidth
//                        getView(viewHolder, R.id.assessment_cl).layoutParams = lp
//                        getTextView(viewHolder, R.id.title).text = obj?.title
//                        getTextView(viewHolder, R.id.time).text = "02:00"
//
//
//                        getTextView(viewHolder, R.id.status).text = "PENDING"
//                        getTextView(
//                            viewHolder,
//                            R.id.status
//                        ).setBackgroundResource(R.drawable.rect_assessment_status_pending)
//                        (getView(
//                            viewHolder,
//                            R.id.side_bar_status
//                        ) as ImageView).setImageResource(R.drawable.assessment_line_pending)
//
//
//                    })
//            recyclerGenericAdapter.list = content
//            recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
//            assessment_rv.layoutManager = LinearLayoutManager(
//                activity?.applicationContext,
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//            assessment_rv.adapter = recyclerGenericAdapter
        }

    }

    // Need to find if neccessary // language need to test how it is working
//    override fun isDeviceLanguageChangedDialogRequired(): Boolean {
//        return false
//    }

    private fun initializeBottomSheet() {
//        nsv.setBackground(generateBackgroundWithShadow(nsv,R.color.white,
//            R.dimen.eight_dp,R.color.gray_color,R.dimen.five_dp, Gravity.TOP))
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        initializeVerificationAlert()
        initializeFeaturesBottomSheet()
//        initializeAssessmentBottomSheet()
        application_version.text =
            getString(R.string.version) + " " + sharedPreAndCommonUtilInterface.getCurrentVersion()
        listener()
        initializeExploreByRole()
//        initializeExploreByIndustry()
        initBottomMenuClicks()
    }

    private fun initializeVerificationAlert() {
        var clickhere: String = getString(R.string.click_here)
        var content: SpannableString = SpannableString(clickhere)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        kyc_tv.text =
            Html.fromHtml(getString(R.string.kyc_not_done))
        video_resume_tv.text =
            Html.fromHtml(getString(R.string.video_resume_pending_html))
    }

    private fun listener() {
        kyc_tv.setOnClickListener {
            navigation.navigateTo("verification/main")
//            navigate(R.id.gigerVerificationFragment)
        }
        video_resume.setOnClickListener {
            navigation.navigateTo("videoResumeFragment")
//            navigate(R.id.videoResumeFragment)
        }
        show_upcominggig_layout.setOnClickListener {
            showKYCAndHideUpcomingLayout(false)
        }

    }

    private fun showKYCAndHideUpcomingLayout(show: Boolean) {
        if (show) {
            kyc_video_resume.visibility = View.VISIBLE
            upcoming_gig_title.visibility = View.GONE
            upcoming_gig_rv.visibility = View.GONE
        } else {
            upcoming_gig_title.visibility = View.VISIBLE
            upcoming_gig_rv.visibility = View.VISIBLE
            kyc_video_resume.visibility = View.GONE
        }
    }

    private val timeFormatter = SimpleDateFormat("hh.mm aa")

    var width: Int = 0
    private fun initializeUpcomingGigBottomSheet(upcomingGigs: List<Gig>) {
        if (upcomingGigs.isNotEmpty()) {
            upcoming_gig_rv.visibility = View.VISIBLE
            upcoming_gig_title.visibility = View.VISIBLE

            val itemWidth = ((width / 5) * 4).toInt()
//            val recyclerGenericAdapter: RecyclerGenericAdapter<Gig> =
//                RecyclerGenericAdapter<Gig>(
//                    activity?.applicationContext,
//                    PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                        val gig = item as Gig
//                        GigNavigation.openGigMainPage(
//                            findNavController(),
//                            gig.openNewGig(),
//                            gig.gigId
//                        )
////                    showKYCAndHideUpcomingLayout(
////                        true
////                    )
//                    },
//                    RecyclerGenericAdapter.ItemInterface<Gig?> { obj, viewHolder, position ->
//                        val lp = getView(viewHolder, R.id.card_view).layoutParams
//                        lp.height = lp.height
//                        lp.width = itemWidth
//                        var ivContact = getImageView(viewHolder, R.id.iv_call)
//
//                        ivContact.setImageResource(R.drawable.fui_ic_phone_white_24dp)
//                        ivContact.setColorFilter(
//                            ContextCompat.getColor(
//                                viewHolder.itemView.context,
//                                R.color.lipstick
//                            ), android.graphics.PorterDuff.Mode.SRC_IN
//                        )
//
//                        getImageView(
//                            viewHolder,
//                            R.id.iv_message
//                        ).setImageResource(R.drawable.ic_chat)
//
//                        if (obj!!.openNewGig() && obj.agencyContact?.uid != null) {
//
//                            getView(viewHolder, R.id.messageCardView).visible()
//                            getView(viewHolder, R.id.messageCardView).setOnClickListener {
//                                val bundle = Bundle()
//                                val agencyContact =
//                                    upcomingGigs[viewHolder.adapterPosition].agencyContact
//                                        ?: return@setOnClickListener
//                                navigate(
//                                    R.id.chatPageFragment, bundleOf(
//                                        ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
//                                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to agencyContact.uid,
//                                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE to agencyContact.profilePicture,
//                                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to agencyContact.name
//                                    )
//                                )
//                            }
//
//                        } else if (obj.gigContactDetails != null && obj.gigContactDetails?.contactNumber != null) {
//                            if (obj.chatInfo?.isNullOrEmpty() == false) {
//                                getView(viewHolder, R.id.messageCardView).visible()
//                                getView(viewHolder, R.id.messageCardView).setOnClickListener {
//                                    val bundle = Bundle()
//                                    val map = upcomingGigs[viewHolder.adapterPosition].chatInfo
//                                    bundle.putString(
//                                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE,
//                                        AppConstants.IMAGE_URL
//                                    )
//                                    bundle.putString(
//                                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME,
//                                        AppConstants.CONTACT_NAME
//                                    )
//                                    bundle.putString(
//                                        ChatPageFragment.INTENT_EXTRA_CHAT_TYPE,
//                                        ChatConstants.CHAT_TYPE_USER
//                                    )
//
//                                    bundle.putString(
//                                        ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID,
//                                        map?.get("chatHeaderId") as String
//                                    )
//                                    bundle.putString(
//                                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID,
//                                        map.get("otherUserId") as String
//                                    )
//                                    bundle.putString(
//                                        StringConstants.MOBILE_NUMBER.value,
//                                        map.get(StringConstants.MOBILE_NUMBER.value) as String
//                                    )
//                                    bundle.putBoolean(
//                                        StringConstants.FROM_CLIENT_ACTIVATON.value,
//                                        map.get(StringConstants.FROM_CLIENT_ACTIVATON.value) as Boolean
//                                    )
//                                    navigate(R.id.chatPageFragment, bundle)
//                                }
//
//                            } else {
//
//                                getView(viewHolder, R.id.messageCardView).gone()
//                            }
//                        } else {
//                            getView(viewHolder, R.id.messageCardView).gone()
//                        }
//
//                        getView(viewHolder, R.id.card_view).layoutParams = lp
//                        getView(viewHolder, R.id.card_view).layoutParams = lp
//                        getTextView(viewHolder, R.id.textView41).text = obj.getGigTitle()
//                        getTextView(viewHolder, R.id.contactPersonTV).text = if (obj.openNewGig())
//                            obj.agencyContact?.name
//                        else
//                            obj.gigContactDetails?.contactName
//
//                        val gigStatus = GigStatus.fromGig(obj)
//                        when (gigStatus) {
//                            GigStatus.UPCOMING,
//                            GigStatus.DECLINED,
//                            GigStatus.CANCELLED,
//                            GigStatus.COMPLETED,
//                            GigStatus.MISSED -> {
//
//                                getView(viewHolder, R.id.checkInTV).isEnabled = false
//                                (getView(viewHolder, R.id.checkInTV) as Button).text = "Check In"
//                            }
//                            GigStatus.ONGOING,
//                            GigStatus.PENDING,
//                            GigStatus.NO_SHOW -> {
//
//                                getView(viewHolder, R.id.checkInTV).setOnClickListener(
//                                    CheckInClickListener(
//                                        upcoming_gig_rv,
//                                        position
//                                    )
//                                )
//
//                                if (obj.isCheckInAndCheckOutMarked()) {
//                                    getView(viewHolder, R.id.checkInTV).isEnabled = false
//                                    (getView(viewHolder, R.id.checkInTV) as Button).text =
//                                        "Checked Out"
//                                } else if (obj.isCheckInMarked()) {
//                                    getView(viewHolder, R.id.checkInTV).isEnabled = true
//                                    (getView(viewHolder, R.id.checkInTV) as Button).text =
//                                        getString(R.string.check_out)
//                                } else {
//                                    getView(viewHolder, R.id.checkInTV).isEnabled = true
//                                    (getView(viewHolder, R.id.checkInTV) as Button).text =
//                                        getString(R.string.check_in)
//                                }
//                            }
//                        }
//
//                        if (obj.isGigOfToday()) {
//
//                            val gigTiming = if (obj.endDateTime != null)
//                                "${timeFormatter.format(obj.startDateTime.toDate())} - ${
//                                timeFormatter.format(
//                                    obj.endDateTime.toDate()
//                                )
//                                }"
//                            else
//                                "${timeFormatter.format(obj.startDateTime.toDate())} - "
//                            getTextView(viewHolder, R.id.textView67).text = gigTiming
//
//                        } else {
//                            val date = DateHelper.getDateInDDMMYYYY(obj.startDateTime.toDate())
//                            getTextView(viewHolder, R.id.textView67).text = date
//                        }
//
//                        getView(viewHolder, R.id.navigateTV).setOnClickListener(
//                            NavigationClickListener(upcoming_gig_rv, position)
//                        )
//
//                        val callView = getView(viewHolder, R.id.callCardView)
//                        if (obj.gigContactDetails?.contactNumber != null) {
//
//                            callView.visible()
//                            callView.setOnClickListener(
//                                CallClickListener(
//                                    upcoming_gig_rv,
//                                    position
//                                )
//                            )
//                        } else if (!obj.agencyContact?.contactNumber.isNullOrEmpty()) {
//
//                            callView.visible()
//                            callView.setOnClickListener(
//                                CallClickListener(
//                                    upcoming_gig_rv,
//                                    position
//                                )
//                            )
//                        } else {
//                            callView.gone()
//                        }
//
//
//                        val companyLogoIV = getImageView(viewHolder, R.id.companyLogoIV)
//                        if (!obj.getFullCompanyLogo().isNullOrBlank()) {
//
//                            if (obj.getFullCompanyLogo()!!.startsWith("http", true)) {
//
//                                Glide.with(requireContext())
//                                    .load(obj.getFullCompanyLogo())
//                                    .into(companyLogoIV)
//
//                            } else {
//                                FirebaseStorage.getInstance()
//                                    .reference
//                                    .child(obj.getFullCompanyLogo()!!)
//                                    .downloadUrl
//                                    .addOnSuccessListener {
//
//                                        Glide.with(requireContext())
//                                            .load(it)
//                                            .into(companyLogoIV)
//                                    }
//                            }
//                        } else {
//                            val companyInitials = if (obj.getFullCompanyName().isNullOrBlank())
//                                "C"
//                            else
//                                obj.getFullCompanyName()!![0].toString().toUpperCase()
//                            val drawable = TextDrawable.builder().buildRound(
//                                companyInitials,
//                                ResourcesCompat.getColor(resources, R.color.lipstick, null)
//                            )
//
//                            companyLogoIV.setImageDrawable(drawable)
//                        }
//
//                    })
//            recyclerGenericAdapter.setList(upcomingGigs)

            var upcomingGigBSAdapter = UpcomingGigBSAdapter(requireContext())
            upcomingGigBSAdapter.data = upcomingGigs
            upcomingGigBSAdapter.setAgencyOnclickListener(object :
                AdapterClickListener<ContactPerson> {
                override fun onItemClick(view: View, obj: ContactPerson, position: Int) {
                    navigation.navigateTo(
                        "chats/chatPage", bundleOf(
                            ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to obj.uid,
                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE to obj.profilePicture,
                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to obj.name
                        )
                    )
//                    navigate(
//                        R.id.chatPageFragment, bundleOf(
//                            ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to obj.uid,
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE to obj.profilePicture,
//                            ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to obj.name
//                        )
//                    )
                }

            })

            upcomingGigBSAdapter.setchatInfoOnclickListener(object :
                AdapterClickListener<Map<String, Any>> {
                override fun onItemClick(view: View, obj: Map<String, Any>, position: Int) {
                    val bundle = Bundle()
                    val map = obj
                    bundle.putString(
                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_IMAGE,
                        AppConstants.IMAGE_URL
                    )
                    bundle.putString(
                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME,
                        AppConstants.CONTACT_NAME
                    )
                    bundle.putString(
                        ChatPageFragment.INTENT_EXTRA_CHAT_TYPE,
                        ChatConstants.CHAT_TYPE_USER
                    )

                    bundle.putString(
                        ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID,
                        map.get("chatHeaderId") as String
                    )
                    bundle.putString(
                        ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID,
                        map.get("otherUserId") as String
                    )
                    bundle.putString(
                        StringConstants.MOBILE_NUMBER.value,
                        map.get(StringConstants.MOBILE_NUMBER.value) as String
                    )
                    bundle.putBoolean(
                        StringConstants.FROM_CLIENT_ACTIVATON.value,
                        map.get(StringConstants.FROM_CLIENT_ACTIVATON.value) as Boolean
                    )
                    navigation.navigateTo("chats/chatPage", bundle)
//                    navigate(R.id.chatPageFragment, bundle)
                }

            })

            upcomingGigBSAdapter.setcallOnclickListener(object : AdapterClickListener<Any> {
                override fun onItemClick(view: View, obj: Any, position: Int) {
                    CallClickListener(
                        upcoming_gig_rv,
                        position
                    )
                }
            })

            upcomingGigBSAdapter.setnavigationOnclickListener(object : AdapterClickListener<Any> {
                override fun onItemClick(view: View, obj: Any, position: Int) {
                    NavigationClickListener(upcoming_gig_rv, position)
                }
            })

            viewModel.getTeamLeadInfo(upcomingGigs)
            viewModel.observableChatInfo.observe(viewLifecycleOwner, Observer {
                upcomingGigBSAdapter.notifyItemChanged(upcomingGigs.indexOf(it))
            })
//            recyclerGenericAdapter.setLayout(R.layout.upcoming_gig_item)
            upcoming_gig_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            upcoming_gig_rv.adapter = upcomingGigBSAdapter
        } else {
            upcoming_gig_rv.visibility = View.GONE
            upcoming_gig_title.visibility = View.GONE
        }
    }

    inner class NavigationClickListener(val rv: RecyclerView, var position: Int) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            //val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)

            showToast("This is work in progress. Please check again in a few days")

//            navigate(R.id.gigPageNavigationFragment, Bundle().apply {
//                this.putString(GigPageNavigationFragment.INTENT_EXTRA_GIG_ID, gig.gigId)
//            })
        }
    }

    inner class CheckInClickListener(val rv: RecyclerView, var position: Int) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)
            GigNavigation.openGigAttendancePage(findNavController(), gig.openNewGig(), gig.gigId)
        }
    }

    inner class CallClickListener(val rv: RecyclerView, var position: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)

            if (gig.gigContactDetails?.contactNumber != null &&
                gig.gigContactDetails?.contactNumber != 0L
            ) {

                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.fromParts("tel", gig.gigContactDetails?.contactNumber.toString(), null)
                )
                startActivity(intent)
            } else if (!gig.agencyContact?.contactNumber.isNullOrEmpty()) {
                val intent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.fromParts("tel", gig.agencyContact?.contactNumber, null)
                )
                startActivity(intent)
            }
        }
    }

    open inner class ChatClickListener(val rv: RecyclerView, var position: Int) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            val gig = (rv.adapter as RecyclerGenericAdapter<Gig>).list.get(position)
            // navigate(R.id.fakeGigContactScreenFragment)
        }
    }

    fun generateBackgroundWithShadow(
        view: View, @ColorRes backgroundColor: Int,
        @DimenRes cornerRadius: Int,
        @ColorRes shadowColor: Int,
        @DimenRes elevation: Int,
        shadowGravity: Int
    ): Drawable? {
        val cornerRadiusValue =
            view.context.resources.getDimension(cornerRadius)
        val elevationValue = view.context.resources.getDimension(elevation).toInt()
        val shadowColorValue = ContextCompat.getColor(view.context, shadowColor)
        val backgroundColorValue = ContextCompat.getColor(view.context, backgroundColor)
        val outerRadius = floatArrayOf(
            cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
            cornerRadiusValue, cornerRadiusValue, cornerRadiusValue, cornerRadiusValue,
            cornerRadiusValue
        )
        val backgroundPaint = Paint()
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.setShadowLayer(cornerRadiusValue, 0F, 0F, 0)
        val shapeDrawablePadding = Rect()
        shapeDrawablePadding.left = elevationValue
        shapeDrawablePadding.right = elevationValue
        val DY: Int
        when (shadowGravity) {
            Gravity.CENTER -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue
                DY = 0
            }
            Gravity.TOP -> {
                shapeDrawablePadding.top = elevationValue * 2
                shapeDrawablePadding.bottom = elevationValue
                DY = -1 * elevationValue / 3
            }
            Gravity.BOTTOM -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue * 2
                DY = elevationValue / 3
            }
            else -> {
                shapeDrawablePadding.top = elevationValue
                shapeDrawablePadding.bottom = elevationValue * 2
                DY = elevationValue / 3
            }
        }
        val shapeDrawable = ShapeDrawable()
        shapeDrawable.setPadding(shapeDrawablePadding)
        shapeDrawable.paint.color = backgroundColorValue
        shapeDrawable.paint
            .setShadowLayer(cornerRadiusValue / 3, 0f, DY.toFloat(), shadowColorValue)
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, shapeDrawable.paint)
        shapeDrawable.shape = RoundRectShape(outerRadius, null, null)
        val drawable =
            LayerDrawable(arrayOf<Drawable>(shapeDrawable))
        drawable.setLayerInset(
            0,
            elevationValue,
            elevationValue * 2,
            elevationValue,
            elevationValue * 2
        )
        return drawable
    }

    private fun initializeFeaturesBottomSheet() {
        var datalist: ArrayList<FeatureModel> = ArrayList<FeatureModel>()
        datalist.add(FeatureModel("My Gig", R.drawable.mygig, navigationPath = "mygig"))
        datalist.add(
            FeatureModel(
                "Wallet",
                R.drawable.wallet,
                navigationPath = "payslipMonthlyFragment"
            )
        )
        datalist.add(FeatureModel("Profile", R.drawable.profile, navigationPath = "profile"))
        datalist.add(
            FeatureModel(
                "Learning",
                R.drawable.learning,
                navigationPath = "learning/main"
            )
        )
        datalist.add(FeatureModel("Settings", R.drawable.settings, navigationPath = "setting"))
        datalist.add(
            FeatureModel(
                "Chat",
                R.drawable.ic_homescreen_chat,
                navigationPath = "chats/chatList"
            )
        )
        datalist.add(
            FeatureModel(
                "Verification",
                R.drawable.ic_shield_black,
                navigationPath = "verification/main"
            )
        )

        val itemWidth = ((width / 7) * 1.6).toInt()
//        val recyclerGenericAdapter: RecyclerGenericAdapter<FeatureModel> =
//            RecyclerGenericAdapter<FeatureModel>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<FeatureModel?> { view, position, item ->
//                    if (item?.navigationID != -1) {
//                        item?.navigationID?.let { navigate(it) }
//                    } else {
//                        showToast("This page is inactive. We’ll activate it in a few weeks")
//                    }
//                },
//                RecyclerGenericAdapter.ItemInterface<FeatureModel?> { obj, viewHolder, position ->
//                    val lp = getView(viewHolder, R.id.card_view).layoutParams
//                    lp.height = lp.height
//                    lp.width = itemWidth
//                    getView(viewHolder, R.id.card_view).layoutParams = lp
//                    getImageView(viewHolder, R.id.feature_icon).setImageResource(obj?.icon!!)
//                    getTextView(viewHolder, R.id.feature_title).text = obj.title
//
//                })
//        recyclerGenericAdapter.list = datalist

        var featureItemAdapter = FeatureItemAdapter(requireContext())
        featureItemAdapter.data = datalist
        featureItemAdapter.setOnclickListener(object : AdapterClickListener<FeatureModel> {
            override fun onItemClick(view: View, obj: FeatureModel, position: Int) {
                if (obj.navigationPath.equals("")) {
                    navigation.navigateTo(obj.navigationPath)
                }
            }
        })
//        recyclerGenericAdapter.setLayout(R.layout.feature_item)
        feature_rv.layoutManager = GridLayoutManager(
            activity, 2,
            GridLayoutManager.HORIZONTAL, false
        )
        feature_rv.adapter = featureItemAdapter
    }

//    private fun navigateToFeature(position: Int) {
//        when (position) {
//            0 -> showToast("")
//            1 -> showToast("")
//            2 -> navigate(R.id.walletBalancePage)
//            3 -> navigate(R.id.profileFragment)
//            4 -> showToast("")
//            5 -> navigate(R.id.settingFragment)
//            6 -> navigate(R.id.helpChatFragment)
//            7 -> navigate(R.id.landinghomefragment)
//        }
//    }


    class Assessment(var title: String, var time: String, var status: Boolean)


//    private fun initializeAssessmentBottomSheet() {
//        val itemWidth = ((width / 5) * 2.8).toInt()
//        var datalist: ArrayList<Assessment> = ArrayList<Assessment>()
//
//        datalist.add(
//            Assessment(
//                "Getting prepared for a product demo",
//                "02:00 Min",
//                true
//            )
//        )
//        datalist.add(
//            Assessment(
//                "Conducting an effective product demo",
//                "05:00 Min",
//                false
//            )
//        )
//        datalist.add(
//            Assessment(
//                "How to connect with customer",
//                "05:00 Min",
//                false
//            )
//        )
//        datalist.add(
//            Assessment(
//                "Closing the product demo",
//                "05:00 Min",
//                false
//            )
//        )
//
//
//        val recyclerGenericAdapter: RecyclerGenericAdapter<Assessment> =
//            RecyclerGenericAdapter<Assessment>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    //                    showToast("This page are inactive. We’ll activate it in a few weeks")
//                    navigate(R.id.assessment_fragment)
//                },
//                RecyclerGenericAdapter.ItemInterface<Assessment?> { obj, viewHolder, position ->
//                    val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
//                    lp.height = lp.height
//                    lp.width = itemWidth
//                    getView(viewHolder, R.id.assessment_cl).layoutParams = lp
//                    getTextView(viewHolder, R.id.title).text = obj?.title
//                    getTextView(viewHolder, R.id.time).text = obj?.time
//
//                    if (obj?.status!!) {
//                        getTextView(viewHolder, R.id.status).text = getString(R.string.completed)
//                        getTextView(
//                            viewHolder,
//                            R.id.status
//                        ).setBackgroundResource(R.drawable.rect_assessment_status_completed)
//                        (getView(
//                            viewHolder,
//                            R.id.side_bar_status
//                        ) as ImageView).setImageResource(R.drawable.assessment_line_done)
//
//                    } else {
//                        getTextView(viewHolder, R.id.status).text = getString(R.string.pending)
//                        getTextView(
//                            viewHolder,
//                            R.id.status
//                        ).setBackgroundResource(R.drawable.rect_assessment_status_pending)
//                        (getView(
//                            viewHolder,
//                            R.id.side_bar_status
//                        ) as ImageView).setImageResource(R.drawable.assessment_line_pending)
//                    }
//
//                })
//        recyclerGenericAdapter.list = datalist
//        recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
//        assessment_rv.layoutManager = LinearLayoutManager(
//            activity?.applicationContext,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        assessment_rv.adapter = recyclerGenericAdapter
//    }


    private fun showGlideImage(url: String, imgview: ImageView) {
        GlideApp.with(requireContext())
            .load(url)
            .placeholder(getCircularProgressDrawable())
            .into(imgview)
    }

//    private fun initializeExploreByIndustry() {
//
//        val itemWidth = ((width / 3) * 2).toInt()
//        // model will change when integrated with DB
////        var datalist: ArrayList<UpcomingGigModel> = ArrayList<UpcomingGigModel>()
//        var datalist: ArrayList<LandingScreenFragment.TitleSubtitleModel> =
//            ArrayList<LandingScreenFragment.TitleSubtitleModel>()
//
//        datalist.add(
//            LandingScreenFragment.TitleSubtitleModel(
//                "Delivery",
//                "",
//                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry.jpg?alt=media&token=039ddf50-9597-4ee4-bc12-0abdea74fd16"
//            )
//        )
//
//        datalist.add(
//            LandingScreenFragment.TitleSubtitleModel(
//                "Retail",
//                "",
//                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry3.jpg?alt=media&token=1813f5dd-5596-4a04-a0e1-3c8400a3d82d"
//            )
//        )
//
//
//        datalist.add(
//            LandingScreenFragment.TitleSubtitleModel(
//                "Quick Service Restuarant",
//                "",
//                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry1.jpg?alt=media&token=2634019b-9777-4dbb-9103-1d63eb44df97"
//            )
//        )
//
//        datalist.add(
//            LandingScreenFragment.TitleSubtitleModel(
//                "Telesales and Support",
//                "",
//                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Findustry2.jpg?alt=media&token=00412b0a-fbbe-4790-9a9b-050fefaf5d02"
//            )
//        )
//
//        val recyclerGenericAdapter: RecyclerGenericAdapter<LandingScreenFragment.TitleSubtitleModel> =
//            RecyclerGenericAdapter<LandingScreenFragment.TitleSubtitleModel>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    showToast("This is under development. Please check again in a few days.")
//                },
//                RecyclerGenericAdapter.ItemInterface<LandingScreenFragment.TitleSubtitleModel?> { obj, viewHolder, position ->
//                    var view = getView(viewHolder, R.id.card_view)
//                    val lp = view.layoutParams
//                    lp.height = lp.height
//                    lp.width = itemWidth
//                    view.layoutParams = lp
//
//                    var title = getTextView(viewHolder, R.id.title)
//                    title.text = obj?.title
//                    obj?.imgStr?.let {
//                        var img = getImageView(viewHolder, R.id.img_view)
//                        showGlideImage(it, img)
//                    }
////                    img.setImageResource(obj?.imgIcon!!)
//                })
//        recyclerGenericAdapter.list = datalist
//        recyclerGenericAdapter.setLayout(R.layout.explore_by_industry_item)
//        explore_by_industry.layoutManager = LinearLayoutManager(
//            activity?.applicationContext,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        explore_by_industry.adapter = recyclerGenericAdapter
//    }

    private fun initBottomMenuClicks() {


        contact_us_bs_calendar_screen.setOnClickListener {
            // navigate(R.id.fakeGigContactScreenFragment)
        }

        invite_contact_bs_calendar_screen.setOnClickListener {
            navigation.navigateTo("referrals")
//            navigate(R.id.referrals_fragment)
        }



        help_topic_bs_calendar_screen.setOnClickListener {
            navigation.navigateTo("all_videos")
//            navigate(R.id.helpVideosFragment)
        }


    }

    private fun initializeExploreByRole() {
        if (AppConstants.UNLOCK_FEATURE) {
            ll_search_role.setOnClickListener {
                navigation.navigateTo("fragment_explore_by_role")
//                navigate(R.id.fragment_explore_by_role)
            }
            landingScreenViewModel.observerRole.observe(viewLifecycleOwner, Observer { items ->
                run {
                    val gig = items?.get(0)
                    ll_search_role.visibility = if (items?.size!! > 1) View.VISIBLE else View.GONE
                    showGlideImage(gig?.role_image ?: "", iv_role)
                    tv_title_role.text = gig?.role_title
                    if (!gig?.job_description.isNullOrEmpty()) {
                        tv_subtitle_role.visible()
                        tv_subtitle_role.text = gig?.job_description?.get(0)
                    }
                    cv_role.setOnClickListener {
                        navigation.navigateTo(
                            "fragment_role_details", bundleOf(
                                StringConstants.ROLE_ID.value to gig?.id!!
                            )
                        )
//                        navigate(
//                            R.id.fragment_role_details, bundleOf(
//                                StringConstants.ROLE_ID.value to gig?.id!!
//                            )
//                        )
                    }
                }
            })
            landingScreenViewModel.getRoles()
            val itemWidth = ((width / 3) * 2).toInt()
            val lp = cv_role.layoutParams
            lp.height = lp.height
            lp.width = itemWidth
            cv_role.layoutParams = lp

        } else {
            explore_by_role_rl.gone()
//            showToast("This is under development. Please check again in a few days.")
        }

    }

    private fun initializeClientActivation() {
        landingScreenViewModel.observableJobProfile.observe(
            viewLifecycleOwner,
            Observer { jobProfile ->


                run {
                    jobProfile?.let {
                        showClientActivations(jobProfile)
                    }

                }


            })
        landingScreenViewModel.getJobProfile()

    }

    private fun showClientActivations(jobProfiles: ArrayList<JobProfile>) {

        client_activation_progress_bar_bs.gone()
        client_activation_error_bs.gone()
        client_activation_rv_bs.visible()

        if (jobProfiles.isEmpty()) {
            rl_cient_activation_bs.gone()
        } else {
            rl_cient_activation_bs.visible()

            val itemWidth = ((width / 3) * 2).toInt()
            // model will change when integrated with DB

//            val recyclerGenericAdapter: RecyclerGenericAdapter<JobProfile> =
//                RecyclerGenericAdapter<JobProfile>(
//                    activity?.applicationContext,
//                    PFRecyclerViewAdapter.OnViewHolderClick<JobProfile?> { view, position, item ->
//                        navigation.navigateTo(
//                            "client_activation",
//                            bundleOf(StringConstants.JOB_PROFILE_ID.value to item?.id)
//                        )
////                        navigate(
////                            R.id.fragment_client_activation,
////                            bundleOf(StringConstants.JOB_PROFILE_ID.value to item?.id)
////                        )
//                    },
//                    RecyclerGenericAdapter.ItemInterface<JobProfile?> { obj, viewHolder, position ->
//
//                        var view = getView(viewHolder, R.id.top_to_cardview)
//                        val lp = view.layoutParams
//                        lp.height = lp.height
//                        lp.width = itemWidth
//                        view.layoutParams = lp
//
//                        showGlideImage(
//                            obj?.cardImage ?: "",
//                            getImageView(viewHolder, R.id.iv_client_activation)
//                        )
//                        getTextView(viewHolder, R.id.tv_client_activation).text = obj?.cardTitle
//                        getTextView(viewHolder, R.id.tv_sub_client_activation).text = obj?.title
//
//                        //img.setImageResource(obj?.imgIcon!!)
//                    })
            var clientActivationAdapter = ClientActivationAdapter(requireContext())
            clientActivationAdapter.data = jobProfiles
//            recyclerGenericAdapter.list = jobProfiles
//            recyclerGenericAdapter.setLayout(R.layout.client_activation_item)
            client_activation_rv_bs.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            client_activation_rv_bs.adapter = clientActivationAdapter

        }
    }
}