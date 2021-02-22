package com.gigforce.app.modules.assessment

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.LearningViewModel
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.modules.learning.learningVideo.PlayVideoDialogFragment
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.learning.slides.SlidesFragment
import com.gigforce.app.utils.*
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.decors.ItemDecor
import com.gigforce.common_ui.decors.ItemDecorSugLearning
import com.gigforce.common_ui.utils.getScreenShot
import com.gigforce.common_ui.utils.openPopupMenu
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.PermissionUtils
import com.gigforce.core.utils.shareFile
import com.gigforce.core.utils.storeImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_assessment_result.*
import kotlinx.android.synthetic.main.fragment_assessment_result.view.*
import kotlinx.android.synthetic.main.fragment_learning_video_item.*
import kotlinx.android.synthetic.main.fragment_learning_video_item.view.*
import kotlinx.android.synthetic.main.layout_rv_question_wisr_sum_assess_result.view.*
import kotlinx.android.synthetic.main.layout_sug_learning_ass_result.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.util.concurrent.TimeUnit


class AssessmentResultFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    AdapterSuggestedLearning.AdapterSuggestedLearningCallbacks {
    private var adapter: RecyclerGenericAdapter<Boolean>? = null
    private val viewModelAssessmentResult by lazy {
        ViewModelProvider(this).get(ViewModelAssessmentResult::class.java)
    }
    private val learningViewModel: LearningViewModel by viewModels()

    private var nextLessonId: String? = null
    private var currentLessonId: String? = null
    private var moduleId: String? = null

    private val navController: NavController by lazy {
        findNavController()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_assessment_result, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            nextLessonId = it.getString(AssessmentFragment.INTENT_NEXT_LESSON_ID)
            currentLessonId = it.getString(AssessmentFragment.INTENT_LESSON_ID)
            moduleId = it.getString(AssessmentFragment.INTENT_MODULE_ID)
        }

        arguments?.let {
            nextLessonId = it.getString(AssessmentFragment.INTENT_NEXT_LESSON_ID)
            currentLessonId = it.getString(AssessmentFragment.INTENT_LESSON_ID)
            moduleId = it.getString(AssessmentFragment.INTENT_MODULE_ID)
        }

        initUI()
        setupRecycler()
        initObservers()
        initClicks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(AssessmentFragment.INTENT_NEXT_LESSON_ID, nextLessonId)
        outState.putString(AssessmentFragment.INTENT_LESSON_ID, currentLessonId)
        outState.putString(AssessmentFragment.INTENT_MODULE_ID, moduleId)
    }

    private fun initObservers() {

        viewModelAssessmentResult.observableQuestionWiseSumList.observe(viewLifecycleOwner,
            Observer {

            })
        viewModelAssessmentResult.observableIsUserPassed.observe(viewLifecycleOwner, Observer {
//            tv_sug_learnings_label_assess_frag.visibility = it
//            rv_sug_learnings_assess_result.visibility = it
        })
        viewModelAssessmentResult.observablePermResultsGranted.observe(
            viewLifecycleOwner,
            Observer {
                initShareImage()
            })
        viewModelAssessmentResult.observablePermResultsNotGranted.observe(
            viewLifecycleOwner,
            Observer {
                checkForRequiredPermissions()
            })
        viewModelAssessmentResult.observablePermAlReadyGranted.observe(
            viewLifecycleOwner,
            Observer {
                initShareImage()
            })

        learningViewModel.allCourses.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lce.Loading -> {

                }
                is Lce.Content -> {
                    showLearnings(it.content)
                }
                is Lce.Error -> {
                }
            }
        })

        learningViewModel.lessonDetails.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lce.Loading -> {
                }
                is Lce.Content -> {
                    nextLesson = it.content
                    val isContentPresent = it.content != null
                    next_lesson_btn.isVisible = isContentPresent

                    next_lesson_btn.text = when (nextLesson?.type) {
                        CourseContent.TYPE_ASSESSMENT -> "Next Assessment"
                        CourseContent.TYPE_VIDEO -> "Next Lesson"
                        CourseContent.TYPE_SLIDE -> "Next Slide"
                        else -> "Okay"
                    }
                }
                is Lce.Error -> {
                }
            }
        })

        learningViewModel.showLessonOnFailing.observe(viewLifecycleOwner, Observer {

            when (it) {
                Lce.Loading -> {

                }
                is Lce.Content -> {
                    redoLesson = it.content
                    lesson_suggestions_layout.visible()
                    showLessonSuggestionOnFailing(it.content)
                }
                is Lce.Error -> {

                }
            }
        })

        if (userPassed) {

            if (nextLessonId != null)
                learningViewModel.getLessonDetails(nextLessonId!!)
            else {
                rl_new_certificate_assess_result.isVisible = true
                tv_sug_learnings_label_assess_frag.isVisible = true
                rv_sug_learnings_assess_result.isVisible = true
                tv_new_cert_asses_frag.text =
                    getString(R.string.you_will_be_soon_sent_an_invite)
                learningViewModel.getAllCourses()
            }
        } else {

            if (moduleId != null && currentLessonId != null)
                learningViewModel.showLessonToRedoOnFailing(
                    moduleId = moduleId!!,
                    lessonId = currentLessonId!!
                )
        }
    }

    private fun showLessonSuggestionOnFailing(content: CourseContent) {
        when (content.type) {
            CourseContent.TYPE_VIDEO -> {
                lesson_suggestions_layout.lessons_on_failed_layout.apply {
                    course_content_video_slide_layout.visible()
                    course_content_assessment_layout.gone()

                    video_title.text = content.title
                    video_time.text = content.videoLengthString

                    if (!content.coverPicture.isNullOrBlank()) {
                        if (content.coverPicture!!.startsWith("http", true)) {

                            GlideApp.with(context)
                                .load(content.coverPicture)
                                .placeholder(getCircularProgressDrawable())
                                .error(R.drawable.ic_learning_default_back)
                                .into(videoThumbnailIV)
                        } else {
                            FirebaseStorage.getInstance()
                                .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                                .child(content.coverPicture!!)
                                .downloadUrl
                                .addOnSuccessListener { fileUri ->

                                    GlideApp.with(context)
                                        .load(fileUri)
                                        .placeholder(getCircularProgressDrawable())
                                        .error(R.drawable.ic_learning_default_back)
                                        .into(videoThumbnailIV)
                                }
                        }
                    } else {
                        videoThumbnailIV.setBackgroundColor(
                            ResourcesCompat.getColor(
                                context.resources,
                                R.color.warm_grey,
                                null
                            )
                        )
                    }

                }
            }
            CourseContent.TYPE_SLIDE -> {
                lesson_suggestions_layout.lessons_on_failed_layout.apply {
                    course_content_video_slide_layout.visible()
                    course_content_assessment_layout.gone()

                    video_title.text = content.videoLengthString
                    video_time.text = content.videoLengthString

                    if (!content.coverPicture.isNullOrBlank()) {
                        if (content.coverPicture!!.startsWith("http", true)) {

                            GlideApp.with(context)
                                .load(content.coverPicture)
                                .placeholder(getCircularProgressDrawable())
                                .error(R.drawable.ic_learning_default_back)
                                .into(videoThumbnailIV)
                        } else {
                            FirebaseStorage.getInstance()
                                .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                                .child(content.coverPicture!!)
                                .downloadUrl
                                .addOnSuccessListener { fileUri ->

                                    GlideApp.with(context)
                                        .load(fileUri)
                                        .placeholder(getCircularProgressDrawable())
                                        .error(R.drawable.ic_learning_default_back)
                                        .into(videoThumbnailIV)
                                }
                        }
                    } else {
                        videoThumbnailIV.setBackgroundColor(
                            ResourcesCompat.getColor(
                                context.resources,
                                R.color.warm_grey,
                                null
                            )
                        )
                    }
                }
            }
            CourseContent.TYPE_ASSESSMENT -> {
                lesson_suggestions_layout.lessons_on_failed_layout.apply {
                    course_content_video_slide_layout.gone()
                    course_content_assessment_layout.visible()
                    title.text = content.title
                }
            }
        }
    }

    private var nextLesson: CourseContent? = null
    private var redoLesson: CourseContent? = null


    var width = 0
    private fun showLearnings(content: List<Course>) {

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//        width = displayMetrics.widthPixels
//        val itemWidth = ((width / 3) * 2).toInt()
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
                RecyclerGenericAdapter.ItemInterface<Course?> { obj, viewHolder, position ->
//                    var view = getView(viewHolder, R.id.card_view)
//                    val lp = view.layoutParams
//                    lp.height = lp.height
//                    lp.width = itemWidth
//                    view.layoutParams = lp

                    var title = title_//getTextView(viewHolder, R.id.title_)
                    title.text = obj?.name

                    var subtitle = title //getTextView(viewHolder, R.id.title)
                    subtitle.text = obj?.level

                    var img = learning_img//getImageView(viewHolder, R.id.learning_img)
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
        recyclerGenericAdapter.setLayout(R.layout.layout_sug_learning_ass_result)
        rv_sug_learnings_assess_result.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        rv_sug_learnings_assess_result.adapter = recyclerGenericAdapter
        rv_sug_learnings_assess_result.addItemDecoration(
            ItemDecorSugLearning(
                requireContext()
            )
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModelAssessmentResult.onActivityResultCalled(requestCode, resultCode, data)
    }

    private fun setupRecycler() {
        adapter = RecyclerGenericAdapter<Boolean>(context,
            PFRecyclerViewAdapter.OnViewHolderClick<Boolean> { view, position, item -> },
            RecyclerGenericAdapter.ItemInterface { obj, viewHolder, position ->

                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.text = "" + (position + 1)
                viewHolder.itemView.fl_rv_question_wise_sum_assess_result.setSolidColor(if (obj) "#ffd9e6" else "#888888")

                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.setTextColor(
                    if (obj) activity?.getColor(R.color.darkish_pink_100)!! else activity?.getColor(
                        R.color.black_85
                    )!!
                )
                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.isSelected = true

            })

        adapter?.setLayout(R.layout.layout_rv_question_wisr_sum_assess_result)
        rv_question_wise_sum_assess_frag.setHasFixedSize(true)
        rv_question_wise_sum_assess_frag.layoutManager = GridLayoutManager(activity, 5)
        rv_question_wise_sum_assess_frag.addItemDecoration(
            ItemDecor(requireContext())
        )
        rv_question_wise_sum_assess_frag.adapter = adapter
        adapter?.addAll(arguments?.getBooleanArray(StringConstants.ANSWERS_ARR.value)?.toList())


    }

    private fun initClicks() {
        iv_options_menu_tb.setOnClickListener {
            openPopupMenu(
                it,
                R.menu.menu_assessment_result,
                this,
                activity
            )
        }
        iv_back.setOnClickListener {
            clearBackStackToContentList()
        }

        next_lesson_btn.setOnClickListener {

            if (nextLesson == null) {
                clearBackStackToContentList()
                return@setOnClickListener
            }

            nextLesson?.let { cc ->

                when (cc.type) {
                    CourseContent.TYPE_VIDEO -> {
                        PlayVideoDialogFragment.launch(
                            childFragmentManager = childFragmentManager,
                            moduleId = cc.moduleId,
                            lessonId = cc.id,
                            shouldShowFeedbackDialog = cc.shouldShowFeedbackDialog
                        )
                    }
                    CourseContent.TYPE_ASSESSMENT -> {
                        navigate(
                            R.id.assessment_fragment, bundleOf(
                                AssessmentFragment.INTENT_LESSON_ID to cc.id,
                                AssessmentFragment.INTENT_MODULE_ID to cc.moduleId
                            )
                        )
                    }
                    CourseContent.TYPE_SLIDE -> {
                        navigate(
                            R.id.slidesFragment,
                            bundleOf(
                                SlidesFragment.INTENT_EXTRA_SLIDE_TITLE to cc.title,
                                SlidesFragment.INTENT_EXTRA_MODULE_ID to cc.moduleId,
                                SlidesFragment.INTENT_EXTRA_LESSON_ID to cc.id
                            )
                        )
                    }
                }

            }
        }

        lessons_on_failed_layout.setOnClickListener {
            openLessonToRedo()
        }

        lessonsSeeMoreButton.setOnClickListener {
            openLessonToRedo()
        }
    }

    private fun openLessonToRedo() {
        redoLesson?.let { cc ->

            when (cc.type) {
                CourseContent.TYPE_VIDEO -> {
                    PlayVideoDialogFragment.launch(
                        childFragmentManager = childFragmentManager,
                        moduleId = cc.moduleId,
                        lessonId = cc.id,
                        shouldShowFeedbackDialog = cc.shouldShowFeedbackDialog
                    )
                }
                CourseContent.TYPE_ASSESSMENT -> {
                    navigate(
                        R.id.assessment_fragment, bundleOf(
                            AssessmentFragment.INTENT_LESSON_ID to cc.id,
                            AssessmentFragment.INTENT_MODULE_ID to cc.moduleId
                        )
                    )
                }
                CourseContent.TYPE_SLIDE -> {
                    navigate(
                        R.id.slidesFragment,
                        bundleOf(
                            SlidesFragment.INTENT_EXTRA_SLIDE_TITLE to cc.title,
                            SlidesFragment.INTENT_EXTRA_MODULE_ID to cc.moduleId,
                            SlidesFragment.INTENT_EXTRA_LESSON_ID to cc.id
                        )
                    )
                }
            }

        }
    }

    override fun onBackPressed(): Boolean {
        clearBackStackToContentList()
        return true
    }

    private fun popTillSecondLastFragment() {
        val index = parentFragmentManager.backStackEntryCount - 2
        val backEntry = parentFragmentManager.getBackStackEntryAt(index);
        val tag = backEntry.name;
        val fragmentManager: FragmentManager? = parentFragmentManager
        fragmentManager?.executePendingTransactions()
        fragmentManager?.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private var userPassed = false
    private fun initUI() {
        userPassed = arguments?.getBoolean(StringConstants.ASSESSMENT_PASSED.value) ?: false
        tv_title_toolbar.text = getString(R.string.assessment)
        tv_kp_it_up_assess_result.text =
            if (userPassed) getString(R.string.keep_it_up) else getString(R.string.watch_lesson_again)
        iv_options_menu_tb.visibility = View.VISIBLE
        var correctAns = 0
        arguments?.getBooleanArray(StringConstants.ANSWERS_ARR.value)?.forEach { item ->
            run {
                if (item) correctAns++
            }
        }
        var percent = String.format(
            "%.1f",
            (((correctAns / arguments?.getBooleanArray(StringConstants.ANSWERS_ARR.value)?.size?.toFloat()!!) * 100))
        ) + " %"
        tv_score_assess_result.text =
            Html.fromHtml("${getString(R.string.you_have_scored)} <b>${percent}</b> ${getString(R.string.in_your_assessment)}")
        tv_new_cert_asses_frag.text =
            Html.fromHtml(getString(R.string.new_cert_added_underlined))

        viewModelAssessmentResult.checkIfUserPassed(userPassed)

        iv_options_menu_tb.visibility =
            if (arguments?.getBoolean(
                    StringConstants.ASSESSMENT_PASSED.value,
                    false
                )!!
            ) View.VISIBLE else View.GONE

        val timeTaken = arguments?.getInt(StringConstants.TIME_TAKEN.value)?.toLong()!!
        tv_time_taken_value_assess_frag.text = String.format(
            " %02d ${getString(R.string.hours)} %02d ${getString(R.string.mins)}  %02d ${getString(R.string.secs)} ",
            TimeUnit.MILLISECONDS.toHours(timeTaken),
            TimeUnit.MILLISECONDS.toMinutes(timeTaken) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(timeTaken) % TimeUnit.MINUTES.toSeconds(1)
        )


    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                viewModelAssessmentResult.checkForPermissionsAndInitSharing(
                    checkForRequiredPermissions()
                ); return true
            }
        }
        return false
    }

    private fun checkForRequiredPermissions(): Boolean {
        return PermissionUtils.checkForPermissionFragment(
            this,
            PermissionUtils.reqCodePerm,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun initShareImage() {
        storeImage(
            getScreenShot(cl_sv_nested_assess_result),
            StringConstants.CERTIFICATE_SSC.value,
            context?.filesDir?.absolutePath!!

        )
        shareFile(
            File(context?.filesDir?.absolutePath + "/" + StringConstants.CERTIFICATE_SSC.value),
            requireContext(),
            "image/*"
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModelAssessmentResult.checkIfPermGranted(requestCode, grantResults)
    }


    override fun onClickSuggestedLearnings() {
        navigate(R.id.mainLearningFragment)
    }

    private fun clearBackStackToContentList() {
        try {
            navController.getBackStackEntry(R.id.assessmentListFragment)
            navController.popBackStack(R.id.assessmentListFragment, false)
        } catch (e: Exception) {

            try {
                navController.getBackStackEntry(R.id.courseContentListFragment)
                navController.popBackStack(R.id.courseContentListFragment, false)
            } catch (e: Exception) {

                try {
                    navController.getBackStackEntry(R.id.learningCourseDetails)
                    navController.popBackStack(R.id.learningCourseDetails, false)
                } catch (e: Exception) {

                    try {
                        navController.getBackStackEntry(R.id.mainLearningFragment)
                        navController.popBackStack(R.id.mainLearningFragment, false)
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }


}