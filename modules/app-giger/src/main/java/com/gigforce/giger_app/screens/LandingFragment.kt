package com.gigforce.giger_app.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.ILoginInfoProvider
import com.gigforce.giger_app.R
import com.gigforce.giger_app.vm.LandingViewModel
import kotlinx.android.synthetic.main.fragment_landing.*


class LandingFragment : Fragment() {

    val viewModel: LandingViewModel by viewModels<LandingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allLandingData.observe(viewLifecycleOwner, Observer {
            gig_info.bind(it.get(0))
            gigforce_tip.bind(it.get(1))
            join_ambassador.bind(it.get(2))
            my_interest.bind(it.get(3))
            set_preference.bind(it.get(5))
            complete_verification.bind(it.get(7))
            help_layout.bind(it.get(8))
            landing_rv.collection = it
        })
        initViews()
        listeners()
    }

    private fun initViews() {
        (this.context?.applicationContext as? ILoginInfoProvider)?.provideLoginInfo() ?. let {

        } ?: let {

        }
//        set profile name profile_name
//        set profile image
    }

    private fun listeners() {
        join_ambassador.setButtonClick(View.OnClickListener {
//            if(profile == null)
//                return@setOnClickListener
//
//            if (profile!!.isUserAmbassador) {
//                navigate(R.id.ambassadorEnrolledUsersListFragment)
//            } else {
//                navigate(R.id.ambassadorProgramDetailsFragment)
//            }
        })

        /*app_bar.chat_fragment.setOnClickListener{
            //call chat header fragment
        }*/

    }

}