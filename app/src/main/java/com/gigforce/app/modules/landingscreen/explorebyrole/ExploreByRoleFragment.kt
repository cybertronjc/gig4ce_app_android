package com.gigforce.app.modules.landingscreen.explorebyrole

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.explore_by_role_fragment.*

class ExploreByRoleFragment : BaseFragment() {

    companion object {
        fun newInstance() = ExploreByRoleFragment()
    }

    private lateinit var viewModel: ExploreByRoleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_by_role_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ExploreByRoleViewModel::class.java)
        listener()
    }

    private fun listener() {
        backpress_icon.setOnClickListener{
            activity?.onBackPressed()
        }
    }

}