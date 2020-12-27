package com.gigforce.app.modules.ambassador_user_enrollment.ambassador_enrollment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.LearningViewModel
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.StringConstants
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_ambassador_program_details.*
import kotlinx.android.synthetic.main.fragment_ambassador_program_details_info.*
import kotlinx.android.synthetic.main.fragment_main_learning_role_based_learnings.*

class AmbassadorProgramDetailsFragment : BaseFragment(),
    Toolbar.OnMenuItemClickListener {

    private val learningViewModel: LearningViewModel by viewModels()
    private val viewModel: AmbassadorProgramViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambassador_program_details, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel()
        initLearningViewModel()
        initObservers()
    }

    private fun initObservers() {
        viewModel.observableAmbassadorProgram.observe(viewLifecycleOwner, Observer {
//            it?.checkForLangTranslation()
            roleNameTV.text = it?.title
            tv_sub_title.text = it?.subTitle
            address_label.text = it?.ambassadorQuestion
            separator30.text = Html.fromHtml(it?.payoutNote)
            gig_address_tv.text = it?.ambassadorAnswer
            btn_apply_now.text = it?.actionButtonText
            gig_resp_label.text = it?.responsibilitiesTitle
            inflateAmbResponsibilities(
                it?.responsibilities ?: listOf()
            )

        })
        viewModel.getAmbassadorProfiles()

    }


    private fun initUi() {

        roleBasedLearningTV.text = getString(R.string.related_learnings)
        ambRequirementsSeeMoreTV.setOnClickListener {

        }

        back_btn_iv.setOnClickListener {
            activity?.onBackPressed()
        }

        btn_apply_now.setOnClickListener {
            navigate(
                R.id.ambassadorEnrollmentRequirementFragment, bundleOf(
                    StringConstants.AMBASSADOR_ID.value to viewModel.observableAmbassadorProgram.value?.id
                )
            )
        }
    }

    private fun initViewModel() {

    }

    private fun initLearningViewModel() {
        learningViewModel
            .roleBasedCourses
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showRoleBasedLearningProgress()
                    is Lce.Content -> showRoleBasedLearnings(it.content.sortedBy { it.priority })
                    is Lce.Error -> showRoleBasedLearningError(it.error)
                }
            })

        learningViewModel.getRoleBasedCourses()
    }

    private fun showRoleBasedLearningError(error: String) {

        learning_based_role_rv.gone()
        role_based_learning_progress_bar.gone()
        role_based_learning_error.visible()

        role_based_learning_error.text = error
    }

    private fun showRoleBasedLearningProgress() {

        learning_based_role_rv.gone()
        role_based_learning_error.gone()
        role_based_learning_progress_bar.visible()
    }

    var width = 0
    private fun showRoleBasedLearnings(content: List<Course>) {
        role_based_learning_progress_bar.gone()
        role_based_learning_error.gone()
        learning_based_role_rv.visible()

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 2).toInt()
        // model will change when integrated with DB

        val recyclerGenericAdapter: RecyclerGenericAdapter<Course> =
            RecyclerGenericAdapter<Course>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    val course = item as Course

                    navigate(
                        R.id.learningCourseDetails,
                        bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to course.id)
                    )
                },
                RecyclerGenericAdapter.ItemInterface<Course> { obj, viewHolder, position ->
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
                })
        recyclerGenericAdapter.list = content
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        learning_based_role_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_based_role_rv.adapter = recyclerGenericAdapter
    }


    private fun showErrorWhileLoadingGigData(error: String) {

    }

    private fun showGigDetailsAsLoading() {

    }

    private fun setGigDetailsOnView(gig: Gig) {


//        if (gig.isPresentGig() || gig.isPastGig()) {
//            toolbar?.menu?.findItem(R.id.action_decline_gig)?.setVisible(false)
//        } else {
//            toolbar.menu?.findItem(R.id.action_decline_gig)?.setVisible(true)
//        }
//
//        toolbar?.title = gig.title
//        roleNameTV.text = gig.title
//        company_rating_tv.text = if (gig.gigRating != 0.0f) gig.gigRating.toString() else "-"
//
//        inflateGigChips(gig)
//
//        gig_req_container.removeAllViews()
//        if (gig.gigRequirements.size > 4) {
//            inflateambRequirements(gig.gigRequirements.take(4))
//            gigRequirementsSeeMoreTV.visible()
//        } else {
//            inflateambRequirements(gig.gigRequirements)
//            gigRequirementsSeeMoreTV.gone()
//        }
//
//        gig_resp_container.removeAllViews()
//        if (gig.gigResponsibilities.size > 4) {
//            inflateGigResponsibilities(gig.gigResponsibilities.take(4))
//            gigResponsiblitiesSeeMoreTV.visible()
//        } else {
//            inflateGigResponsibilities(gig.gigResponsibilities)
//            gigResponsiblitiesSeeMoreTV.gone()
//        }
//
//        val earningRow = if (gig.isMonthlyGig) {
//            "<b>Typical per month earning</b> : ${if (gig.gigAmount != 0.0) "Rs. ${gig.gigAmount}" else "As per contract"}"
//        } else {
//            "<b>Typical per day earning</b> : ${if (gig.gigAmount != 0.0) "Rs. ${gig.gigAmount}" else "As per contract"}"
//        }
//
//        gig_earning_container.removeAllViews()
//        inflateGigPayments(
//            listOf(
//                earningRow
//            )
//        )
//
//        gig_others_container.removeAllViews()
//        inflateGigOthers(
//            listOf(
//                "Dummy Other row",
//                "Dummy Other row 2"
//            )
//        )
//
//        gig_faq_container.removeAllViews()
//        inflateGigFaqs(
//            listOf(
//                "Dummy Faq row",
//                "Dummy Faq row 2"
//            )
//        )
    }


    private fun inflateAmbRequirements(ambRequirements: List<String>) = ambRequirements.forEach {

        if (it.contains(":")) {
            ambReqContainer.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                ambReqContainer.getChildAt(ambReqContainer.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            ambReqContainer.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                ambReqContainer.getChildAt(ambReqContainer.childCount - 1) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }

    private fun inflateAmbResponsibilities(ambResp: List<String>) = ambResp.forEach {

        if (it.contains(":")) {
            ambRespContainer.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                ambRespContainer.getChildAt(ambRespContainer.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            ambRespContainer.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                ambRespContainer.getChildAt(ambRespContainer.childCount - 1) as LinearLayout
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
                navigate(R.id.contactScreenFragment)
                true
            }
            R.id.action_share -> {
                showToast("This feature is under development")
                true
            }
            R.id.action_decline_gig -> {

                true
            }
            else -> false
        }
    }

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }

}