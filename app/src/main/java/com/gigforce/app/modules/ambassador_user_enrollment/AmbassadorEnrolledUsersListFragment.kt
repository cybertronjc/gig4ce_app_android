package com.gigforce.app.modules.ambassador_user_enrollment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.VerticalItemDecorator
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*

class AmbassadorEnrolledUsersListFragment : BaseFragment(),
    EnrolledUsersRecyclerAdapter.EnrolledUsersRecyclerAdapterClickListener {
    private lateinit var mAmbassadorID: String

    private val viewModel: AmbassadorEnrollViewModel by viewModels()

    private val enrolledUserAdapter: EnrolledUsersRecyclerAdapter by lazy {
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
        getDataFromIntents(savedInstanceState)
        initUi()
        initViewModel()
    }

    private fun initUi() {
        ic_back_iv?.setOnClickListener {
            activity?.onBackPressed()
        }



        enrolled_users_rv.layoutManager = LinearLayoutManager(activity?.applicationContext)
        enrolled_users_rv.addItemDecoration(VerticalItemDecorator(30))
        enrolled_users_rv.adapter = enrolledUserAdapter
    }

    private fun initViewModel() {
        viewModel.enrolledUsers
            .observe(viewLifecycleOwner, Observer {

                if (it.isEmpty()) {
                    enrolledUserAdapter.setData(emptyList())
                    no_users_enrolled_layout.visible()
                    createProfileBtn.gone()
                    total_users_enrolled_tv.gone()
                } else {
                    no_users_enrolled_layout.gone()
                    createProfileBtn.visible()
                    enrolledUserAdapter.setData(it)
                    total_users_enrolled_tv.visible()
                    total_users_enrolled_tv.text = buildSpannedString {
                        append(getString(R.string.total_profiles_created))
                        bold { append(it.size.toString()) }
                    }
                }
            })

        viewModel.observableEnrollmentProfile.observe(viewLifecycleOwner, Observer {
            tb_title_enrollment.text = it?.enrollmentTitle
            chip_interest_driving.text = it?.profileTabText
            chip_interest_bike_rider.text = it?.sourcingTabText
            chip_interest_delivery_executive.text = it?.managingGigsTabText
            tv_no_profiles_title.text = it?.noProfileHeaderText
            tv_no_profiles.text = it?.noProfileSubtitleText
            create_profile_btn.text = it?.noProfileActionText
            createProfileBtn.text = it?.noProfileActionText
            create_profile_btn.setOnClickListener {view->
                navigate(
                    R.id.checkMobileFragment, bundleOf(
                        StringConstants.AMB_APPLICATION_OBJ.value to it
                    )
                )
            }

            createProfileBtn.setOnClickListener {
                navigate(
                    R.id.checkMobileFragment, bundleOf(
                        StringConstants.AMB_APPLICATION_OBJ.value to it
                    )
                )
            }


        })
        viewModel.getAmbassadorApplication(mAmbassadorID)
    }

    override fun onBackPressed(): Boolean {

        try {
            findNavController().getBackStackEntry(R.id.mainHomeScreen)
            findNavController().popBackStack(R.id.mainHomeScreen, false)
        } catch (e: Exception) {
            findNavController().popBackStack(R.id.landinghomefragment, false)
        }
        return true
    }

    override fun onUserClicked(enrolledUser: EnrolledUser) {

    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mAmbassadorID = it.getString(StringConstants.AMBASSADOR_ID.value) ?: ""


        }

        arguments?.let {
            mAmbassadorID = it.getString(StringConstants.AMBASSADOR_ID.value) ?: ""


        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.AMBASSADOR_ID.value, mAmbassadorID)


    }


}