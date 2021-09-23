package com.gigforce.lead_management.ui.select_tls

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectClientTlBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectClientTlFragment : BaseFragment2<FragmentSelectClientTlBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_client_tl,
    statusBarColor = R.color.lipstick_2
), ClientTLAdapter.OnClientTLListener {

    companion object {
        private const val TAG = "SelectClientTlFragment"
        const val INTENT_EXTRA_CLIENT_TLS = "client_tls"
    }

    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var clientTls: ArrayList<BusinessTeamLeadersItem> = arrayListOf()

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val clientTLAdapter: ClientTLAdapter by lazy {
        ClientTLAdapter(requireContext(), glide).apply {
            setOnClientTLListener(this@SelectClientTlFragment)
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }


    override fun viewCreated(
        viewBinding: FragmentSelectClientTlBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initListeners()
        setDataOnView()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            clientTls = it.getParcelableArrayList(INTENT_EXTRA_CLIENT_TLS) ?: return@let
        }

        savedInstanceState?.let {
            clientTls = it.getParcelableArrayList(INTENT_EXTRA_CLIENT_TLS) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_CLIENT_TLS,
            clientTls
        )
    }


    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            setBackButtonListener{
                navigation.navigateUp()
            }
            searchTextChangeListener = object : SearchTextChangeListener {
                override fun onSearchTextChanged(text: String) {
                    clientTLAdapter.filter.filter(text)
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = clientTLAdapter

        okayButton.setOnClickListener {
            val selectedJobProfile = clientTLAdapter.getSelectedTL() ?: return@setOnClickListener
            sharedViewModel.clientTLSelected(selectedJobProfile)
            findNavController().navigateUp()
        }
    }

    private fun setDataOnView() = viewBinding.apply {
        if (clientTls.isEmpty()) {
            this.infoLayout.root.visible()
            this.infoLayout.infoMessageTv.text = "No Client Team leader to show"
        } else {
            this.infoLayout.root.gone()
            clientTLAdapter.setData(clientTls)
        }
    }

    override fun onClientTLSelected(businessTl: BusinessTeamLeadersItem) {
        viewBinding.okayButton.isEnabled = true
    }
}