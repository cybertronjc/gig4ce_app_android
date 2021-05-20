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
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.client_activation.client_activation.models.Role
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.common_ui.decors.GridSpacingItemDecoration
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.ViewModelProviderFactory
import kotlinx.android.synthetic.main.layout_fragment_explore_by_role.*

class ExploreByRoleFragment : BaseFragment(), AdapterExploreByRole.AdapterExploreByRoleCallbacks {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(
            ExploreByRoleViewModel(ExploreByRoleRepository())
        )
    }
    private val viewModel: ExploreByRoleViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ExploreByRoleViewModel::class.java)
    }

    private val viewModelProfile: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
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
        viewModel.observerRoleList.observe(viewLifecycleOwner, Observer { rolesList ->
            run {

                viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer {
                    it.role_interests?.forEach { element ->
                        val index = rolesList?.indexOf(Role(id = element.interestID))
                        if (index != -1) {
                            rolesList?.get(index!!)?.isMarkedAsInterest = true
                        }
                    }
                    adapter.addData(rolesList ?: mutableListOf())
                    pb_explore_by_role.gone()
                })
            }


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