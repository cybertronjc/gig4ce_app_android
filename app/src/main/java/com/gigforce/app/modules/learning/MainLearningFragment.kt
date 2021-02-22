package com.gigforce.app.modules.learning

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.modules.learning.models.Course
import com.gigforce.app.utils.Lce
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.common_ui.ILoginInfoRepo
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.calendar_home_screen.chat_icon_iv
import kotlinx.android.synthetic.main.fragment_main_learning_assessments.*
import kotlinx.android.synthetic.main.fragment_main_learning_explore_learnings_layout.*
import kotlinx.android.synthetic.main.fragment_main_learning_journey_layout.*
import kotlinx.android.synthetic.main.fragment_main_learning_role_based_learnings.*
import kotlinx.android.synthetic.main.fragment_main_learning_toolbar.*
import javax.inject.Inject

@AndroidEntryPoint
class MainLearningFragment : BaseFragment() {//, IOnBackPressedOverride
    //    private val viewModelProfile: ProfileViewModel by viewModels()
    private val learningViewModel: LearningViewModel by viewModels()

    @Inject
    lateinit var loginInfo: ILoginInfoRepo

        private val mainLearningViewModel: MainLearningViewModel by viewModels()
    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_main_learning,inflater,container)//inflater.inflate(R.layout.fragment_main_learning, container,false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        learningBackButton.setOnClickListener {
//            popBackState()
//            activity?.onBackPressed()
            parentFragmentManager.popBackStack()
        }

        journey_completed_cardview.setOnClickListener {
//            navigate(R.id.myLearningFragment)
            navigation.navigateTo("mylearning")
        }

        journey_ongoing_cardview.setOnClickListener {
//            navigate(R.id.myLearningFragment)
            navigation.navigateTo("mylearning")
        }

        journey_pending_cardview.setOnClickListener {
//            navigate(R.id.myLearningFragment)
            navigation.navigateTo("mylearning")
        }
        //Todo: uncomment initexplorebyindustry
//        initializeExploreByIndustry()
        //   mostPopularLearning()
        listener()
        observerProfile()

        initLearningViewModel()
        imageView30.gone()
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

        learningViewModel
            .allCourses
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showExploreLearningProgress()
                    is Lce.Content -> showCoursesOnExploreLearning(it.content.sortedBy { it.priority })
                    is Lce.Error -> showExploreLearningError(it.error)
                }
            })

//        mainLearningViewModel
//            .allAssessments
//            .observe(viewLifecycleOwner, Observer {
//
//                when (it) {
//                    Lce.Loading -> showAssessmentProgress()
//                    is Lce.Content -> showAssessments(it.content)
//                    is Lce.Error -> showAssessmentError(it.error)
//                }
//            })

        learningViewModel.getRoleBasedCourses()
        learningViewModel.getAllCourses()
//        mainLearningViewModel.getAssessmentsFromAllAssignedCourses()
    }

    private fun showAssessmentProgress() {
        main_learning_assessments_rv.gone()
        main_learning_assessment_error.gone()
        startShimmer(
            assessment_loader as LinearLayout,
            ShimmerDataModel(
                minHeight = 78,
                minWidth = 227, marginRight = 1,
                orientation = LinearLayout.HORIZONTAL
            ), R.id.shimmer_controller
        )
    }

    private fun showAssessmentError(error: String) {

        main_learning_assessments_rv.gone()
        main_learning_assessment_error.visible()

        main_learning_assessment_error.text = error
        stopShimmer(assessment_loader as LinearLayout, R.id.shimmer_controller)
    }


//    private fun showAssessments(content: List<CourseContent>) {
//
//        main_learning_assessment_error.gone()
//        main_learning_assessments_rv.visible()
//        stopShimmer(assessment_loader as LinearLayout, R.id.shimmer_controller)
//
//        if (content.isEmpty()) {
//            main_learning_assessments_rv.gone()
//            main_learning_assessment_error.visible()
//
//            main_learning_assessment_error.text = "No Assessment Found"
//        } else {
//
//            val displayMetrics = DisplayMetrics()
//            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//            val width = displayMetrics.widthPixels
//            val itemWidth = ((width / 5) * 4).toInt()
//
//
//            val recyclerGenericAdapter: RecyclerGenericAdapter<CourseContent> =
//                RecyclerGenericAdapter<CourseContent>(
//                    activity?.applicationContext,
//                    PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                        val assessment = item as CourseContent
//
//                        showToast("Disabled ,will be enabled soon")
//
////                        navigate(R.id.assessment_fragment,  bundleOf(
////                            AssessmentFragment.INTENT_LESSON_ID to assessment.id
////                        ))
//                    },
//                    RecyclerGenericAdapter.ItemInterface<CourseContent> { obj, viewHolder, position ->
//                        val lp = getView(viewHolder, R.id.assessment_cl).layoutParams
//                        lp.height = lp.height
//                        lp.width = itemWidth
//                        getView(viewHolder, R.id.assessment_cl).layoutParams = lp
//                        getTextView(viewHolder, R.id.title).text = obj?.title
//                        getTextView(viewHolder, R.id.time).text = "02:00"
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
//                    })
//            recyclerGenericAdapter.list = content
//            recyclerGenericAdapter.setLayout(R.layout.assessment_bs_item)
//            main_learning_assessments_rv.layoutManager = LinearLayoutManager(
//                activity?.applicationContext,
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//            main_learning_assessments_rv.adapter = recyclerGenericAdapter
//        }
//
//    }

    private fun showRoleBasedLearningError(error: String) {

        learning_based_role_rv.gone()
        stopShimmer(learning_based_horizontal_progress as LinearLayout, R.id.shimmer_controller)
        role_based_learning_error.visible()
        role_based_learning_error.text = error

    }

    private fun showRoleBasedLearningProgress() {
        startShimmer(
            learning_based_horizontal_progress as LinearLayout,
            ShimmerDataModel(
                minHeight = 148,
                minWidth = 300,
                marginRight = 1,
                orientation = LinearLayout.HORIZONTAL
            ),
            R.id.shimmer_controller
        )
        learning_based_role_rv.gone()
        role_based_learning_error.gone()

    }

    private fun showRoleBasedLearnings(content: List<Course>) {
        learning_based_horizontal_progress.gone()
        role_based_learning_error.gone()
        learning_based_role_rv.visible()
        stopShimmer(learning_based_horizontal_progress as LinearLayout)
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

    private fun showExploreLearningError(error: String) {

        explore_learnings_rv.gone()
        explore_learning_error.visible()
        explore_learning_error.text = error
        stopShimmer(explore_learnings_loader as LinearLayout, R.id.shimmer_controller)
    }

    private fun showExploreLearningProgress() {
        startShimmer(
            explore_learnings_loader as LinearLayout,
            ShimmerDataModel(
                minHeight = 168,
                minWidth = 147, marginRight = 1,
                orientation = LinearLayout.HORIZONTAL
            ), R.id.shimmer_controller
        )
        explore_learnings_rv.gone()
        explore_learning_error.gone()
    }


    private fun observerProfile() {
        loginInfo.getData().observe(viewLifecycleOwner, Observer { profile ->
            profile?.profilePicPath?.let {
                displayImage(it)
            }
        })
//        viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer { profile ->
//            displayImage(profile?.profileAvatarName!!)
//        })

    }

    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                FirebaseStorage.getInstance().reference.child("profile_pics").child(profileImg)
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
        chat_icon_iv.setOnClickListener {
            navigation.navigateTo("chats/main")
        }
        cardView.setOnClickListener {
            navigation.navigateTo("profile")
//            navigate(R.id.profileFragment)
        }
    }

//    private fun mostPopularLearning() {
//        val displayMetrics = DisplayMetrics()
//        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//        width = displayMetrics.widthPixels
//        val itemWidth = ((width / 2.8) * 1).toInt()
//        // model will change when integrated with DB
//        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()
//
//        datalist.add(
//            TitleSubtitleModel(
//                "Delivery",
//                "Maintaining hygiene and safety at gig",
//                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Fman_with_mask.jpg?alt=media&token=5b39c546-edcb-4d09-be53-70d34febb192"
//            )
//        )
//
//        datalist.add(
//            TitleSubtitleModel(
//                "Cook",
//                "How to cook low salt meals",
//                "How to prepare coffee?\", \"https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Fbrista_ls_img.jpg?alt=media&token=c5061822-a7d6-497c-8bee-09079cb8dc70"
//            )
//        )
//
//
//        datalist.add(
//            TitleSubtitleModel(
//                "Housekeeping",
//                "Selecting the right reagent to clean different floors?",
//                "Maintaining hygiene and safety at gig\", \"https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Fman_with_mask.jpg?alt=media&token=5b39c546-edcb-4d09-be53-70d34febb192"
//            )
//        )
//        datalist.add(
//            TitleSubtitleModel(
//                "Barista",
//                "How to prepare coffee?",
//                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Fbrista_ls_img.jpg?alt=media&token=c5061822-a7d6-497c-8bee-09079cb8dc70"
//            )
//        )
//
//        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
//            RecyclerGenericAdapter<TitleSubtitleModel>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    val course = item as Course
//
//                    navigate(
//                        R.id.learningCourseDetails,
//                        bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to course.id)
//                    )
//                },
//                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel> { obj, viewHolder, position ->
//                    var view = getView(viewHolder, R.id.card_view)
//                    val lp = view.layoutParams
//                    lp.height = lp.height
//                    lp.width = itemWidth
//                    view.layoutParams = lp
//
//                    var title = getTextView(viewHolder, R.id.title)
//                    title.text = obj?.title
//
//                    var subtitle = getTextView(viewHolder, R.id.subtitle)
//                    subtitle.text = obj?.subtitle
//
//                    obj?.imgStr?.let {
//                        var img = getImageView(viewHolder, R.id.img)
//                        showGlideImage(it, img)
//                    }
//
////                    var img = getImageView(viewHolder,R.id.img)
////                    img.setImageResource(obj?.imgIcon!!)
//                })
//        recyclerGenericAdapter.list = datalist
//        recyclerGenericAdapter.setLayout(R.layout.most_popular_item)
//        mostPopularLearningsRV.layoutManager = LinearLayoutManager(
//            activity?.applicationContext,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        mostPopularLearningsRV.adapter = recyclerGenericAdapter
//    }

    private fun showGlideImage(url: String, imgview: ImageView) {
        GlideApp.with(requireContext())
            .load(url)
            .placeholder(getCircularProgressDrawable())
            .into(imgview)
    }

    var width: Int = 0
//    private fun initializeExploreByIndustry() {
//        val displayMetrics = DisplayMetrics()
//        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
//        width = displayMetrics.widthPixels
//        val itemWidth = ((width / 3) * 2).toInt()
//        // model will change when integrated with DB
//        var datalist: ArrayList<TitleSubtitleModel> = ArrayList<TitleSubtitleModel>()
////        datalist.add(
////            TitleSubtitleModel(
////                "Retail Sales Executive",
////                "Demonstrate products to customers", R.drawable.learning2
////            )
////        )
//        datalist.add(
//            TitleSubtitleModel(
//                "Driver",
//                "How to accept a ride",
//                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/temp_files%2Fdriver_img.jpg?alt=media&token=68412376-59c8-4598-81d6-9630724afff6"
//            )
//        )
//        val recyclerGenericAdapter: RecyclerGenericAdapter<TitleSubtitleModel> =
//            RecyclerGenericAdapter<TitleSubtitleModel>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<Any?> { view, position, item ->
//                    val course = item as Course
//
//                    navigate(
//                        R.id.learningCourseDetails,
//                        bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to course.id)
//                    )
//                },
//                RecyclerGenericAdapter.ItemInterface<TitleSubtitleModel> { obj, viewHolder, position ->
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
//                    subtitle.text = obj?.subtitle
//                    obj?.imgStr?.let {
//                        var img = getImageView(viewHolder, R.id.learning_img)
//                        showGlideImage(it, img)
//                    }
//                })
//        recyclerGenericAdapter.list = datalist
//        recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
//        searchSuggestionBasedVideosRV.layoutManager = LinearLayoutManager(
//            activity?.applicationContext,
//            LinearLayoutManager.HORIZONTAL,
//            false
//        )
//        searchSuggestionBasedVideosRV.adapter = recyclerGenericAdapter
//    }


    class TitleSubtitleModel(var title: String, var subtitle: String, var imgStr: String)

    override fun onBackPressed(): Boolean {
        parentFragmentManager.popBackStack()
        return false
    }

    // Todo: uncomment courses on Explore Learning method
    private fun showCoursesOnExploreLearning(content: List<Course>) {
        explore_learning_error.gone()
        explore_learnings_rv.visible()
        stopShimmer(explore_learnings_loader as LinearLayout)
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
                RecyclerGenericAdapter.ItemInterface<Course> { obj, viewHolder, position ->
                    var view = getView(viewHolder, R.id.card_view)
                    val lp = view.layoutParams
                    lp.height = lp.height
                    lp.width = itemWidth
                    view.layoutParams = lp

                    var title = getTextView(viewHolder, R.id.title)
                    title.text = obj?.name

                    var subtitle = getTextView(viewHolder, R.id.subtitle)
                    subtitle.text = obj?.level

                    var comImg = getImageView(viewHolder, R.id.completed_iv)
                    comImg.isVisible = obj?.completed ?: false

                    var img = getImageView(viewHolder, R.id.img)
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
        recyclerGenericAdapter.setLayout(R.layout.most_popular_item)
        explore_learnings_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        explore_learnings_rv.adapter = recyclerGenericAdapter
    }
}