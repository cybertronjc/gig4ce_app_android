package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.utils.StringConstants
import io.reactivex.Observable
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
                Observable.fromIterable(viewModel.observableJpApplication.value?.process)
                    .all { item -> item.isDone }.subscribe { success, err ->
                        run {
                            if (success) {
                                tv_verification_gig_activation.text = "Completed"
                                tv_verification_gig_activation.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_applied,
                                    0,
                                    0,
                                    0
                                )
                            }
                        }
                    }
                initApplication(viewModel.observableJpApplication.value!!)
            }
        })
        viewModel.getActivationData(mWordOrderID)
    }


    private fun initApplication(jpApplication: JpApplication) {
        adapter.addData(jpApplication.process)
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

    override fun onItemClick(feature: String, title: String, isSlotBooked: Boolean) {
        when (title) {
            "Driving Test Certificate" ->
                navigate(
                    if (isSlotBooked) R.id.fragment_doc_sub else R.id.fragment_upload_cert,
                    bundleOf(
                        StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                        StringConstants.TITLE.value to title,
                        StringConstants.TYPE.value to feature
                    )
                )
        }
    }

 
}