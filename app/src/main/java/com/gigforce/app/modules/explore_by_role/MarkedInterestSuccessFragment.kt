package com.gigforce.app.modules.explore_by_role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.landingscreen.models.Role
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.ViewModelProviderFactory
import kotlinx.android.synthetic.main.layout_marked_interest_success_fragment.*

class MarkedInterestSuccessFragment : BaseFragment() {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ExploreByRoleViewModel(ExploreByRoleRepository()))
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
        return inflateView(R.layout.layout_marked_interest_success_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
        setUpRecycler()
        initObservers()

    }

    private fun initObservers() {
        viewModel.observerRoleList.observe(viewLifecycleOwner, Observer { rolesList ->
            run {

                viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer {
                    it.role_interests?.forEach { element ->
                        val index = rolesList?.indexOf(Role(id = element.interestID))
                        if (index != -1) {

                            rolesList?.removeAt(index!!)
                        }
                    }
                    adapter.addData(rolesList ?: mutableListOf())
                    pb_marked_as_interest.gone()
                })
            }


        })
        viewModel.observerError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
            pb_marked_as_interest.gone()
        })

        pb_marked_as_interest.visible()
        viewModel.getRoles()
    }

    private fun setUpRecycler() {
        rv_not_interested_roles.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv_not_interested_roles.addItemDecoration(
            HorizontaltemDecoration(
                requireContext(),
                R.dimen.size_16
            )
        )
        rv_not_interested_roles.adapter = adapter
        adapter.isHorizontalCarousel()
    }

    private fun initClicks() {

        iv_close_mark_interest.setOnClickListener {
            popBackState()
        }
        tv_learning_mark_interest_success.setOnClickListener {
            navigate(R.id.mainLearningFragment)
        }
        tv_update_profile_mark_interest_success.setOnClickListener {
            navigate(R.id.profileFragment)
        }
    }

    private fun popTillSecondLastFragment() {
        val index = parentFragmentManager.backStackEntryCount - 2
        val backEntry = parentFragmentManager.getBackStackEntryAt(index);
        val tag = backEntry.name;
        val fragmentManager: FragmentManager? = parentFragmentManager
        fragmentManager?.executePendingTransactions()
        fragmentManager?.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)


    }

}