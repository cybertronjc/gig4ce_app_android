package com.gigforce.client_activation.client_activation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
import com.gigforce.core.datamodels.client_activation.Dependency
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.client_activation.client_activation.models.JpSettings
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.NavFragmentsData
import com.gigforce.core.extensions.*
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_application_client_activation_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationClientActivationFragment : Fragment(),
        AdapterApplicationClientActivation.AdapterApplicationClientActivationCallbacks,
        ReviewApplicationDialogClientActivation.ReviewApplicationDialogCallbacks {
    private var dialog: ReviewApplicationDialogClientActivation? = null
    private lateinit var viewModel: ApplicationClientActivationViewModel
    @Inject lateinit var navigation : INavigation

    private val adapter: AdapterApplicationClientActivation by lazy {
        AdapterApplicationClientActivation()
    }
    private lateinit var mJobProfileId: String


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
                R.layout.layout_application_client_activation_fragment,
                container,
                false
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
        viewModel.draftApplication(mJobProfileId)

    }


    private fun checkForBackPress() {
            var navFragmentsData = activity as NavFragmentsData
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
            activity?.onBackPressed()
        }

        tv_action_application_client_activation.setOnClickListener {

            onClickSubmit()

            viewModel.redirectToNextStep = false
            parent_application_client_activation.visibility = View.INVISIBLE


        }

    }

    var jpSettings: JpSettings? = null
    private fun initObservers() {

        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observableApplicationStatus.observe(viewLifecycleOwner, Observer {
            pb_application_client_activation.gone()
            navigation.popBackStack()
            if (viewModel.isActivationScreenFound) {
                navigation.navigateTo(
                        "client_activation/gigActivation", bundleOf(
                        StringConstants.JOB_PROFILE_ID.value to mJobProfileId
                )
                )
            } else {
                jpSettings?.completionTitle?.let {
                    navigation.navigateTo(
                            "client_activation/applicationSubmission", bundleOf(StringConstants.JOB_PROFILE_ID.value to mJobProfileId, StringConstants.BUSSINESS_NAME.value to it)
                    )
                }
            }
        })
        viewModel.observableInitApplication.observe(viewLifecycleOwner, Observer {
            pb_application_client_activation.gone()
            if (it == true) {

                initApplication(viewModel.observableJpApplication.value!!)
            }
        })
        viewModel.observableJobProfile.observe(viewLifecycleOwner, Observer {
            jpSettings = it
            Glide.with(this).load(it?.coverImg).placeholder(
                com.gigforce.common_ui.utils.getCircularProgressDrawable(
                    requireContext()
                )
            ).into(iv_application_client_activation)

            tv_thanks_application.text = Html.fromHtml(it?.title ?: "")
            tv_completion_application.text = it?.subTitle ?: ""
            tv_title_application_client_activation.text = it?.businessTitle ?: ""
            viewModel.updateDraftJpApplication(
                    mJobProfileId,
                    it?.requiredFeatures ?: listOf()
            )

        })

        viewModel.getJobProfileDependency(mJobProfileId)


    }

    private fun initApplication(jpApplication: JpApplication) {
        adapter.addData(jpApplication.application)
        adapter.setCallbacks(this)
        for (i in 0 until jpApplication.application.size) {
            if (!jpApplication.application[i].isDone) {
                adapter.setImageDrawable(
                        jpApplication.application[i].type!!,
                        resources.getDrawable(R.drawable.ic_status_pending),
                        false
                )
//               viewModel.setData(jpApplication.application[i].feature)
            } else {
                adapter.setImageDrawable(
                        jpApplication.application[i].type!!,
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
                        navigation.navigateTo(
                                "profile", bundleOf(
                                StringConstants.FROM_CLIENT_ACTIVATON.value to true,
                                StringConstants.ACTION.value to UPLOAD_PROFILE_PIC

                        )
                        )
                    }
                    "about_me" -> {
                        navigation.navigateTo(
                                "learning/addBio", bundleOf(
                                StringConstants.FROM_CLIENT_ACTIVATON.value to true
                        )
                        )
                    }
                    "questionnaire" -> navigation.navigateTo(
                            "learning/questionnair", bundleOf(
                            StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
                            StringConstants.TITLE.value to adapter.items[i].title,
                            StringConstants.TYPE.value to adapter.items[i].type,
                            StringConstants.FROM_CLIENT_ACTIVATON.value to true
                    )
                    )
                    "driving_licence" -> navigation.navigateTo(
                            "verification/DLCA",
                            bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
                    )
                    "learning" -> navigation.navigateTo(
                            "learning/coursedetails",
                            bundleOf(
                                    INTENT_EXTRA_COURSE_ID to adapter.items[i].courseId,
                                    StringConstants.FROM_CLIENT_ACTIVATON.value to true
                            )
                    )
                }
                break
            }

        }


    }
    companion object {
        val UPLOAD_PROFILE_PIC = 1
        const val INTENT_EXTRA_COURSE_ID = "course_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
    }
    fun checkAndUpdateUI() {
        h_pb_application_frag.max = adapter.items.size

        Observable.fromIterable(adapter.items).all { item -> item.isDone }.subscribe({ success ->
            tv_action_application_client_activation.isEnabled = success
        }, { err -> })
        Observable.fromIterable(adapter.items).filter { item -> !item.isDone }.toList()
                .subscribe({ success ->
                    run {
                        h_pb_application_frag.progress = adapter.items.size - success.size
                        tv_steps_pending_application_value.text =
                                "" + success.size + "/" + adapter.items.size
                        h_pb_application_frag.visible()
                    }
                }, { _ ->

                    h_pb_application_frag.visible()
                })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let

        }

        arguments?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let

        }
    }


    private fun setupRecycler() {
        rv_status_pending.adapter = adapter
        rv_status_pending.layoutManager =
                LinearLayoutManager(requireContext())


    }

    override fun onItemClick(dependency: Dependency) {
        viewModel.redirectToNextStep = true

        when (dependency.type) {
            "profile_pic" -> {
                navigation.navigateTo(
                        "profile", bundleOf(
                        StringConstants.FROM_CLIENT_ACTIVATON.value to true,
                        StringConstants.ACTION.value to UPLOAD_PROFILE_PIC

                )
                )

            }
            "about_me" -> {
                navigation.navigateTo(
                        "profile/addBio", bundleOf(
                        StringConstants.FROM_CLIENT_ACTIVATON.value to true
                )
                )
            }
            "questionnaire" -> navigation.navigateTo(
                    "learning/questionnair", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
                    StringConstants.TITLE.value to dependency.title,
                    StringConstants.TYPE.value to dependency.type,
                    StringConstants.FROM_CLIENT_ACTIVATON.value to true
            )
            )
            "driving_licence" -> navigation.navigateTo(
                    "verification/DLCA",
                    bundleOf(StringConstants.FROM_CLIENT_ACTIVATON.value to true)
            )
            "learning" ->

                navigation.navigateTo(
                        "learning/coursedetails",
                        bundleOf(
                                INTENT_EXTRA_COURSE_ID to dependency.courseId,
                                StringConstants.FROM_CLIENT_ACTIVATON.value to true
                        )
                )
        }
    }

    private var PHOTO_CROP: Int = 45
    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"


    override fun onClickSubmit() {
        viewModel.apply1(mJobProfileId)
        dialog?.dismiss()
        pb_application_client_activation.visible()

    }

    override fun onClickReview() {
        dialog?.dismiss()
        parent_application_client_activation.visible()


    }


}