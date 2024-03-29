package com.gigforce.giger_gigs

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.giger_gigs.adapters.GigDetailAdapter
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragment
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragmentResultListener
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.common_ui.utils.openPopupMenu
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.extensions.inflate
import com.gigforce.core.extensions.toLocalDateTime
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_gig_page_2_details.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_info.*
import kotlinx.android.synthetic.main.fragment_gig_page_2_keywords.*
import kotlinx.android.synthetic.main.fragment_main_learning_role_based_learnings.*
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class GigDetailsFragment : Fragment(),
    DeclineGigDialogFragmentResultListener, PopupMenu.OnMenuItemClickListener {

    private val viewModel: GigViewModel by viewModels()
//    private val learningViewModel: LearningViewModel by viewModels()
    private lateinit var gigId: String

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_page_2_details, container,false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
        initLearningViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            gigId = it.getString(GigPage2Fragment.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigPage2Fragment.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        if (::gigId.isLateinit.not()) {
            FirebaseCrashlytics.getInstance()
                .setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            FirebaseCrashlytics.getInstance().log("GigPage2Fragment: No Gig id found")
        }
    }

    private fun initUi() {
        iv_back_gig_details.setOnClickListener {
            activity?.onBackPressed()
        }


        roleBasedLearningTV.text = getString(R.string.related_learnings_giger_gigs)
        iv_options_gig_details.setOnClickListener {
            openPopupMenu(
                it,
                R.menu.menu_gig_attendance,
                GigDetailsFragment@ this,
                requireActivity()
            )
        }
        gigRequirementsSeeMoreTV.setOnClickListener {

            if (viewModel.currentGig == null)
                return@setOnClickListener

            if (gig_req_container.childCount == 4) {
                //Collapsed
                inflateGigRequirements(
                    viewModel.currentGig!!.gigRequirements.subList(
                        4,
                        viewModel.currentGig!!.gigRequirements.size
                    )
                )
                gigRequirementsSeeMoreTV.text = getString(R.string.plus_see_less_giger_gigs)
            } else {
                //Expanded
                gig_req_container.removeViews(4, gig_req_container.childCount - 4)
                gigRequirementsSeeMoreTV.text = getString(R.string.plus_see_more_giger_gigs)
            }
        }


    }

    private fun initViewModel() {
        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> showGigDetailsAsLoading()
                    is Lce.Content -> setGigDetailsOnView(it.content)
                    is Lce.Error -> showErrorWhileLoadingGigData(it.error)
                }
            })


        viewModel.getGigWithDetails(gigId)
    }

    private fun initLearningViewModel() {
//        learningViewModel
//            .roleBasedCourses
//            .observe(viewLifecycleOwner, Observer {
//
//                when (it) {
//                    Lce.Loading -> showRoleBasedLearningProgress()
//                    is Lce.Content -> showRoleBasedLearnings(it.content.sortedBy { it.priority })
//                    is Lce.Error -> showRoleBasedLearningError(it.error)
//                }
//            })
//
//        learningViewModel.getRoleBasedCourses()
    }

    var width = 0
    private fun showRoleBasedLearningError(error: String) {

        learning_based_role_rv.gone()
        stopShimmer(learning_based_horizontal_progress as LinearLayout,R.id.shimmer_controller)
        role_based_learning_error.visible()
        role_based_learning_error.text = error

    }

    private fun showRoleBasedLearningProgress() {
        startShimmer(
            learning_based_horizontal_progress as LinearLayout,
            ShimmerDataModel(
                minHeight = R.dimen.size_148,
                minWidth = R.dimen.size_300,
                marginRight = R.dimen.size_1,
                orientation = LinearLayout.HORIZONTAL
            ),R.id.shimmer_controller
        )
        learning_based_role_rv.gone()
        role_based_learning_error.gone()

    }

    private fun showRoleBasedLearnings(content: List<Course>) {
        learning_based_horizontal_progress.gone()
        role_based_learning_error.gone()
        learning_based_role_rv.visible()
        stopShimmer(learning_based_horizontal_progress as LinearLayout,R.id.shimmer_controller)



        if (content.size != 0) {
            learning_based_role_layout.visible()
        } else {
            learning_based_role_layout.gone()
        }

        context?.let {
            var gigDetailAdapter = GigDetailAdapter(it)
            gigDetailAdapter.setOnClickListener( object : GigDetailAdapter.IOnclickListener {
                override fun onclickListener(course: Course) {
                    navigation.navigateTo("learning/coursedetails", bundleOf(
                        "course_id" to course.id))
                }

            })
            gigDetailAdapter.data = content
            learning_based_role_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_based_role_rv.adapter = gigDetailAdapter

        }

//        val displayMetrics = DisplayMetrics()
//        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//        width = displayMetrics.widthPixels
//        val itemWidth = ((width / 3) * 2).toInt()
//        // model will change when integrated with DB
//
//        val recyclerGenericAdapter: RecyclerGenericAdapter<Course> =
//            RecyclerGenericAdapter<Course>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    val course = item as Course
//
//                    navigate(
//                        R.id.learningCourseDetails,
//                        bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to course.id)
//                    )
//                },
//                RecyclerGenericAdapter.ItemInterface<Course?> { obj, viewHolder, position ->
//                    var view = getView(viewHolder, R.id.card_view)
//                    val lp = view.layoutParams
//                    lp.height = lp.height
//                    lp.width = itemWidth
//                    view.layoutParams = lp
//
//                    var title = getTextView(viewHolder, R.id.title_)
//                    title.text = obj?.name
//
//                    var subtitle = getTextView(viewHolder, R.id.title)
//                    subtitle.text = obj?.level
//
//                    var comImg = getImageView(viewHolder, R.id.completed_iv)
//                    comImg.isVisible = obj?.completed ?: false
//
//                    var img = getImageView(viewHolder, R.id.learning_img)
//                    if (!obj!!.coverPicture.isNullOrBlank()) {
//                        if (obj.coverPicture!!.startsWith("http", true)) {
//
//                            GlideApp.with(requireContext())
//                                .load(obj.coverPicture!!)
//                                .placeholder(getCircularProgressDrawable())
//                                .error(R.drawable.ic_learning_default_back)
//                                .into(img)
//                        } else {
//                            FirebaseStorage.getInstance()
//                                .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
//                                .child(obj.coverPicture!!)
//                                .downloadUrl
//                                .addOnSuccessListener { fileUri ->
//
//                                    GlideApp.with(requireContext())
//                                        .load(fileUri)
//                                        .placeholder(getCircularProgressDrawable())
//                                        .error(R.drawable.ic_learning_default_back)
//                                        .into(img)
//                                }
//                        }
//                    } else {
//                        GlideApp.with(requireContext())
//                            .load(R.drawable.ic_learning_default_back)
//                            .into(img)
//                    }
//                })
//        recyclerGenericAdapter.list = content
//        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
//        learning_based_role_rv.layoutManager = LinearLayoutManager(
//            activity?.applicationContext,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        learning_based_role_rv.adapter = recyclerGenericAdapter


    }


    private fun showErrorWhileLoadingGigData(error: String) {

    }

    private fun showGigDetailsAsLoading() {

    }

    private fun setGigDetailsOnView(gig: Gig) {

        tv_title_gig_details.text = gig.getGigTitle()
        roleNameTV.text = gig.getGigTitle()
        company_rating_tv.text = if (gig.gigRating != 0.0f) gig.gigRating.toString() else "-"
        gig_desc_tv.text = gig.description

        inflateGigChips(gig)
        inflateKeywords(gig.keywords)

        if (!gig.bannerImage.isNullOrBlank())
            Glide.with(requireContext()).load(gig.bannerImage).into(gigBannerImageIV)

        gig_req_container.removeAllViews()
        if (gig.gigRequirements.size > 4) {
            inflateGigRequirements(gig.gigRequirements.take(4))
            gigRequirementsSeeMoreTV.visible()
        } else {
            inflateGigRequirements(gig.gigRequirements)
            gigRequirementsSeeMoreTV.gone()
        }

        gig_resp_container.removeAllViews()
        if (gig.gigResponsibilities.size > 4) {
            inflateGigResponsibilities(gig.gigResponsibilities.take(4))
            gigResponsiblitiesSeeMoreTV.visible()
        } else {
            inflateGigResponsibilities(gig.gigResponsibilities)
            gigResponsiblitiesSeeMoreTV.gone()
        }

        val earningRow = if (gig.payoutDetails != null) {
            gig.payoutDetails!!
        } else {
            "<b>Typical per day earning</b> : ${if (gig.gigAmount != 0.0) "Rs. ${gig.gigAmount}" else "As per contract"}"
        }

        gig_earning_container.removeAllViews()
        inflateGigPayments(
            listOf(
                earningRow
            )
        )

//        gig_others_container.removeAllViews()
//        inflateGigOthers(
//                listOf(
//                        "Dummy Other row",
//                        "Dummy Other row 2"
//                )
//        )
//
//        gig_faq_container.removeAllViews()
//        inflateGigFaqs(
//                listOf(
//                        "Dummy Faq row",
//                        "Dummy Faq row 2"
//                )
//        )
    }

    private fun inflateKeywords(keywords: List<String>) {
        keywords.forEach {

            val chip = layoutInflater.inflate(
                R.layout.fragment_gig_page_2_details_chips,
                gig_keywords_group,
                false
            ) as Chip
            chip.text = it
            gig_keywords_group.addView(chip)
        }
    }

    private fun inflateGigChips(gig: Gig) {
        if (gig.gigType != null) {
            var chip = layoutInflater.inflate(
                R.layout.fragment_gig_page_2_details_chips,
                gig_chip_group,
                false
            ) as Chip
            chip.text = gig.gigType
            gig_chip_group.addView(chip)
        }

        var chip: Chip
        chip = layoutInflater.inflate(
            R.layout.fragment_gig_page_2_details_chips,
            gig_chip_group,
            false
        ) as Chip
        chip.text = if (gig.isMonthlyGig) getString(R.string.monthly_giger_gigs) else getString(R.string.daily_giger_gigs)
        gig_chip_group.addView(chip)


        if (gig.gigAmount != 0.0) {
            chip = layoutInflater.inflate(
                R.layout.fragment_gig_page_2_details_chips,
                gig_chip_group,
                false
            ) as Chip
            chip.text = "Rs ${gig.gigAmount}"
            gig_chip_group.addView(chip)
        }
    }

    private fun inflateGigRequirements(gigRequirements: List<String>) = gigRequirements.forEach {

        if (it.contains(":")) {
            gig_req_container.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                gig_req_container.getChildAt(gig_req_container.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            gig_req_container.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gig_req_container.getChildAt(gig_req_container.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }

    private fun inflateGigResponsibilities(gigResp: List<String>) = gigResp.forEach {

        if (it.contains(":")) {
            gig_resp_container.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                gig_resp_container.getChildAt(gig_resp_container.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            gig_resp_container.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gig_resp_container.getChildAt(gig_resp_container.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }

    private fun inflateGigPayments(gigResp: List<String>) = gigResp.forEach {

        if (it.contains(":")) {
            gig_earning_container.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                gig_earning_container.getChildAt(gig_earning_container.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            gig_earning_container.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gig_earning_container.getChildAt(gig_earning_container.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }

    private fun inflateGigOthers(gigResp: List<String>) = gigResp.forEach {

        if (it.contains(":")) {
            gig_others_container.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                gig_others_container.getChildAt(gig_others_container.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            gig_others_container.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gig_others_container.getChildAt(gig_others_container.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }


    private fun inflateGigFaqs(gigResp: List<String>) = gigResp.forEach {

        if (it.contains(":")) {
            gig_faq_container.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                gig_faq_container.getChildAt(gig_faq_container.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            gig_faq_container.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                gig_faq_container.getChildAt(gig_faq_container.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }

    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        item ?: return false

        return when (item.itemId) {
            R.id.action_help -> {
              //  navigate(R.id.contactScreenFragment)
                true
            }
            R.id.action_share -> {
                showToast(getString(R.string.under_development_giger_gigs))
                true
            }
            R.id.action_decline_gig -> {
                if (viewModel.currentGig == null)
                    return true

                if (viewModel.currentGig!!.startDateTime.toLocalDateTime() < LocalDateTime.now()) {
                    //Past or ongoing gig

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.alert_giger_gigs))
                        .setMessage(getString(R.string.cannot_decline_past_gigs_giger_gigs))
                        .setPositiveButton(getString(R.string.okay_text_giger_gigs)) { _, _ -> }
                        .show()

                    return true
                }

                if (viewModel.currentGig != null) {
                    declineGigDialog()
                }
                true
            }
            else -> false
        }
    }

    private fun declineGigDialog() {
        //event
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            val newMap = mapOf("Giger ID" to it, "Gig ID" to gigId)
            eventTracker.pushEvent(TrackingEventArgs("giger_attempted_decline",newMap))
        }
        DeclineGigDialogFragment.launch(gigId, childFragmentManager, this)
    }

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

    override fun gigDeclined() {
    }
}