package com.gigforce.app.modules.explore_by_role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.utils.GridSpacingItemDecoration
import com.gigforce.app.utils.ViewModelProviderFactory
import kotlinx.android.synthetic.main.layout_fragment_explore_by_role.*

class ExploreByRoleFragment : BaseFragment(), AdapterExploreByRole.AdapterExploreByRoleCallbacks {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ExploreByRoleViewModel(ExploreByRoleRepository()))
    }
    private val viewModel: ExploreByRoleViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ExploreByRoleViewModel::class.java)
    }
    private val adapter: AdapterExploreByRole by lazy {
        AdapterExploreByRole()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_explore_by_role, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        initObservers()
        initClicks()
    }

    private fun initClicks() {
        iv_back_explore_by_role.setOnClickListener {
            popBackState()
        }
        iv_search_explore_by_role.setOnClickListener {
            showToast("Coming Soon!!")
        }
        tv_sort_explore_by_role.setOnClickListener {
            showToast("Coming Soon!!")
        }

    }

    private fun initObservers() {
        viewModel.observerRoleList.observe(viewLifecycleOwner, Observer {
            adapter.addData(it ?: mutableListOf())
            pb_explore_by_role.gone()

        })
        viewModel.observerError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
            pb_explore_by_role.gone()
        })
        pb_explore_by_role.visible()
        viewModel.getRoles()
    }

    private fun setupRecyclerView() {
        rv_explore_by_role.adapter = adapter
        adapter.setCallbacks(this)
        rv_explore_by_role.layoutManager = GridLayoutManager(requireContext(), 2)
        rv_explore_by_role.addItemDecoration(
            GridSpacingItemDecoration(
                2,
                resources.getDimensionPixelSize(R.dimen.size_16), true
            )
        )

    }

    override fun onItemClicked(id: String?) {
        findNavController().navigate(ExploreByRoleFragmentDirections.openRoleDetails(id!!))
    }
}