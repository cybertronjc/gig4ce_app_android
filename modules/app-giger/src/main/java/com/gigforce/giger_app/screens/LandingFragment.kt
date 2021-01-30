package com.gigforce.giger_app.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import com.gigforce.core.ILoginInfoProvider
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.LandingViewModel
import kotlinx.android.synthetic.main.fragment_landing.*


class LandingFragment : Fragment() {

    lateinit var viewModel: LandingViewModel //by viewModels<LandingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        viewModel =
            ViewModelProvider(
                this,
                SavedStateViewModelFactory(requireActivity().application, this)
            ).get(LandingViewModel::class.java)
        viewModel.allLandingData.observe(viewLifecycleOwner, Observer {
            gig_info.bind(it.get(0))
            gigforce_tip.bind(it.get(1))
            join_ambassador.bind(it.get(2))
            my_interest.bind(it.get(3))
            set_preference.bind(it.get(5))
            complete_verification.bind(it.get(7))
            help_layout.bind(it.get(8))
            features_list.bind(it.get(9))
//            landing_rv.collection = it
        })
        initViews()
        listeners()
    }

    private fun initViews() {
        (this.context?.applicationContext as? ILoginInfoProvider)?.provideLoginInfo() ?. let {
//        app_bar.profile_name.text = it.profileName
        } ?: let {

        }
//        set profile name profile_name
//        set profile image
    }

    private fun listeners() {
        join_ambassador.setPrimaryActionClick(View.OnClickListener {

        })
//                navigate(R.id.ambassadorEnrolledUsersListFragment)
//                navigate(R.id.ambassadorProgramDetailsFragment)

    }

}