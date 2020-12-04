package com.gigforce.app.modules.client_activation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.landingscreen.models.Dependency
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.StringConstants
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_application_client_activation_fragment.*

class ApplicationClientActivationFragment : BaseFragment(),
        AdapterApplicationClientActivation.AdapterApplicationClientActivationCallbacks {
    private var profileAvatarName: String = "avatar.jpg"
    private lateinit var viewModel: ApplicationClientActivationViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    private val adapter: AdapterApplicationClientActivation by lazy {
        AdapterApplicationClientActivation()
    }
    private lateinit var mWordOrderID: String
    private lateinit var mNextDep: String


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(
                R.layout.layout_application_client_activation_fragment,
                inflater,
                container
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(ApplicationClientActivationViewModel::class.java)
        getDataFromIntents(savedInstanceState)
        checkForBackPress()
        setupRecycler()
        initObservers()
        initClicks()
        mNextDep = ""

    }


    private fun checkForBackPress() {

        if (navFragmentsData?.getData() != null) {
            if (navFragmentsData?.getData()
                            ?.getBoolean(StringConstants.BACK_PRESSED.value, false) == true
            ) {
                viewModel.redirectToNextStep = false
                navFragmentsData?.setData(bundleOf())
            }
        }
    }


    private fun initClicks() {
        iv_back_application_client_activation.setOnClickListener {
            onBackPressed()
        }

        tv_action_application_client_activation.setOnClickListener {
            viewModel.redirectToNextStep = false
            viewModel.apply(mWordOrderID)
            pb_application_client_activation.visible()


        }

    }

    private fun initObservers() {
        viewModel.observableApplicationStatus.observe(viewLifecycleOwner, Observer {
            pb_application_client_activation.gone()
            popBackState()
            navigate(
                    R.id.fragment_gig_activation, bundleOf(
                    StringConstants.NEXT_DEP.value to mNextDep,
                    StringConstants.WORK_ORDER_ID.value to mWordOrderID

            )
            )
        })
        viewModel.observableInitApplication.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                initApplication(viewModel.observableJpApplication.value!!)
            }
        })
        viewModel.observableWorkOrderDependency.observe(viewLifecycleOwner, Observer {
            Glide.with(this).load(it?.coverImg).placeholder(
                    com.gigforce.app.utils.getCircularProgressDrawable(requireContext())
            ).into(iv_application_client_activation)
            viewModel.updateDraftJpApplication(
                    mWordOrderID,
                    it?.requiredFeatures ?: listOf()
            )

        })

        viewModel.getWorkOrderDependency(mWordOrderID)


    }

    private fun initApplication(jpApplication: JpApplication) {
        adapter.addData(jpApplication.draft)
        adapter.setCallbacks(this)
        for (i in 0 until jpApplication.draft.size) {
            if (!jpApplication.draft[i].isDone) {
                adapter.setImageDrawable(
                        jpApplication.draft[i].type!!,
                        resources.getDrawable(R.drawable.ic_status_pending),
                        false
                )
//               viewModel.setData(jpApplication.draft[i].feature)
            } else {
                adapter.setImageDrawable(
                        jpApplication.draft[i].type!!,
                        resources.getDrawable(R.drawable.ic_applied),
                        true
                )
            }
        }
        checkAndUpdateUI()
        checkForRedirection()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_CANCELED) {
            viewModel.redirectToNextStep = false
        } else if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_OK) {
            initObservers()

        }
    }

    private fun checkForRedirection() {
        if (!viewModel.redirectToNextStep) return
        for (i in adapter.items.indices) {
            if (!adapter.items[i].isDone) {
                when (adapter.items[i].type) {
                    "profile_pic" -> {
                        val photoCropIntent = Intent(context, PhotoCrop::class.java)
                        photoCropIntent.putExtra("purpose", "profilePictureCrop")
                        photoCropIntent.putExtra("uid", viewModel.repository.getUID())
                        photoCropIntent.putExtra("fbDir", "/profile_pics/")
                        photoCropIntent.putExtra("detectFace", 1)
                        photoCropIntent.putExtra("folder", PROFILE_PICTURE_FOLDER)
                        photoCropIntent.putExtra("file", profileAvatarName)
                        startActivityForResult(photoCropIntent, PHOTO_CROP)
                    }
                    "about_me" -> {
                        navigate(
                                R.id.fragment_add_bio,
                                bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
                        )
                    }
                    "questionnaire" -> navigate(
                            R.id.application_questionnaire, bundleOf(
                            StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                            StringConstants.TITLE.value to adapter.items[i].title,
                            StringConstants.TYPE.value to adapter.items[i].type,
                            StringConstants.FROM_CLIENT_ACTIVATON.value to true
                    )
                    )
                    "driving_licence" -> navigate(
                            R.id.fragment_upload_dl_cl_act,
                            bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
                    )
                    "learning" -> navigate(
                            R.id.learningCourseDetails,
                            bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to adapter.items[i].courseId,
                                    StringConstants.FROM_CLIENT_ACTIVATON.value to true
                            )
                    )
                }
                break
            }

        }


    }

    fun checkAndUpdateUI() {
        h_pb_application_frag.max = adapter.items.size
        Observable.fromIterable(adapter.items).all { item -> item.isDone }.subscribe({ success ->
            tv_action_application_client_activation.isEnabled = success
        }, { err -> })
        Observable.fromIterable(adapter.items).filter { item -> item.isDone }.toList()
                .subscribe({ success ->
                    run {
                        h_pb_application_frag.progress = success.size
                        tv_steps_pending_application_value.text =
                                "" + (h_pb_application_frag.progress) + "/" + adapter.items.size
                    }
                }, { _ -> })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)
        outState.putString(StringConstants.NEXT_DEP.value, mNextDep)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mNextDep = it.getString(StringConstants.NEXT_DEP.value) ?: return@let

        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mNextDep = it.getString(StringConstants.NEXT_DEP.value) ?: return@let

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setupRecycler() {
        rv_status_pending.adapter = adapter
        rv_status_pending.layoutManager =
                LinearLayoutManager(requireContext())
//        rv_status_pending.addItemDecoration(
//            HorizontaltemDecoration(
//                requireContext(),
//                R.dimen.size_11
//            )
//        )

    }

    override fun onItemClick(dependency: Dependency) {
        viewModel.redirectToNextStep = true

        when (dependency.type) {
            "profile_pic" -> {
                val photoCropIntent = Intent(context, PhotoCrop::class.java)
                photoCropIntent.putExtra("purpose", "profilePictureCrop")
                photoCropIntent.putExtra("uid", viewModel.repository.getUID())
                photoCropIntent.putExtra("fbDir", "/profile_pics/")
                photoCropIntent.putExtra("detectFace", 1)
                photoCropIntent.putExtra("folder", PROFILE_PICTURE_FOLDER)
                photoCropIntent.putExtra("file", profileAvatarName)
                startActivityForResult(photoCropIntent, PHOTO_CROP)
            }
            "about_me" -> {
                navigate(
                        R.id.fragment_add_bio,
                        bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
                )
            }
            "questionnaire" -> navigate(
                    R.id.application_questionnaire, bundleOf(
                    StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                    StringConstants.TITLE.value to dependency.title,
                    StringConstants.TYPE.value to dependency.type,
                    StringConstants.FROM_CLIENT_ACTIVATON.value to true
            )
            )
            "driving_licence" -> navigate(
                    R.id.fragment_upload_dl_cl_act,
                    bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
            )
            "learning" -> navigate(
                    R.id.learningCourseDetails,
                    bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to dependency.courseId,
                            StringConstants.FROM_CLIENT_ACTIVATON.value to true)
            )
        }
    }

    private var PHOTO_CROP: Int = 45
    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"
    override fun onBackPressed(): Boolean {
        popBackState()
        return true
    }


}