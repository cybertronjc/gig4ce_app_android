package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.landingscreen.models.Dependency
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.layout_fragment_activation_gig.*

class GigActivationFragment : BaseFragment(),
        AdapterGigActivation.AdapterApplicationClientActivationCallbacks {
    private lateinit var viewModel: GigActivationViewModel
    private lateinit var mNextDep: String
    private lateinit var mWordOrderID: String

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_activation_gig, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        checkForBackPress()
        viewModel =
                ViewModelProvider(
                        this,
                        SavedStateViewModelFactory(requireActivity().application, this)
                ).get(GigActivationViewModel::class.java)
        setupRecycler()
        initObservers()
        initClicks()

    }

    private fun initClicks() {
        iv_back_application_gig_activation.setOnClickListener { popBackState() }
    }

    private fun initObservers() {

        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observableGigActivation.observe(viewLifecycleOwner, Observer { gigAcivation ->
            if (gigAcivation != null) {
                Glide.with(this).load(gigAcivation.coverImg).placeholder(
                        com.gigforce.app.utils.getCircularProgressDrawable(requireContext())
                ).into(iv_gig_activation)
                tv_application_gig_activation.text = Html.fromHtml(gigAcivation.subTitle)
                tv_title_toolbar.text = gigAcivation.title
                tv_complete_gig_activation.text = gigAcivation.instruction

                viewModel.updateDraftJpApplication(mWordOrderID, gigAcivation.requiredFeatures)

            }
        })

        viewModel.observableInitApplication.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                pb_gig_activation.gone()
                tv_verification_gig_activation.text = viewModel.observableJpApplication.value?.status
                tv_verification_gig_activation.setCompoundDrawablesWithIntrinsicBounds(if (viewModel.observableJpApplication.value?.status == "Activated") R.drawable.ic_applied else R.drawable.ic_status_pending, 0, 0, 0)
                initApplication(viewModel.observableJpApplication.value!!)
            }
        })
        viewModel.getActivationData(mWordOrderID)
    }


    private fun initApplication(jpApplication: JpApplication) {
        adapter.addData(jpApplication.process)

        sv_gig_activation.post {
            if (sv_gig_activation != null)
                sv_gig_activation.fullScroll(ScrollView.FOCUS_DOWN);
        }
        adapter.setCallbacks(this)
        for (i in 0 until jpApplication.process.size) {
            if (!jpApplication.process[i].isDone) {
                adapter.setImageDrawable(
                        jpApplication.process[i].type!!,
                        resources.getDrawable(R.drawable.ic_status_pending),
                        false
                )
//               viewModel.setData(jpApplication.draft[i].feature)
            } else {
                adapter.setImageDrawable(
                        jpApplication.process[i].type!!,
                        resources.getDrawable(R.drawable.ic_applied),
                        true
                )
            }
        }
        checkForRedirection()

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


    private val adapter: AdapterGigActivation by lazy {
        AdapterGigActivation()
    }

    private fun setupRecycler() {
        rv_gig_activation.adapter = adapter
        rv_gig_activation.layoutManager =
                LinearLayoutManager(requireContext())
//        rv_status_pending.addItemDecoration(
//            HorizontaltemDecoration(
//                requireContext(),
//                R.dimen.size_11
//            )
//        )

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


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)


    }

    override fun onItemClick(dependency: Dependency) {
        viewModel.redirectToNextStep = true
        when (dependency.type) {
            "onsite_document" -> {
                if (dependency.isSlotBooked) {
                    val index = adapter.items.indexOf(Dependency(type = "document"))
                    if (index != -1) {
                        navigate(
                                R.id.fragment_doc_sub,
                                bundleOf(
                                        StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                                        StringConstants.TITLE.value to adapter.items[index].title,
                                        StringConstants.TYPE.value to adapter.items[index].docType
                                )
                        )
                    }

                }
            }
            "document" ->
//                navigate(
//                        if (dependency.isSlotBooked) R.id.fragment_doc_sub else R.id.fragment_upload_cert,
//                        bundleOf(
//                                StringConstants.WORK_ORDER_ID.value to mWordOrderID,
//                                StringConstants.TITLE.value to dependency.title,
//                                StringConstants.TYPE.value to dependency.docType
//                        )
//                )

                navigate(
                        R.id.fragment_doc_sub,
                        bundleOf(
                                StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                                StringConstants.TITLE.value to dependency.title,
                                StringConstants.TYPE.value to dependency.docType
                        )
                )

            "learning" -> {
                navigate(
                        R.id.learningCourseDetails,
                        bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to dependency.courseId,
                                StringConstants.FROM_CLIENT_ACTIVATON.value to true
                        )
                )
            }
        }
    }

    private fun checkForRedirection() {
        if (!viewModel.redirectToNextStep) return
        for (i in adapter.items.indices) {
            if (!adapter.items[i].isDone) {
                when (adapter.items[i].type) {

                    "learning" ->
                        if (checForOtherIndices(i, adapter.items)) {
                            navigate(
                                    R.id.learningCourseDetails,
                                    bundleOf(LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to adapter.items[i].courseId,
                                            StringConstants.FROM_CLIENT_ACTIVATON.value to true
                                    )
                            )
                        }
                }


            }

        }


    }

    private fun checForOtherIndices(index: Int, items: List<Dependency>): Boolean {
        var allTrue = true
        for (i in items.indices) {
            if (index != i && !items[i].isDone) {
                allTrue = false
                break
            }
        }
        return allTrue
    }


}