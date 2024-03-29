package com.gigforce.landing_screen.landingscreen.explorebyrole

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
//import com.gigforce.app.R
//import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.navigation.INavigation
import com.gigforce.landing_screen.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.explore_by_role_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class ExploreByRoleFragment : Fragment() {

    companion object {
        fun newInstance() = ExploreByRoleFragment()
    }

    @Inject lateinit var navigation: INavigation
    private lateinit var viewModel: ExploreByRoleViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.explore_by_role_fragment,container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ExploreByRoleViewModel::class.java)
        listener()
    }

    private fun listener() {
        activate_status2.setOnClickListener{
            //navigate(R.id.jdScreenFragment)
            navigation.navigateTo("jdscreen")
        }
        backpress_icon.setOnClickListener{
            activity?.onBackPressed()
        }
    }

}