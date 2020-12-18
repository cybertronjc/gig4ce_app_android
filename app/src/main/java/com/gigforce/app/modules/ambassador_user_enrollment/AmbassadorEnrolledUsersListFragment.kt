package com.gigforce.app.modules.ambassador_user_enrollment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.utils.VerticalItemDecorator
import kotlinx.android.synthetic.main.fragment_chat_screen.*
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*

class AmbassadorEnrolledUsersListFragment : BaseFragment(),
    EnrolledUsersRecyclerAdapter.EnrolledUsersRecyclerAdapterClickListener {

    private val viewModel : AmbassadorEnrollViewModel by viewModels()

    private val enrolledUserAdapter : EnrolledUsersRecyclerAdapter by lazy {
        EnrolledUsersRecyclerAdapter(requireContext()).apply {
                this.setListener(this@AmbassadorEnrolledUsersListFragment)
            }
    }

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
            navigate(R.id.checkMobileFragment)
        }

        createProfileBtn.setOnClickListener {
            navigate(R.id.checkMobileFragment)
        }

        enrolled_users_rv.layoutManager = LinearLayoutManager(activity?.applicationContext)
        enrolled_users_rv.addItemDecoration(VerticalItemDecorator(30))
        enrolled_users_rv.adapter = enrolledUserAdapter
    }

    private fun initViewModel() {
        viewModel.enrolledUsers
            .observe(viewLifecycleOwner, Observer {

                if(it.isEmpty()){
                    enrolledUserAdapter.setData(emptyList())
                    no_users_enrolled_layout.visible()
                    create_profile_btn.gone()
                } else{
                    no_users_enrolled_layout.gone()
                    create_profile_btn.visible()
                    enrolledUserAdapter.setData(it)
                }
            })
    }

    override fun onBackPressed(): Boolean {
        findNavController().popBackStack(R.id.landinghomefragment, false)
        return true
    }

    override fun onUserClicked(enrolledUser: EnrolledUser) {

    }

}