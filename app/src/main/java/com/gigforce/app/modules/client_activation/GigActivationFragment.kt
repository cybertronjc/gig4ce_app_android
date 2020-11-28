package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.layout_fragment_activation_gig.*

class GigActivationFragment : BaseFragment() {
    private lateinit var viewModel: GigActivationViewModel
    private lateinit var mNextDep: String
    private lateinit var mWordOrderID: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

    }

    private fun initObservers() {

        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observableGigActivation.observe(viewLifecycleOwner, Observer { gigAcivation ->
            if (gigAcivation != null) {
                Glide.with(this).load(gigAcivation.icon).placeholder(
                        com.gigforce.app.utils.getCircularProgressDrawable(requireContext())
                ).into(iv_gig_activation)
                tv_application_gig_activation.text = Html.fromHtml(gigAcivation.subTitle)
                tv_title_toolbar.text = gigAcivation.title
//                tv_application_stat_gig_activation.text = gigAcivation.status
                tv_verification_gig_activation.text = gigAcivation.status
                tv_complete_gig_activation.text = gigAcivation.instruction
                adapter.addData(gigAcivation.dependency)

            }
        })
        viewModel.getActivationData(mWordOrderID, mNextDep)
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
}