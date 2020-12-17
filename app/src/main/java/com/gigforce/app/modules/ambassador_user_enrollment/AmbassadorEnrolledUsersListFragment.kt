package com.gigforce.app.modules.ambassador_user_enrollment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*

class AmbassadorEnrolledUsersListFragment : BaseFragment() {

    private val viewModel : AmbassadorEnrollViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_embassador_enrolled_users_list, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        initViewModel()
    }

    private fun initUi() {
        ic_back_iv?.setOnClickListener {
            activity?.onBackPressed()
        }

        create_profile_btn.setOnClickListener {
        }

        createProfileBtn.setOnClickListener {

        }
    }

    private fun initViewModel() {

    }
}