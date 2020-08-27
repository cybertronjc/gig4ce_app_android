package com.gigforce.app.modules.learning.courseDetails

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.modules.Course
import com.gigforce.app.modules.learning.modules.CourseContent
import com.gigforce.app.modules.learning.modules.Module
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_learning_course_details.*
import kotlinx.android.synthetic.main.fragment_learning_course_details_main.*

class LearningCourseDetailsFragment : BaseFragment() {

    private var mCurrentModuleNo: Int = -1
    private lateinit var mCourseId: String
    private var mModuleId: String? = null

    private val viewModel: CourseDetailsViewModel by viewModels()

    private val mAdapter: LearningDetailsLessonsAdapter by lazy {
        LearningDetailsLessonsAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_learning_course_details, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            mCourseId = it.getString(INTENT_EXTRA_COURSE_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        arguments?.let {
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
            viewModel.getCourseDetails(mCourseId)
            viewModel.getCourseModules(mCourseId)
        }
    }

    private fun initView() {
        ViewCompat.setNestedScrollingEnabled(learning_details_lessons_rv, false)

        learning_details_lessons_rv.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        mAdapter.setOnLearningVideoActionListener {

            if (it == 0)
                navigate(R.id.slidesFragment)
            else
                navigate(R.id.playVideoDialogFragment)
        }
        learning_details_lessons_rv.adapter = mAdapter


        learningBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        playFab.setOnClickListener {
            navigate(R.id.playVideoDialogFragment)
        }

        assessmentSeeMoreButton.setOnClickListener {
            navigate(R.id.assessmentListFragment)
        }

        lessonsSeeMoreButton.setOnClickListener {
            navigate(R.id.courseContentListFragment)
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
                    Lce.Loading -> showLessonsAsLoading()
                    is Lce.Content -> showLessonsOnView(it.content)
                    is Lce.Error -> showErrorInLoadingLessons(it.error)
                }
            })

        viewModel
            .courseAssessments
            .observe(viewLifecycleOwner, Observer {

                when(it){
                    Lce.Loading -> showAssessmentsAsLoading()
                    is Lce.Content -> showAssessmentsOnView(it.content)
                    is Lce.Error -> showErrorInLoadingAssessments(it.error)
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
                            .into(videoThumnailIV)
                    }
            }
        }

        videoTitleTV.text = course.name
        videoDescTV.text = course.description
        levelTV.text = "Module $mCurrentModuleNo of ${course.moduleCount}"
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

    private fun showLessonsAsLoading() {

        learning_details_lessons_rv.gone()
        learning_details_learning_error.gone()
        learning_details_progress_bar.visible()
    }

    private fun showLessonsOnView(content: List<CourseContent>) {

        learning_details_learning_error.gone()
        learning_details_progress_bar.gone()
        learning_details_lessons_rv.visible()

        if (content.size > 4) {
            lessonsSeeMoreButton.visible()
            mAdapter.updateCourseContent(content.take(4))
        } else {
            lessonsSeeMoreButton.gone()
            mAdapter.updateCourseContent(content)
        }
    }

    private fun showErrorInLoadingLessons(error: String) {

        learning_details_lessons_rv.gone()
        learning_details_progress_bar.gone()

        learning_details_learning_error.visible()
        learning_details_learning_error.text = error
    }


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

    private fun setModulesOnView(content: List<Module>) {
        var width: Int = 0
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 3) * 1.7).toInt()
        // model will change when integrated with DB

        val recyclerGenericAdapter: RecyclerGenericAdapter<Module> =
            RecyclerGenericAdapter<Module>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    //navigate(R.id.learningVideoFragment)
                    val module = item as Module
                    viewModel.getCourseLessonsAndAssessments(
                        courseId = mCourseId,
                        moduleId = module.id
                    )
                },
                RecyclerGenericAdapter.ItemInterface<Module> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title_)
                    title.text = obj?.title

                    var subtitle = getTextView(viewHolder, R.id.title)
                    subtitle.text = "Lesson ${obj.totalLessons} / ${obj.totalLessons}"

                    var img = getImageView(viewHolder, R.id.learning_img)

                    if (!obj.coverPicture.isNullOrBlank()) {
                        if (obj.coverPicture!!.startsWith("http", true)) {

                            GlideApp.with(requireContext())
                                .load(obj.coverPicture)
                                .placeholder(getCircularProgressDrawable())
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
                                        .into(img)
                                }
                        }
                    } else {
                        img.setBackgroundColor(
                            ResourcesCompat.getColor(
                                requireContext().resources,
                                R.color.warm_grey,
                                null
                            )
                        )
                    }

                })
        recyclerGenericAdapter.list = content
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        learning_details_modules_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_details_modules_rv.adapter = recyclerGenericAdapter
    }


    private fun showAssessmentsAsLoading() {

        learning_details_assessments_rv.gone()
        learning_details_assessments_error.gone()
        learning_details_assessments_progress_bar.visible()
    }

    private fun showAssessmentsOnView(content: List<CourseContent>) {

        learning_details_assessments_error.gone()
        learning_details_assessments_progress_bar.gone()
        learning_details_assessments_rv.visible()

        if(content.isEmpty()){
            learning_details_assessments_error.visible()
            learning_details_assessments_error.text = "No assessments found"
        }

        if (content.size > 4) {
            assessmentSeeMoreButton.visible()
            showAssessments(content.take(4))
        } else {
            assessmentSeeMoreButton.gone()
            showAssessments(content)
        }
    }

    private fun showErrorInLoadingAssessments(error: String) {

        learning_details_assessments_rv.gone()
        learning_details_assessments_progress_bar.gone()

        learning_details_assessments_error.visible()
        learning_details_assessments_error.text = error
    }


    private fun showAssessments(content: List<CourseContent>) {

        val recyclerGenericAdapter: RecyclerGenericAdapter<CourseContent> =
            RecyclerGenericAdapter<CourseContent>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->

                },
                RecyclerGenericAdapter.ItemInterface<CourseContent> { obj, viewHolder, position ->

                    getTextView(viewHolder, R.id.title).text = obj?.title
                    getTextView(viewHolder, R.id.time).text = "00:00"


                    getTextView(viewHolder, R.id.status).text = "PENDING"
                    getTextView(
                        viewHolder,
                        R.id.status
                    ).setBackgroundResource(R.drawable.rect_assessment_status_pending)
                    (getView(
                        viewHolder,
                        R.id.side_bar_status
                    ) as CardView).setCardBackgroundColor(resources.getColor(R.color.status_bg_pending))


                })!!
        recyclerGenericAdapter.setList(content)
        recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
        learning_details_assessments_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        learning_details_assessments_rv.adapter = recyclerGenericAdapter
    }


    companion object {

        const val INTENT_EXTRA_COURSE_ID = "course_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
    }
}