package com.gigforce.app.modules.gigPage2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.GigViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics

class GigPage2Fragment : BaseFragment() {

    private val viewModel: GigViewModel by viewModels()
    private lateinit var gigId: String

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_page_2, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments,savedInstanceState)
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        if(::gigId.isLateinit.not()){
            FirebaseCrashlytics.getInstance().setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            FirebaseCrashlytics.getInstance().log("GigPage2Fragment: No Gig id found")
        }
    }

    private fun initViewModel() {
        viewModel.gigDetails
                .observe(viewLifecycleOwner, Observer {
                    when (it) {
                        Lce.Loading -> showGigDetailsAsLoading()
                        is Lce.Content -> setGigDetailsOnView(it.content)
                        is Lce.Error -> showErrorWhileLoadingGigData(it.error)
                    }
                })

        viewModel.watchGig(gigId)
    }

    private fun showErrorWhileLoadingGigData(error: String) {

    }

    private fun showGigDetailsAsLoading() {

    }

    private fun setGigDetailsOnView(gig: Gig) {

        if(gig.isPastGig())
            showPastGigDetails(gig)
        else if(gig.isPresentGig())
            showPresentGigDetails(gig)
        else if(gig.isGigOfFuture())
            showFutureGigDetails(gig)
        else {
//            FirebaseCrashlytics.getInstance().apply {
//                log("Gig did not qualify for any criteria")
//            }
        }
    }

    private fun showPastGigDetails(gig: Gig) {

    }

    private fun showPresentGigDetails(gig: Gig) {

    }

    private fun showFutureGigDetails(gig: Gig) {

    }

    companion object {
        const val INTENT_EXTRA_GIG_ID = "gig_id"
        const val INTENT_EXTRA_COMING_FROM_CHECK_IN = "coming_from_checkin"
        const val TEXT_VIEW_ON_MAP = "(View On Map)"

        const val PERMISSION_FINE_LOCATION = 100
        const val REQUEST_CODE_UPLOAD_SELFIE_IMAGE = 2333
    }
}