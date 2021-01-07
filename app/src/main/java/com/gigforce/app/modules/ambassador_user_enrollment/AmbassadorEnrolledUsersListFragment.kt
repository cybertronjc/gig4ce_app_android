package com.gigforce.app.modules.ambassador_user_enrollment

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.gigforce.app.utils.LocationUpdates
import com.gigforce.app.utils.PermissionUtils
import com.gigforce.app.utils.VerticalItemDecorator
import kotlinx.android.synthetic.main.fragment_embassador_enrolled_users_list.*

class AmbassadorEnrolledUsersListFragment : BaseFragment(),
    EnrolledUsersRecyclerAdapter.EnrolledUsersRecyclerAdapterClickListener, LocationUpdates.LocationUpdateCallbacks {
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }
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
                        append("Total profiles Created : ")
                        bold { append(it.size.toString()) }
                    }
                }
            })
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

    override fun onDestroy() {
        super.onDestroy()
        locationUpdates.stopLocationUpdates(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
        locationUpdates.setLocationUpdateCallbacks(this)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        when (requestCode) {

            LocationUpdates.REQUEST_PERMISSIONS_REQUEST_CODE -> if (PermissionUtils.permissionsGrantedCheck(
                            grantResults
                    )
            ) {
                locationUpdates!!.startUpdates(requireActivity() as AppCompatActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            LocationUpdates.REQUEST_CHECK_SETTINGS -> if (resultCode == Activity.RESULT_OK) locationUpdates.startUpdates(
                    requireActivity() as AppCompatActivity
            )

        }
    }

    override fun locationReceiver(location: Location?) {
    }

    override fun lastLocationReceiver(location: Location?) {
    }

}