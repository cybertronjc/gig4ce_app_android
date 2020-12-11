package com.gigforce.app.modules.client_activation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.explore_by_role.AdapterPreferredLocation
import com.gigforce.app.modules.learning.LearningConstants
import com.gigforce.app.modules.learning.learningVideo.PlayVideoDialogFragment
import com.gigforce.app.modules.learning.models.LessonModel
import com.gigforce.app.utils.*
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.layout_fragment_client_activation.*
import kotlinx.android.synthetic.main.layout_role_description.view.*

class ClientActivationFragment : BaseFragment() {
    private lateinit var mWordOrderID: String
    private lateinit var viewModel: ClientActivationViewmodel
    private var adapterPreferredLocation: AdapterPreferredLocation? = null
    private lateinit var adapterBulletPoints: AdapterBulletPoints;


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_client_activation, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        viewModel =
            ViewModelProvider(
                this,
                SavedStateViewModelFactory(requireActivity().application, this)
            ).get(ClientActivationViewmodel::class.java)
        setupPreferredLocationRv()
        setupBulletPontsRv()
        initObservers()
        initClicks()
    }


    private fun setupBulletPontsRv() {
        adapterBulletPoints = AdapterBulletPoints();

        rv_bullet_points.adapter = adapterBulletPoints
        rv_bullet_points.layoutManager =
            LinearLayoutManager(requireContext())


    }

    private fun initClicks() {

        iv_back_client_activation.setOnClickListener {
            popBackState()
        }

    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
            pb_client_activation.gone()
        })
        viewModel.observableWorkOrder.observe(viewLifecycleOwner, Observer { it ->
            if (it.info == null) return@Observer

            Glide.with(this).load(it.coverImg).placeholder(
                com.gigforce.app.utils.getCircularProgressDrawable(requireContext())
            ).into(iv_main_client_activation)
            tv_businessname_client_activation.text = (it?.title ?: "")
            tv_role_client_activation.text = (it?.subTitle ?: "")
            it?.locationList?.map { item -> item.location }?.let { locations ->
                adapterPreferredLocation?.addData(locations)
            }
            tv_earning_client_activation.text = Html.fromHtml(it?.payoutNote)
            val viewRoleDesc = layoutInflater.inflate(R.layout.layout_role_description, null)
            ll_role_desc.removeAllViews()
            it?.queries?.forEach { element ->
                viewRoleDesc.tv_what_client_activation.text = element.query
                viewRoleDesc.tv_what_value_client_activation.text = element.answer
                if (!element.icon.isNullOrEmpty()) {
                    GlideApp.with(requireContext())
                        .load(element.icon)
                        .placeholder(getCircularProgressDrawable())
                        .into(viewRoleDesc.iv_what)

                } else {
                    viewRoleDesc.iv_what.setImageResource(R.drawable.ic_play_gradient)
                }
                ll_role_desc.addView(viewRoleDesc)

            }

            adapterBulletPoints.addData(it?.info!!)


//            if (!(it?.requiredLessons?.lessons.isNullOrEmpty())) {
            learning_cl.visible()
            textView120.text = it?.requiredMedia?.title
            initializeLearningModule(it?.requiredMedia?.media?.map { it.courseId } ?: listOf())
//            }

            viewModel.getApplication(it?.profileId ?: "")

        })

        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer { jpApplication ->
            pb_client_activation.gone()

            run {
                tv_mark_as_interest_role_details.setOnClickListener {

//                    if (jpApplication == null || jpApplication.stepDone == 1) {
                    if(jpApplication == null || jpApplication.status == "" || jpApplication.status == "Draft"){
                        navigate(
                            R.id.fragment_application_client_activation, bundleOf(
                                StringConstants.WORK_ORDER_ID.value to viewModel.observableWorkOrder.value?.profileId
                            )
                        )
                        viewModel.observableJpApplication.removeObservers(viewLifecycleOwner)
                    } else if (jpApplication.status == "Applied") {
                        navigate(
                            R.id.fragment_gig_activation, bundleOf(
                                StringConstants.WORK_ORDER_ID.value to viewModel.observableWorkOrder.value?.profileId,
                                StringConstants.NEXT_DEP.value to viewModel.observableWorkOrder.value?.nextDependency
                            )
                        )
                    }
                }
                tv_mark_as_interest_role_details.text = getString(R.string.apply_now)
                if (jpApplication == null) return@Observer
                if(jpApplication.status == "")
                    tv_applied_client_activation.gone()
                else
                tv_applied_client_activation.visible()
                tv_applied_client_activation.text = if(jpApplication.status == "Draft" || jpApplication.status == "Applied") "Pending" else jpApplication.status
                tv_applied_client_activation.setCompoundDrawablesWithIntrinsicBounds(if(jpApplication.status == "Draft" || jpApplication.status == "Applied" || jpApplication.status == "Inprocess")R.drawable.ic_status_pending else if (jpApplication.status == "Activated") R.drawable.ic_applied else R.drawable.ic_application_rejected,
                    0,
                    0,
                    0
                )
                setTextViewColor(tv_applied_client_activation,if(jpApplication.status == "Draft" || jpApplication.status == "Applied" || jpApplication.status == "Inprocess")R.color.pending_color else if (jpApplication.status == "Activated") R.color.activated_color else R.color.rejected_color)
                var actionButtonText = if(jpApplication.status == "Draft") getString(R.string.complete_application) else if(jpApplication.status == "Applied") getString(R.string.complete_activation) else if(jpApplication.status == "") getString(R.string.apply_now) else ""
                if(actionButtonText == "")
                    tv_mark_as_interest_role_details.gone()
                else
                    tv_mark_as_interest_role_details.text = actionButtonText
            }


        })
        if (!viewModel.initialized)
            viewModel.getWorkOrder(docID = mWordOrderID)


    }

    private fun setupPreferredLocationRv() {


        adapterPreferredLocation = AdapterPreferredLocation()
        rv_preferred_locations_client_activation.adapter = adapterPreferredLocation

        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.FLEX_START
        layoutManager.alignItems = AlignItems.FLEX_START
        rv_preferred_locations_client_activation.layoutManager = layoutManager
        rv_preferred_locations_client_activation.addItemDecoration(
            HorizontaltemDecoration(
                requireContext(),
                R.dimen.size_11
            )
        )

    }

//    fun setOnExpandListener(
//        role: List<String>?,
//        layout: TableLayout,
//        moreText: Boolean,
//        maxPoints: Int
//    ) {
//        if (layout.childCount > 0) {
//            layout.removeAllViews()
//        } else {
//            if (moreText) {
//                addBulletsTill(
//                    0,
//                    if (role?.size!! > maxPoints) 1 else role.size!! - 1,
//                    layout,
//                    role,
//                    true
//                )
//                if (role.size > 2) {
//                    val moreTextView = AppCompatTextView(requireContext())
//                    moreTextView.setTextSize(
//                        TypedValue.COMPLEX_UNIT_SP,
//                        14F
//                    )
//                    moreTextView.setTextColor(resources.getColor(R.color.lipstick))
//                    moreTextView.text = getString(R.string.plus_more)
//                    val face =
//                        Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
//                    moreTextView.typeface = face
//                    moreTextView.setPadding(
//                        resources.getDimensionPixelSize(R.dimen.size_16),
//                        0,
//                        0,
//                        0
//                    )
//
//                    layout.addView(moreTextView)
//                    moreTextView.setOnClickListener {
//                        layout.removeViewInLayout(moreTextView)
//                        addBulletsTill(
//                            2,
//                            role.size!! - 1,
//                            layout,
//                            role,
//                            false
//                        )
//                    }
//                }
//            } else {
//                addBulletsTill(
//                    0,
//                    role?.size!! - 1,
//                    layout,
//                    role,
//                    true
//                )
//
//            }
//
//        }
//
//    }
//
//    fun addBulletsTill(
//        from: Int,
//        to: Int,
//        layout: TableLayout,
//        arr: List<String>?,
//        removeAllViews: Boolean
//    ) {
//        if (removeAllViews)
//            layout.removeAllViews()
//        for (i in from..to) {
//
//            val iv = ImageView(requireContext())
//            val layoutParams = TableRow.LayoutParams(
//                TableRow.LayoutParams.WRAP_CONTENT,
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//            layoutParams.setMargins(
//                0,
//                resources.getDimensionPixelSize(R.dimen.font_9),
//                resources.getDimensionPixelSize(R.dimen.size_8),
//                0
//            )
//            iv.layoutParams = layoutParams
//            iv.setImageResource(R.drawable.shape_circle_lipstick)
//            val textView = TextView(requireContext())
//            val face =
//                Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
//            textView.typeface = face
//            textView.layoutParams = TableRow.LayoutParams(
//                getScreenWidth(requireActivity()).width - (resources.getDimensionPixelSize(R.dimen.size_66)),
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//
//            textView.setTextSize(
//                TypedValue.COMPLEX_UNIT_SP,
//                14F
//            )
//            textView.text = Html.fromHtml(arr?.get(i))
//
//            textView.setTextColor(resources.getColor(R.color.black))
//            val tr = TableRow(requireContext())
//
//
//            tr.layoutParams = TableRow.LayoutParams(
//                TableRow.LayoutParams.MATCH_PARENT,
//                TableRow.LayoutParams.WRAP_CONTENT
//            )
//
//            tr.addView(iv)
//            tr.addView(textView)
//            layout.addView(
//                tr,
//                TableLayout.LayoutParams(
//                    TableLayout.LayoutParams.MATCH_PARENT,
//                    TableLayout.LayoutParams.WRAP_CONTENT
//                )
//            )
//
//        }
//    }

    private fun initializeLearningModule(lessons: List<String>) {
        viewModel.observableCourses.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> showLearningAsLoading()
                is Lce.Content -> showUserLearningCourses(it.content)
                is Lce.Error -> showErrorWhileLoadingCourse(it.error)
            }
        })
        viewModel.getCoursesList(lessons)


    }

    private fun showLearningAsLoading() {

        learning_cl.visible()
        learning_rv.gone()
        learning_learning_error.gone()
        learning_progress_bar.visible()
    }

    private fun showErrorWhileLoadingCourse(error: String) {

        learning_cl.visible()
        learning_progress_bar.gone()
        learning_rv.gone()
        learning_learning_error.visible()

        learning_learning_error.text = error
    }

    private fun showUserLearningCourses(content: List<LessonModel>) {

        learning_progress_bar.gone()
        learning_learning_error.gone()
        learning_rv.visible()

        if (content.isEmpty()) {
            learning_cl.gone()
        } else {
            learning_cl.visible()

            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val width = displayMetrics.widthPixels
            val itemWidth = ((width / 3) * 2).toInt()
            // model will change when integrated with DB

            val recyclerGenericAdapter: RecyclerGenericAdapter<LessonModel> =
                RecyclerGenericAdapter<LessonModel>(
                    activity?.applicationContext,
                    PFRecyclerViewAdapter.OnViewHolderClick<LessonModel> { view, position, item ->
                        if (item.type == "document") {
                            val docIntent = Intent(
                                requireContext(),
                                DocViewerActivity::class.java
                            )
                            docIntent.putExtra(
                                StringConstants.DOC_URL.value,
                                item.url
                            )
                            startActivity(docIntent)
                        } else {
                            PlayVideoDialogFragment.launch(
                                childFragmentManager = childFragmentManager,
                                lessonId = viewModel.observableWorkOrder.value?.requiredMedia?.media?.get(
                                    position
                                )?.lessonId
                                    ?: "", moduleId = ""

                            )

                        }

                    },
                    RecyclerGenericAdapter.ItemInterface<LessonModel?> { obj, viewHolder, position ->
                        var view = getView(viewHolder, R.id.card_view)
                        val lp = view.layoutParams
                        lp.height = lp.height
                        lp.width = itemWidth
                        view.layoutParams = lp

                        var title = getTextView(viewHolder, R.id.title_)
                        title.text = obj?.name

                        var subtitle = getTextView(viewHolder, R.id.title)
                        subtitle.text = obj?.description

                        var comImg = getImageView(viewHolder, R.id.completed_iv)
                        comImg.isVisible = obj?.completed ?: false

                        var img = getImageView(viewHolder, R.id.learning_img)

                        if (!obj!!.coverPicture.isNullOrBlank()) {
                            if (obj!!.coverPicture!!.startsWith("http", true)) {

                                GlideApp.with(requireContext())
                                    .load(obj!!.coverPicture!!)
                                    .placeholder(getCircularProgressDrawable())
                                    .error(R.drawable.ic_learning_default_back)
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
                                            .error(R.drawable.ic_learning_default_back)
                                            .into(img)
                                    }
                            }
                        } else {

                            GlideApp.with(requireContext())
                                .load(R.drawable.ic_learning_default_back)
                                .into(img)
                        }

                        //img.setImageResource(obj?.imgIcon!!)
                    })!!
            recyclerGenericAdapter.setList(content)
            recyclerGenericAdapter.setLayout(R.layout.learning_bs_item)
            learning_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            learning_rv.adapter = recyclerGenericAdapter

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)


    }

}