package com.gigforce.lead_management.ui.select_business_screen

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentSelectBusinessBinding
import com.gigforce.lead_management.ui.LeadManagementSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectBusinessFragment : BaseFragment2<FragmentSelectBusinessBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_select_business,
    statusBarColor = R.color.lipstick_2
), BusinessAdapter.OnBusinessSelectedListener {

    companion object {
        private const val TAG = "SelectBusinessFragment"
        const val INTENT_EXTRA_BUSINESS_LIST = "business_list"
    }

    private val sharedViewModel: LeadManagementSharedViewModel by activityViewModels()
    private var businessList: ArrayList<JoiningBusinessAndJobProfilesItem> = arrayListOf()

    private val glide: RequestManager by lazy {
        Glide.with(requireContext())
    }

    private val businessAdapter: BusinessAdapter by lazy {
        BusinessAdapter(requireContext(), glide).apply {
            setOnBusinessSelectedListener(this@SelectBusinessFragment)
        }
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentSelectBusinessBinding,
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
            businessList = it.getParcelableArrayList(INTENT_EXTRA_BUSINESS_LIST) ?: return@let
        }

        savedInstanceState?.let {
            businessList = it.getParcelableArrayList(INTENT_EXTRA_BUSINESS_LIST) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            INTENT_EXTRA_BUSINESS_LIST,
            businessList
        )
    }


    private fun initListeners() = viewBinding.apply {
        toolbar.apply {
            setBackButtonListener{
                navigation.navigateUp()
            }
            searchTextChangeListener = object : SearchTextChangeListener{

                override fun onSearchTextChanged(text: String) {
                    businessAdapter.filter.filter(text)
                }
            }
        }

        businessRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        businessRecyclerView.adapter = businessAdapter

        okayButton.setOnClickListener {
            val selectedBusiness =
                businessAdapter.getSelectedBusiness() ?: return@setOnClickListener
            sharedViewModel.businessSelected(selectedBusiness)
            findNavController().navigateUp()
        }
    }

    private fun setDataOnView() = viewBinding.apply {
        if (businessList.isEmpty()) {
            this.businessInfoLayout.root.visible()
            this.businessInfoLayout.infoMessageTv.text = "No Business to show"
        } else {
            this.businessInfoLayout.root.gone()
            businessAdapter.setData(businessList)
        }
    }

    override fun onBusinessSelected(businessSelected: JoiningBusinessAndJobProfilesItem) {
        viewBinding.okayButton.isEnabled = true
    }
}