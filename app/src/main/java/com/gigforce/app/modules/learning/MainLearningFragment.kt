package com.gigforce.app.modules.learning

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.calendar_home_screen.chat_icon_iv
import kotlinx.android.synthetic.main.fragment_main_learning_assessments.*
import kotlinx.android.synthetic.main.fragment_main_learning_explore_learnings_layout.*
import kotlinx.android.synthetic.main.fragment_main_learning_journey_layout.*
import kotlinx.android.synthetic.main.fragment_main_learning_most_popular_learnings_layout.*
import kotlinx.android.synthetic.main.fragment_main_learning_recommended_learnings.*
import kotlinx.android.synthetic.main.fragment_main_learning_role_based_learnings.*
import kotlinx.android.synthetic.main.fragment_main_learning_toolbar.*
import java.util.*


class MainLearningFragment : BaseFragment() {
    private val viewModelProfile: ProfileViewModel by viewModels()
    private val learningViewModel: LearningViewModel by viewModels()
    private val mainLearningViewModel: MainLearningViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_main_learning, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        learningBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        dummLayout1.videoTitleTV.text = getString(R.string.achieve_retail_goal)
        dummLayout1.videoDescTV.text = getString(R.string.industry_based)
        dummLayout1.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning))
        journey_completed_cardview.setOnClickListener {
            navigate(R.id.myLearningFragment)
        }

        dummLayout2.videoTitleTV.text = getString(R.string.apply_driving_license)
        dummLayout2.videoDescTV.text = getString(R.string.role_based)
        dummLayout2.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning2))
        journey_ongoing_cardview.setOnClickListener {
            navigate(R.id.myLearningFragment)
        }

        dummLayout3.videoTitleTV.text = getString(R.string.achieve_retail_goal)
        dummLayout3.videoDescTV.text = getString(R.string.industry_based)
        dummLayout3.videoThumbnailIV.setImageDrawable(resources.getDrawable(R.drawable.recent_added_learning1))
        journey_pending_cardview.setOnClickListener {
            navigate(R.id.myLearningFragment)
        }

        initializeExploreByIndustry()
        //   mostPopularLearning()
        listener()
        observerProfile()

        initLearningViewModel()
    }

    private fun initLearningViewModel() {
        learningViewModel
            .roleBasedCourses
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showRoleBasedLearningProgress()
                    is Lce.Content -> showRoleBasedLearnings(it.content)
                    is Lce.Error -> showRoleBasedLearningError(it.error)
                }
            })

        learningViewModel
            .allCourses
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showExploreLearningProgress()
                    is Lce.Content -> showCoursesOnExploreLearning(it.content)
                    is Lce.Error -> showExploreLearningError(it.error)
                }
            })

        mainLearningViewModel
            .allAssessments
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showAssessmentProgress()
                    is Lce.Content -> showAssessments(it.content)
                    is Lce.Error -> showAssessmentError(it.error)
                }
            })

        learningViewModel.getRoleBasedCourses()
        learningViewModel.getAllCourses()
        mainLearningViewModel.getAssessmentsFromAllAssignedCourses()
    }

    private fun showAssessmentProgress() {
        main_learning_assessments_rv.gone()
        main_learning_assessment_error.gone()
        main_learning_assessment_progress_bar.visible()
    }

    private fun showAssessmentError(error: String) {

        main_learning_assessments_rv.gone()
        main_learning_assessment_progress_bar.gone()
        main_learning_assessment_error.visible()

        main_learning_assessment_error.text = error
    }


    private fun showAssessments(content: List<CourseContent>) {

        main_learning_assessment_progress_bar.gone()
        main_learning_assessment_error.gone()
        main_learning_assessments_rv.visible()

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.getDefaultDisplay()?.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val itemWidth = ((width / 5) * 3.5).toInt()


        val recyclerGenericAdapter: RecyclerGenericAdapter<CourseContent> =
            RecyclerGenericAdapter<CourseContent>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    navigate(R.id.assessment_fragment)
                },
                RecyclerGenericAdapter.ItemInterface<CourseContent> { obj, viewHolder, position ->
                    val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    getView(viewHolder, R.id.assessment_cl).layoutParams = lp
                    getTextView(viewHolder, R.id.title).text = obj?.title
                    getTextView(viewHolder, R.id.time).text = "02:00"


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
        main_learning_assessments_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        main_learning_assessments_rv.adapter = recyclerGenericAdapter

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
                RecyclerGenericAdapter.ItemInterface<Course?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title_)
                    title.text = obj?.name

                    var subtitle = getTextView(viewHolder, R.id.title)
                    subtitle.text = obj?.name

                    var img = getImageView(viewHolder, R.id.learning_img)
                    if (!obj!!.coverPicture.isNullOrBlank()) {
                        if (obj!!.coverPicture!!.startsWith("http", true)) {

                            GlideApp.with(requireContext())
                                .load(obj!!.coverPicture!!)
                                .placeholder(getCircularProgressDrawable())
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
                                        .into(img)
                                }
                        }
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

    private fun showExploreLearningError(error: String) {

        explore_learnings_rv.gone()
        explore_learning_progress_bar.gone()
        explore_learning_error.visible()

        explore_learning_error.text = error
    }

    private fun showExploreLearningProgress() {
        explore_learnings_rv.gone()
        explore_learning_error.gone()
        explore_learning_progress_bar.visible()
    }

    private fun showCoursesOnExploreLearning(content: List<Course>) {
        explore_learning_progress_bar.gone()
        explore_learning_error.gone()
        explore_learnings_rv.visible()

        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 2.8) * 1).toInt()
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
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title)
                    title.text = obj?.name

                    var subtitle = getTextView(viewHolder, R.id.subtitle)
                    subtitle.text = obj?.name

                    var img = getImageView(viewHolder, R.id.img)
                    if (!obj!!.coverPicture.isNullOrBlank()) {
                        if (obj!!.coverPicture!!.startsWith("http", true)) {

                            GlideApp.with(requireContext())
                                .load(obj!!.coverPicture!!)
                                .placeholder(getCircularProgressDrawable())
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
                                        .into(img)
                                }
                        }
                    }
                })
        recyclerGenericAdapter.list = content
        recyclerGenericAdapter.setLayout(R.layout.most_popular_item)
        explore_learnings_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        explore_learnings_rv.adapter = recyclerGenericAdapter
    }

    private fun observerProfile() {
        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
            displayImage(profile?.profileAvatarName!!)
        })

    }
    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .apply(RequestOptions().circleCrop())
                .into(profile_image_main)
        } else {
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .apply(RequestOptions().circleCrop())
                .into(profile_image_main)
        }
    }
    private fun listener() {
        chat_icon_iv.setOnClickListener{
            navigate(R.id.contactScreenFragment)
        }
    }

    private fun mostPopularLearning() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
        val itemWidth = ((width / 2.8) * 1).toInt()
        // model will change when integrated with DB
        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()

        datalist.add(
            TitleSubtitleModel(
                "Delivery",
                "Maintaining hygiene and safety at gig", R.drawable.man_with_mask
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Cook",
                "How to cook low salt meals",
                R.drawable.cook_
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Barista",
                "How to prepare coffee?", R.drawable.barista
            )
        )

        datalist.add(
            TitleSubtitleModel(
                "Housekeeping",
                "Selecting the right reagent to clean different floors?",
                R.drawable.housekeeping
            )
        )

        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    val course = item as Course

                    navigate(
                        R.id.learningCourseDetails,
                        bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to course.id)
                    )
                },
                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel?> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title)
                    title.text = obj?.title

                    var subtitle = getTextView(viewHolder, R.id.subtitle)
                    subtitle.text = obj?.subtitle

                    var img = getImageView(viewHolder,R.id.img)
                    img.setImageResource(obj?.imgIcon!!)
                })
        recyclerGenericAdapter.list = datalist
        recyclerGenericAdapter.setLayout(R.layout.most_popular_item)
        mostPopularLearningsRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mostPopularLearningsRV.adapter = recyclerGenericAdapter
    }

    var width: Int = 0
    private fun initializeExploreByIndustry() {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        width = displayMetrics.widthPixels
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
                "Driver",
                "How to accept a ride",
                R.drawable.driver_img
            )
        )
        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
            RecyclerGenericAdapter<TitleSubtitleModel>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
                    val course = item as Course

                    navigate(
                        R.id.learningCourseDetails,
                        bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to course.id)
                    )
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

                    var img = getImageView(viewHolder,R.id.learning_img)
                    img.setImageResource(obj?.imgIcon!!)
                })
        recyclerGenericAdapter.list = datalist
        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
        searchSuggestionBasedVideosRV.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        searchSuggestionBasedVideosRV.adapter = recyclerGenericAdapter
    }



    class TitleSubtitleModel(var title: String, var subtitle: String, var imgIcon: Int = 0)
}