package com.gigforce.giger_gigs.tl_login_report

import android.app.DatePickerDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.format.DateUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.LoginSummaryConstants
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.adapters.OnTlReportItemSelectedListener
import com.gigforce.giger_gigs.adapters.TLLoginReportAdapter
import com.gigforce.giger_gigs.adapters.TLLoginSummaryAdapter
import com.gigforce.giger_gigs.databinding.FragmentTlDailyLoginReportListBinding
import com.gigforce.giger_gigs.databinding.TeamLeaderLoginDetailsFragmentBinding
import com.gigforce.giger_gigs.models.DailyLoginReport
import com.gigforce.giger_gigs.models.ListingTLModel
import com.gigforce.giger_gigs.tl_login_details.views.OnTlItemSelectedListener
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TLDailyReportListFragment : BaseFragment2<FragmentTlDailyLoginReportListBinding>(
    fragmentName = "TLDailyReportListFragment",
    layoutId = R.layout.fragment_tl_daily_login_report_list,
    statusBarColor = R.color.white
), OnTlReportItemSelectedListener {

    companion object {
        fun newInstance() = TLDailyReportListFragment()

        private const val INTENT_EXTRA_DATE = "date"
    }

    @Inject
    lateinit var navigation: INavigation
    private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
    private val viewModel: TLDailyReportListViewModel by viewModels()
    private val dateFormatter =  SimpleDateFormat("dd-MMM-yyyy")
    private val standardDateFormatter =  SimpleDateFormat("dd-MM-yyyy")
    private var currentlySelectedDate = Date()

    private val tlLoginSummaryAdapter: TLLoginReportAdapter by lazy {
        TLLoginReportAdapter().apply {
            setOnTlItemSelectedListener(this@TLDailyReportListFragment)
        }
    }


    override fun viewCreated(
        viewBinding: FragmentTlDailyLoginReportListBinding,
        savedInstanceState: Bundle?
    ) {
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initToolbar()
        initializeViews()
        observer()
        listeners()
    }

    private fun getDataFrom(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            val serializedDate = it.getSerializable(INTENT_EXTRA_DATE) ?: return@let
            currentlySelectedDate = serializedDate as Date
        }

        savedInstanceState?.let {
            val serializedDate = it.getSerializable(INTENT_EXTRA_DATE) ?: return@let
            currentlySelectedDate = serializedDate as Date
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(INTENT_EXTRA_DATE,currentlySelectedDate)
    }

    private fun initToolbar() = viewBinding.apply {
        appBar.apply {
//            hideActionMenu()
//            showTitle("Login Report")
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }
    }

    private fun initializeViews() = viewBinding.apply {

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userUid = FirebaseAuth.getInstance().uid
                val profileData = profileFirebaseRepository.getProfileData(userUid)
                viewBinding.tlNameTv.text = profileData.name
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        viewBinding.dateTv.text = dateFormatter.format(currentlySelectedDate)
        viewModel.getListingForTL("", standardDateFormatter.format(currentlySelectedDate))
    }

    private val datePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, month)
                newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewBinding.dateTv.text = dateFormatter.format(newCal.time)
                currentlySelectedDate = newCal.time

                viewModel.getListingForTL("", standardDateFormatter.format(newCal.time))
            },
            2050,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun listeners() = viewBinding.apply {
        addNew.setOnClickListener {
            navigation.navigateTo("tlReport/addLoginReportFragment", bundleOf(
                LoginSummaryConstants.INTENT_EXTRA_MODE to LoginSummaryConstants.MODE_ADD
            ))
        }

        changeDateBtn.setOnClickListener {
            datePicker.show()
        }

        swipeToRefresh.setOnRefreshListener {
            viewModel.getListingForTL("", standardDateFormatter.format(currentlySelectedDate))
        }
    }

    private fun observer() = viewBinding.apply {
        viewModel.loginListing.observe(viewLifecycleOwner, Observer {
            val res = it ?: return@Observer
            when(res){
                Lce.Loading -> {
                    if(tlLoginSummaryAdapter.itemCount != 0){
                        swipeToRefresh.isRefreshing = true
                    } else {
                        progressBar.visibility = View.VISIBLE
                    }
                }

                is Lce.Content -> {
                    progressBar.visibility = View.GONE
                    if(swipeToRefresh.isRefreshing)
                        swipeToRefresh.isRefreshing = false

                    setupReyclerView(res.content)
                    showToast("Data refreshed")
                }

                is Lce.Error -> {
                    showToast("Error loading data")
                    progressBar.visibility = View.GONE

                    if(swipeToRefresh.isRefreshing)
                      swipeToRefresh.isRefreshing = false
                }
            }
        })
    }

    private fun setupReyclerView(res: List<DailyLoginReport>) {

        if (res.isEmpty()){
            viewBinding.noData.visibility = View.VISIBLE
            viewBinding.datecityRv.visibility = View.GONE
        }else {
            viewBinding.noData.visibility = View.GONE
            viewBinding.datecityRv.visibility = View.VISIBLE
        }
        viewBinding.datecityRv.layoutManager = LinearLayoutManager(context)
        tlLoginSummaryAdapter.submitList(res)
        viewBinding.datecityRv.adapter = tlLoginSummaryAdapter
    }


    override fun onTlReportSelected(listingTLModel: DailyLoginReport) {
        if (DateUtils.isToday(listingTLModel.dateTimestamp)){
            navigation.navigateTo("tlReport/addLoginReportFragment", bundleOf(
                LoginSummaryConstants.INTENT_EXTRA_MODE to LoginSummaryConstants.MODE_EDIT,
                LoginSummaryConstants.INTENT_LOGIN_SUMMARY to listingTLModel
            ))
        }else {
            navigation.navigateTo("tlReport/addLoginReportFragment", bundleOf(
                LoginSummaryConstants.INTENT_LOGIN_SUMMARY to listingTLModel,
                LoginSummaryConstants.INTENT_EXTRA_MODE to LoginSummaryConstants.MODE_VIEW
            ))

        }
    }
}