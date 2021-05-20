package com.gigforce.app.modules.explore_by_role

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.client_activation.client_activation.models.Role
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.app.utils.*
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.decors.HorizontaltemDecoration
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.ViewModelProviderFactory
import com.gigforce.core.utils.PermissionUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.layout_marked_interest_success_fragment.*

class MarkedInterestSuccessFragment : BaseFragment(),
    AdapterExploreByRole.AdapterExploreByRoleCallbacks, LocationUpdates.LocationUpdateCallbacks {
    private var mRoleID: String? = null
    private var roleUpdated: Boolean = false
    private var inviteID: String? = null
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

    var locationUpdates: LocationUpdates? = LocationUpdates()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_marked_interest_success_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromSavedState(savedInstanceState)
        enableGpsAndCaptureLocation()
        initClicks()
        setUpRecycler()
        initObservers()

        pb_marked_as_interest.visible()

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
                    if (rolesList?.isNotEmpty()!!) {
                        tv_explore_more_mark_interest.visible()
                        adapter.addData(rolesList ?: mutableListOf())
                    }


                    pb_marked_as_interest.gone()
                })
            }


        })
        viewModel.observerError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
            pb_marked_as_interest.gone()
        })
        viewModel.observerVerified.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                navFragmentsData?.getData()?.putString(StringConstants.INVITE_USER_ID.value, null)
                navFragmentsData?.getData()?.putString(StringConstants.ROLE_ID.value, null)
                navFragmentsData?.getData()
                    ?.putBoolean(StringConstants.ROLE_VIA_DEEPLINK.value, false)

                tv_title_mark_as_interest.text = getString(R.string.role_activated)
                tv_mark_as_interest_note.setPadding(
                    0,
                    0,
                    0,
                    resources.getDimensionPixelSize(R.dimen.size_34)
                )
                tv_verify_documents.gone()

            } else {
                tv_title_mark_as_interest.text =
                    getString(R.string.application_submission_successfull)
                tv_verify_documents.visible()


            }
        })
        viewModel.observerMarkedAsInterest.observe(viewLifecycleOwner, Observer {
            viewModel.getRoles()
            sv_marked_as_interest.visible()
//            pb_marked_as_interest.gone()
        })


        pb_marked_as_interest.visible()

        viewModel.checkVerifiedDocs()
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
        adapter.setCallbacks(this)
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

    override fun onItemClicked(id: String?) {
        findNavController().navigate(MarkedInterestSuccessFragmentDirections.openRoleDetails(id!!))

    }

    private fun getDataFromSavedState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            inviteID = it.getString(StringConstants.INVITE_USER_ID.value)
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
        }

        arguments?.let {
            inviteID = it.getString(StringConstants.INVITE_USER_ID.value)
            mRoleID = it.getString(StringConstants.ROLE_ID.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.ROLE_ID.value, mRoleID)
        outState.putString(StringConstants.INVITE_USER_ID.value, inviteID)

    }


    fun enableGpsAndCaptureLocation() {
        if (canToggleGPS()) {
            turnGPSOn()
        }

    }


    private fun turnGPSOn() {
        val provider = Settings.Secure.getString(
            context?.contentResolver,
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        if (!provider.contains("gps")) { //if gps is disabled
            val poke = Intent()
            poke.setClassName(
                "com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider"
            )
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE)
            poke.data = Uri.parse("3")
            context?.let { it ->
                LocalBroadcastManager.getInstance(it).sendBroadcast(poke)
            } ?: run {

                FirebaseCrashlytics.getInstance()
                    .log("Context found null in GigPageFragment/turnGPSOn()")
            }
        }
    }

    private fun canToggleGPS(): Boolean {
        val pacman = context?.getPackageManager()
        var pacInfo: PackageInfo? = null
        try {
            pacInfo = pacman?.getPackageInfo("com.android.settings", PackageManager.GET_RECEIVERS)
        } catch (e: PackageManager.NameNotFoundException) {
            return false //package not found
        } catch (e: Exception) {

        }
        if (pacInfo != null) {
            for (actInfo in pacInfo.receivers) {
                //test if recevier is exported. if so, we can toggle GPS.
                if (actInfo.name.equals("com.android.settings.widget.SettingsAppWidgetProvider") && actInfo.exported) {
                    return true
                }
            }
        }
        return false //default
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUpdates!!.stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        locationUpdates!!.startUpdates(requireActivity() as AppCompatActivity)
        locationUpdates!!.setLocationUpdateCallbacks(this)
    }

    override fun locationReceiver(location: Location?) {
        if (!roleUpdated) {
            viewModel.addAsInterest(
                mRoleID!!,
                location,
                inviteID ?: ""

            )
            locationUpdates?.stopLocationUpdates()
            roleUpdated = true
        }

    }

    var location: Location? = null

    override fun lastLocationReceiver(location: Location?) {
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

            LocationUpdates.REQUEST_CHECK_SETTINGS -> if (resultCode == Activity.RESULT_OK) locationUpdates!!.startUpdates(
                requireActivity() as AppCompatActivity
            )

        }
    }

}