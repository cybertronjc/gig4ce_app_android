package com.gigforce.app.modules.gigPage2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigPage.DeclineGigDialogFragmentResultListener
import com.gigforce.app.modules.gigPage.GigViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.fragment_gig_page_2_details.*
import java.text.SimpleDateFormat
import java.util.*

class GigMonthlyAttendanceFragment : BaseFragment() {

    private val viewModel: GigViewModel by viewModels()
    private lateinit var companyName: String
    private var month: Int = -1
    private var year: Int = -1

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_monthly_attendance, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            gigId = it.getString(GigPage2Fragment.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(GigPage2Fragment.INTENT_EXTRA_GIG_ID) ?: return@let
        }

        if (::gigId.isLateinit.not()) {
            FirebaseCrashlytics.getInstance()
                .setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            FirebaseCrashlytics.getInstance().log("GigPage2Fragment: No Gig id found")
        }
    }

    private fun initUi() {
        toolbar?.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun initViewModel() {
        viewModel.monthlyGigs
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                    }
                    is Lce.Content -> setGigAttendanceOnView(it.content)
                    is Lce.Error -> {}
                }
            })

        viewModel.getGigsForMonth(companyName,month, year)
    }

    private fun setGigAttendanceOnView(content: List<Gig>) {

    }

    companion object {
        const val INTENT_EXTRA_COMPANY_NAME = "company_name"
        const val INTENT_EXTRA_MONTH = "month"
        const val INTENT_EXTRA_YEAR = "year"
    }
}