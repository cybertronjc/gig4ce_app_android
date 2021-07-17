package com.gigforce.learning.learning.courseDetails

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learning.R
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.common_ui.viewdatamodels.VideoPlayCardDVM
import com.gigforce.common_ui.viewdatamodels.models.Module
import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.datamodels.learning.CourseContent
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.learning.learning.LearningConstants
import com.gigforce.learning.learning.courseContent.CourseContentListFragment
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_learning_course_details.*
import kotlinx.android.synthetic.main.layout_fragment_course_details.*
import javax.inject.Inject

@AndroidEntryPoint
class LearningCourseDetailsFragment : Fragment(), IOnBackPressedOverride {

    private var mCurrentModuleNo: Int = -1
    private lateinit var mCourseId: String
    private var mModuleId: String? = null
    private var FROM_CLIENT_ACTIVATION = false

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: CourseDetailsViewModel by viewModels()

    private val mAdapter: LearningDetailsLessonsAdapter by lazy {
        LearningDetailsLessonsAdapter(requireContext())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_learning_course_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATION =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            mCourseId = it.getString(INTENT_EXTRA_COURSE_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        arguments?.let {
            FROM_CLIENT_ACTIVATION =
                    it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            mCourseId = it.getString(INTENT_EXTRA_COURSE_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        initView()
        initViewModel()

        if (mModuleId != null) {
            //Hide Module
            viewModel.getCourseDetails(mCourseId)
            viewModel.getCourseLessonsAndAssessments(mCourseId, mModuleId!!)
        } else {
            //   viewModel.getCourseDetails(mCourseId)
            // viewModel.getCourseModules(mCourseId)

            viewModel.getCourseDetailsAndModules(mCourseId)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_COURSE_ID, mCourseId)
        outState.putString(INTENT_EXTRA_MODULE_ID, mModuleId)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATION)
    }

    private fun initView() {
//        ViewCompat.setNestedScrollingEnabled(learning_details_lessons_rv, false)
//        ViewCompat.setNestedScrollingEnabled(learning_details_assessments_rv, false)
//
//        learning_details_lessons_rv.layoutManager = LinearLayoutManager(
//            requireContext(),
//            RecyclerView.VERTICAL,
//            false
//        )
//
//        mAdapter.setOnLearningVideoActionListener {
//            when (it.type) {
//                CourseContent.TYPE_ASSESSMENT -> {
//                    navigation.navigateTo(
//                        "learning/assessment", bundleOf(
//                            StringConstants.INTENT_LESSON_ID.value to it.id,
//                            StringConstants.INTENT_MODULE_ID.value to it.moduleId
//                        )
//                    )
//                }
//                CourseContent.TYPE_SLIDE -> {
//                    navigation.navigateTo(
//                        "learning/assessmentslides",
//                        bundleOf(
//                            SlidesFragment.INTENT_EXTRA_SLIDE_TITLE to it.title,
//                            SlidesFragment.INTENT_EXTRA_MODULE_ID to it.moduleId,
//                            SlidesFragment.INTENT_EXTRA_LESSON_ID to it.id
//                        )
//                    )
//                }
//                CourseContent.TYPE_VIDEO -> {
//                    PlayVideoDialogFragment.launch(
//                        childFragmentManager = childFragmentManager,
//                        moduleId = it.moduleId,
//                        lessonId = it.id,
//                        shouldShowFeedbackDialog = it.shouldShowFeedbackDialog
//                    )
//
////                    navigate(
////                        R.id.playVideoDialogFragment,
////                        bundleOf(
////                            PlayVideoDialogFragment.INTENT_EXTRA_LESSON_ID to it.id,
////                            PlayVideoDialogFragment.INTENT_EXTRA_MODULE_ID to it.moduleId
////                        )
////                    )
//                }
//                else -> {
//                }
//            }
//        }
        //learning_details_lessons_rv.adapter = mAdapter


        learningBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        assessmentSeeMoreButton.setOnClickListener {

            if (viewModel.currentlySelectedModule != null) {
                navigation.navigateTo(
                        "learning/assessmentListFragment", bundleOf(
                        StringConstants.INTENT_COURSE_ID.value to viewModel.currentlySelectedModule?.courseId,
                        StringConstants.INTENT_MODULE_ID.value to viewModel.currentlySelectedModule?.id
                )
                )
            }
        }

        lessonsSeeMoreButton.setOnClickListener {
            navigation.navigateTo(
                    "learning/courseContentListFragment",
                    bundleOf(
                            CourseContentListFragment.INTENT_EXTRA_COURSE_ID to viewModel.currentlySelectedModule?.courseId,
                            CourseContentListFragment.INTENT_EXTRA_MODULE_ID to viewModel.currentlySelectedModule?.id
                    )
            )
        }
    }

    private fun initViewModel() {
        viewModel
                .courseDetails
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lce.Loading -> showCourseDetailsAsLoading()
                        is Lce.Content -> showCourseDetails(it.content)
                        is Lce.Error -> showErrorInLoadingCourseDetails(it.error)
                    }
                })

        viewModel.courseModules
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
                        Lce.Loading -> showModulesAsLoading()
                        is Lce.Content -> showModulesOnView(it.content)
                        is Lce.Error -> showErrorInLoadingModules(it.error)
                    }
                })

        viewModel
                .courseLessons
                .observe(viewLifecycleOwner, Observer {

                    when (it) {
//                    Lce.Loading -> showLessonsAsLoading()
//                    is Lce.Content -> showLessonsOnView(it.content)
//                    is Lce.Error -> showErrorInLoadingLessons(it.error)
                    }
                })

//        viewModel
//            .courseAssessments
//            .observe(viewLifecycleOwner, Observer {
//
//                when (it) {
//                    Lce.Loading -> showAssessmentsAsLoading()
//                    is Lce.Content -> showAssessmentsOnView(it.content)
//                    is Lce.Error -> showErrorInLoadingAssessments(it.error)
//                }
//            })

        //loading and showing lessons and assessments
        viewModel.courseLessonsAndAssessments.observe(viewLifecycleOwner, Observer { it ->
            Log.d("list1234", it.toString())
            if (it.isNotEmpty()) {
                var sublist = it
                if (it.size >= 4)
                    sublist = it.subList(0, 4)
                learning_all_lesson_rv.visible()
                learning_details_learning_error.gone()
                sublist.forEach{ if(it is VideoPlayCardDVM) it.fragment = this }
                learning_all_lesson_rv.collection = sublist
            } else {
                learning_all_lesson_rv.gone()
                learning_details_learning_error.visible()
            }

        })
    }

    private fun showCourseDetails(course: Course) {
        course_details_error.gone()
        course_details_progress_bar.gone()
        course_details_main_layout.visible()

        if (!course.coverPicture.isNullOrBlank()) {
            if (course.coverPicture!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                        .load(course.coverPicture)
                        .placeholder(getCircularProgressDrawable())
                        .error(R.drawable.ic_learning_default_back)
                        .into(videoThumnailIV)
            } else {
                FirebaseStorage.getInstance()
                        .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                        .child(course.coverPicture!!)
                        .downloadUrl
                        .addOnSuccessListener { fileUri ->

                            GlideApp.with(requireContext())
                                    .load(fileUri)
                                    .placeholder(getCircularProgressDrawable())
                                    .error(R.drawable.ic_learning_default_back)
                                    .into(videoThumnailIV)
                        }
            }
        } else {
            GlideApp.with(requireContext())
                    .load(R.drawable.ic_learning_default_back)
                    .into(videoThumnailIV)
        }

        videoTitleTV.text = course.name

        if (!course.description.isNullOrBlank()) {
            if (course.description!!.length > 150) {
                videoDescTV.text = prepareDescription(course.description!!.substring(0, 150))
            } else {
                videoDescTV.text = course.description
            }
        }

        videoDescTV.setOnClickListener {
            val desc = viewModel.mLastReqCourseDetails?.description ?: return@setOnClickListener

            if (desc.length > 150) {

                if (videoDescTV.text.length < 160) {
                    // Collapsed
                    videoDescTV.text = prepareDescription(desc)
                } else {
                    //Expanded
                    videoDescTV.text = prepareDescription(desc.substring(0, 150))
                }
            }
        }

        tv1HS1.text = course.name
        levelTV.text = "Module $mCurrentModuleNo of ${course.moduleCount}"
    }

    private fun prepareDescription(description: String): SpannableString {
        if (description.isBlank())
            return SpannableString("")

        val string = if (description.length > 150) {
            SpannableString(description + SEE_LESS)
        } else {
            SpannableString(description + SEE_MORE)
        }

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.lipstick, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), description.length + 1, string.length, 0)
        string.setSpan(UnderlineSpan(), description.length + 1, string.length, 0)

        return string
    }

    private fun showErrorInLoadingCourseDetails(error: String) {

        course_details_main_layout.gone()
        course_details_progress_bar.gone()

        course_details_error.visible()
        course_details_error.text = error
    }

    private fun showCourseDetailsAsLoading() {

        course_details_main_layout.gone()
        course_details_error.gone()
        course_details_progress_bar.visible()
    }

//    private fun showLessonsAsLoading() {
//
//        learning_details_lessons_rv.gone()
//        learning_details_learning_error.gone()
//        learning_details_progress_bar.visible()
//    }
//
//
//    private fun showLessonsOnView(content: List<CourseContent>) {
//
//        learning_details_learning_error.gone()
//        learning_details_progress_bar.gone()
//        learning_details_lessons_rv.visible()
//
//        loadModulesInfoInView()
//
//        if (content.isEmpty()) {
//            mAdapter.updateCourseContent(emptyList())
//            learning_details_lessons_rv.gone()
//            learning_details_progress_bar.gone()
//
//            learning_details_learning_error.visible()
//            learning_details_learning_error.text = "No Lessons Found"
//            lessonsSeeMoreButton.gone()
//
//        } else if (content.size > 4) {
//            lessonsSeeMoreButton.visible()
//            mAdapter.updateCourseContent(content.sortedBy { it.priority }.take(4))
//        } else {
//            lessonsSeeMoreButton.gone()
//            mAdapter.updateCourseContent(content.sortedBy { it.priority })
//        }
//    }

    private fun loadModulesInfoInView() {

        val moduleNo =
                if (viewModel.currentModules != null && viewModel.currentlySelectedModule != null) {
                    viewModel.currentModules!!.indexOf(viewModel.currentlySelectedModule!!) + 1
                } else 0

        levelTV.text = "Module $moduleNo Of ${viewModel.currentModules?.size}"

        var lessonsCompleted = 0
        var totalLessons = 0

        var assignmentsCompleted = 0
        var totalAssignments = 0

        if (viewModel.currentlySelectedModule != null) {

            val currentModuleProgress = viewModel.mCurrentModulesProgressData?.find { it.moduleId == viewModel.currentlySelectedModule!!.id }
                    ?: return

            currentModuleProgress.lessonsProgress.filter { it.isActive }.forEach { lessonProg ->

                if (lessonProg.lessonType == CourseContent.TYPE_VIDEO) {
                    totalLessons++

                    if (lessonProg.completed)
                        lessonsCompleted++
                } else if (lessonProg.lessonType == CourseContent.TYPE_ASSESSMENT) {
                    totalAssignments++

                    if (lessonProg.completed)
                        assignmentsCompleted++
                }

                complitionStatusTv.text =
                        "$lessonsCompleted/$totalLessons Lessons Completed"
                assessmentCountTv.text =
                        if (viewModel.currentAssessments?.size == null || totalAssignments == 0)
                            "0 Assessments"
                        else if (assignmentsCompleted == 1)
                            "$assignmentsCompleted/$totalAssignments Assessment Completed"
                        else
                            "$assignmentsCompleted/$totalAssignments Assessments Completed"

                lessonsLabel.text = "Lesson (${viewModel.currentlySelectedModule?.title})"
            }
        }
    }

//    private fun showErrorInLoadingLessons(error: String) {
//
//        learning_details_lessons_rv.gone()
//        learning_details_progress_bar.gone()
//
//        learning_details_learning_error.visible()
//        learning_details_learning_error.text = error
//    }


    private fun showModulesAsLoading() {

        learning_details_modules_rv.gone()
        learning_modules_learning_error.gone()
        learning_modules_progress_bar.visible()
    }

    private fun showModulesOnView(content: List<Module>) {

        learning_modules_learning_error.gone()
        learning_modules_progress_bar.gone()
        learning_details_modules_rv.visible()

        setModulesOnView(content)
    }

    private fun showErrorInLoadingModules(error: String) {

        learning_details_modules_rv.gone()
        learning_modules_progress_bar.gone()

        learning_modules_learning_error.visible()
        learning_modules_learning_error.text = error
    }


    //    private var recyclerGenericAdapter: RecyclerGenericAdapter<Module>? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private fun setModulesOnView(content: List<Module>) {
//        var width: Int = 0
//        val displayMetrics = DisplayMetrics()
//        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//        width = displayMetrics.widthPixels
//        val itemWidth = ((width / 3) * 1.7).toInt()
//        // model will change when integrated with DB
//
//        recyclerGenericAdapter =
//            RecyclerGenericAdapter<Module>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    //navigate(R.id.learningVideoFragment)
//                    val module = item as Module
//                    viewModel.currentlySelectedModule = module
//
//                    viewModel.getCourseLessonsAndAssessments(
//                        courseId = mCourseId,
//                        moduleId = module.id
//                    )
//
//                    course_details_main_layout.post {
//                        course_details_main_layout.scrollTo(0, modulesLabel.y.toInt())
//                    }
//
//                    var oldPostion = viewModel.currentlySelectedModulePosition
//                    viewModel.currentlySelectedModulePosition = position
//
//                    if (oldPostion != viewModel.currentlySelectedModulePosition) {
//                        recyclerGenericAdapter?.notifyItemChanged(oldPostion)
//                        recyclerGenericAdapter?.notifyItemChanged(viewModel.currentlySelectedModulePosition)
//                        linearLayoutManager?.scrollToPositionWithOffset(
//                            viewModel.currentlySelectedModulePosition,
//                            40
//                        )
////                        learning_details_modules_rv.scrollTP(viewModel.currentlySelectedModulePosition)
//                    }
//                },
//                RecyclerGenericAdapter.ItemInterface<Module> { obj, viewHolder, position ->
//                    var view = getView(viewHolder, R.id.card_view)
//                    val lp = view.layoutParams
//                    lp.height = lp.height
//                    lp.width = itemWidth
//                    view.layoutParams = lp
//
//                    var title = getTextView(viewHolder, R.id.title_)
//                    title.text = obj?.title
//
//                    var subtitle = getTextView(viewHolder, R.id.title)
//                    subtitle.text = "${obj.lessonsCompleted} / ${obj.totalLessons} Completed"
//
//                    var img = getImageView(viewHolder, R.id.learning_img)
//
//
//                    var completedIV = getImageView(viewHolder, R.id.module_completed_iv)
//                    var completedPercTV = getTextView(viewHolder, R.id.module_completed_perc_tv)
//
//                    if (obj.totalLessons != 0 && obj.lessonsCompleted == obj.totalLessons) {
//                        completedPercTV.text = "100%"
//                        completedPercTV.setTextColor(
//                            ResourcesCompat.getColor(
//                                resources,
//                                R.color.green,
//                                null
//                            )
//                        )
//                        completedIV.setImageResource(R.drawable.ic_successful_green_tick)
//                    } else {
//                        val completedPercentage =
//                            if (obj.totalLessons != 0) (obj.lessonsCompleted * 100) / obj.totalLessons else 0
//
//                        completedPercTV.setTextColor(
//                            ResourcesCompat.getColor(
//                                resources,
//                                R.color.app_orange,
//                                null
//                            )
//                        )
//                        completedPercTV.text = "$completedPercentage%"
//                        completedIV.setImageResource(R.drawable.ic_clock_orange)
//                    }
//
//
//                    var borderView = getView(viewHolder, R.id.borderFrameLayout)
//                    if (viewModel.currentlySelectedModulePosition == position) {
//                        //Set Module as selected
//                        borderView.visible()
//                    } else {
//                        borderView.gone()
//                    }
//
//                    if (!obj.coverPicture.isNullOrBlank()) {
//                        if (obj.coverPicture!!.startsWith("http", true)) {
//
//                            GlideApp.with(requireContext())
//                                .load(obj.coverPicture)
//                                .thumbnail(
//                                    GlideApp.with(requireContext()).load(R.drawable.ic_loading)
//                                )
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
//                                        .thumbnail(
//                                            GlideApp.with(requireContext())
//                                                .load(R.drawable.ic_loading)
//                                        )
//                                        .error(R.drawable.ic_learning_default_back)
//                                        .into(img)
//                                }
//                        }
//                    } else {
//                        GlideApp.with(requireContext())
//                            .load(R.drawable.ic_learning_default_back)
//                            .into(img)
//                    }
//
//                })
//        recyclerGenericAdapter?.list = content
//        recyclerGenericAdapter?.setLayout(R.layout.recycler_item_course_module)
//        linearLayoutManager = LinearLayoutManager(
//            activity?.applicationContext,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        learning_details_modules_rv.layoutManager = linearLayoutManager
//        learning_details_modules_rv.adapter = recyclerGenericAdapter

        learning_details_modules_rv.collection = getAllModules(content)
        learning_details_modules_rv.itemClickListener = object : ItemClickListener {
            override fun onItemClick(view: View, position: Int, dataModel: Any) {
                val module = content.get(position)
                viewModel.currentlySelectedModule = module

                viewModel.getCourseLessonsAndAssessments(
                        courseId = mCourseId,
                        moduleId = module.id
                )

                course_details_main_layout.post {
                    course_details_main_layout.scrollTo(0, modulesLabel.y.toInt())
                }

                var oldPostion = viewModel.currentlySelectedModulePosition
                viewModel.currentlySelectedModulePosition = position

                if (oldPostion != viewModel.currentlySelectedModulePosition) {
                    (learning_details_modules_rv?.collection?.get(oldPostion) as FeatureItemCardDVM).isSelectedView =
                            false
                    (learning_details_modules_rv?.collection?.get(position) as FeatureItemCardDVM).isSelectedView =
                            true

                    learning_details_modules_rv?.coreAdapter?.notifyItemChanged(oldPostion)
                    learning_details_modules_rv?.coreAdapter?.notifyItemChanged(viewModel.currentlySelectedModulePosition)
                    (learning_details_modules_rv?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
                            viewModel.currentlySelectedModulePosition,
                            40
                    )
//                        learning_details_modules_rv.scrollTP(viewModel.currentlySelectedModulePosition)
                }
            }
        }


    }

    private fun getAllModules(content: List<Module>): ArrayList<FeatureItemCardDVM> {
        var moduleList = ArrayList<FeatureItemCardDVM>()
//        var abc = "$e.lessonsCompleted} / ${e.totalLessons} Completed"
        content.forEach { e ->
            moduleList.add(
                    FeatureItemCardDVM(
                            image = e.coverPicture,
                            title = e.title,
                            subtitle = "${e.lessonsCompleted} / ${e.totalLessons} Completed"
                    )
            )
        }
        moduleList.get(0).isSelectedView = true
        return moduleList
    }


    private fun showAssessmentsAsLoading() {

        learning_details_assessments_rv.gone()
        learning_details_assessments_error.gone()
        learning_details_assessments_progress_bar.visible()
    }

//    private fun showAssessmentsOnView(content: List<CourseContent>) {
//
//        learning_details_assessments_error.gone()
//        learning_details_assessments_progress_bar.gone()
//        learning_details_assessments_rv.visible()
//
//        if (content.isEmpty()) {
//            showAssessments(emptyList())
//            assessmentSeeMoreButton.gone()
//            learning_details_assessments_error.visible()
//            learning_details_assessments_error.text = "No assessments found"
//        } else if (content.size > 4) {
//            assessmentSeeMoreButton.visible()
//            showAssessments(content.sortedBy { it.priority }.take(4))
//        } else {
//            assessmentSeeMoreButton.gone()
//            showAssessments(content.sortedBy { it.priority })
//        }
//    }

    private fun showErrorInLoadingAssessments(error: String) {

        learning_details_assessments_rv.gone()
        learning_details_assessments_progress_bar.gone()

        learning_details_assessments_error.visible()
        learning_details_assessments_error.text = error
    }


//    private fun showAssessments(content: List<CourseContent>) {
//
//        val recyclerGenericAdapter: RecyclerGenericAdapter<CourseContent> =
//            RecyclerGenericAdapter<CourseContent>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//
//                    val assessment = item as CourseContent
//
//                    navigate(
//                        R.id.assessment_fragment, bundleOf(
//                            AssessmentFragment.INTENT_LESSON_ID to assessment.id,
//                            AssessmentFragment.INTENT_MODULE_ID to assessment.moduleId
//                        )
//                    )
//
//                },
//                RecyclerGenericAdapter.ItemInterface<CourseContent> { obj, viewHolder, position ->
//
//                    getTextView(viewHolder, R.id.title).text = obj?.title
//                    getTextView(viewHolder, R.id.time).text = obj?.videoLengthString
//
//
//                    if (obj.completed) {
//                        getTextView(viewHolder, R.id.status).text = "COMPLETED"
//                        getTextView(
//                            viewHolder,
//                            R.id.status
//                        ).setBackgroundResource(R.drawable.rect_assessment_status_completed)
//                        (getView(
//                            viewHolder,
//                            R.id.side_bar_status
//                        ) as ImageView).setImageResource(R.drawable.assessment_line_done)
//                    } else {
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
//                    }
//
//                })
//        recyclerGenericAdapter.list = content
//        recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
//        learning_details_assessments_rv.layoutManager = LinearLayoutManager(
//            activity?.applicationContext,
//            LinearLayoutManager.VERTICAL,
//            false
//        )
//        learning_details_assessments_rv.adapter = recyclerGenericAdapter
//
//
//    }

//    override fun onBackPressed(): Boolean {
//        if (FROM_CLIENT_ACTIVATION) {
//            navFragmentsData.setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
//            popBackState()
//            return true
//        }
//        return super.onBackPressed()
//    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATION) {
            (activity as NavFragmentsData).setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
            navigation.popBackStack()
            return true
        }
        return false
    }

    companion object {

        const val INTENT_EXTRA_COURSE_ID = "course_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"

        const val SEE_MORE = " See more"
        const val SEE_LESS = " See less"
    }
}