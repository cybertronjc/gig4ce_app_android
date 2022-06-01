package com.gigforce.giger_gigs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.navigation.gigs.GigNavigation
import com.gigforce.giger_gigs.adapters.GigAttendanceAdapter
import com.gigforce.giger_gigs.adapters.GigAttendanceAdapterClickListener
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.common_ui.viewmodels.gig.GigViewModel
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.viewModels.GigerAttendanceViewModel
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_gig_monthly_attendance.*
import kotlinx.android.synthetic.main.fragment_gig_monthly_attendance_toolbar.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*

@AndroidEntryPoint
class GigMonthlyAttendanceFragment : Fragment(), GigAttendanceAdapterClickListener {

    private val viewModel: GigerAttendanceViewModel by viewModels()

    private val adapter: GigAttendanceAdapter by lazy {
        GigAttendanceAdapter(
            requireContext()
        ).apply {
            setListener(this@GigMonthlyAttendanceFragment)
        }
    }

    private var role: String? = null
    private var companyName: String? = null
    private var companyLogo: String? = null
    private lateinit var gigOrderId: String

    private var currentlySelectedMonthYear: LocalDate = LocalDate.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_monthly_attendance, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)
        initUi()
        initViewModel()
        showMonthYearValueOnViewAndStartFetchingData()
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(
                resources,
                R.color.lipstick_two,
                null
        ))
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            val monthYear = it.getSerializable(GigNavigation.INTENT_EXTRA_SELECTED_DATE)
            if (monthYear != null) currentlySelectedMonthYear = monthYear as LocalDate

            role = it.getString(GigNavigation.INTENT_EXTRA_ROLE)
            companyName = it.getString(GigNavigation.INTENT_EXTRA_COMPANY_NAME)
            companyLogo = it.getString(GigNavigation.INTENT_EXTRA_COMPANY_LOGO)
            gigOrderId = it.getString(GigNavigation.INTENT_EXTRA_GIG_ORDER_ID) ?: throw IllegalArgumentException(
                "Gig order id not passed in intent"
            )
        }

        savedInstanceState?.let {
            currentlySelectedMonthYear = it.getSerializable(GigNavigation.INTENT_EXTRA_SELECTED_DATE) as LocalDate

            role = it.getString(GigNavigation.INTENT_EXTRA_ROLE)
            companyName = it.getString(GigNavigation.INTENT_EXTRA_COMPANY_NAME)
            companyLogo = it.getString(GigNavigation.INTENT_EXTRA_COMPANY_LOGO)
            gigOrderId = it.getString(GigNavigation.INTENT_EXTRA_GIG_ORDER_ID) ?: throw IllegalArgumentException(
                "Gig order id not passed in saved intent"
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(GigNavigation.INTENT_EXTRA_SELECTED_DATE, currentlySelectedMonthYear)

        outState.putString(GigNavigation.INTENT_EXTRA_ROLE, role)
        outState.putString(GigNavigation.INTENT_EXTRA_COMPANY_NAME, companyName)
        outState.putString(GigNavigation.INTENT_EXTRA_COMPANY_LOGO, companyLogo)
        outState.putString(GigNavigation.INTENT_EXTRA_GIG_ORDER_ID, gigOrderId)
    }

    private fun initUi() {

        gig_ellipses_iv.gone()
        dateYearTV.setOnClickListener {
            showMonthCalendar()
        }

        gig_cross_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        attendance_monthly_rv.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        attendance_monthly_rv.adapter = adapter

        if (!companyLogo.isNullOrBlank()) {
            if (companyLogo!!.startsWith("http", true)) {

                Glide.with(requireContext())
                    .load(companyLogo)
                    .placeholder(getCircularProgressDrawable())
                    .into(company_logo_iv)
            } else {
                val imageRef = FirebaseStorage.getInstance()
                    .getReference("companies_gigs_images")
                    .child(companyLogo!!)

                Glide.with(requireContext())
                    .load(imageRef)
                    .placeholder(getCircularProgressDrawable())
                    .into(company_logo_iv)
            }
        } else {
            val companyInitials = if (companyLogo.isNullOrBlank())
                "C"
            else
                companyLogo!![0].toString().toUpperCase()
            val drawable = TextDrawable.builder().buildRound(
                companyInitials,
                ResourcesCompat.getColor(resources, R.color.lipstick, null)
            )

            company_logo_iv.setImageDrawable(drawable)
        }

        gig_title_tv.text = role
        gig_company_name_tv.text = "\ufeff@ $companyName"

        attendance_type_chipgroup.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.attendance_all_chip -> adapter.showAllAttendances()
                R.id.attendance_present_chip -> adapter.showPresentAttendances()
                R.id.attendance_absent_chip -> adapter.showAbsentAttendances()
            }
        }
    }

    private fun initViewModel() {
        viewModel.monthlyGigs
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                        attendance_monthly_learning_error.gone()
                        attendance_montly_progress_bar.visible()
                    }
                    is Lce.Content -> {
                        attendance_monthly_learning_error.gone()
                        attendance_montly_progress_bar.gone()

                        setGigAttendanceOnView(it.content)
                    }
                    is Lce.Error -> {
                        attendance_montly_progress_bar.gone()
                        attendance_monthly_learning_error.visible()

                        attendance_monthly_learning_error.text = it.error
                    }
                }
            })
    }

    private fun setGigAttendanceOnView(content: List<Gig>) {
        attendance_montly_progress_bar.gone()
        attendance_monthly_learning_error.gone()
        attendance_monthly_rv.visible()

        var completedGigsCount = 0
        var absentGigsCount = 0

        content.forEach {
            val status = GigStatus.fromGig(it)

            when (status) {
                GigStatus.COMPLETED, GigStatus.ONGOING -> {
                    completedGigsCount++
                }
                GigStatus.DECLINED, GigStatus.MISSED, GigStatus.NO_SHOW -> {
                    absentGigsCount++
                }
            }
        }

        total_days_tv.text = "  : ${completedGigsCount} ${getString(R.string.days)}"
        total_working_days_tv.text = "  : ${absentGigsCount} ${getString(R.string.days)}"

        attendance_type_chipgroup.check(R.id.attendance_all_chip)
        adapter.updateAttendanceList(content)
        if (content.isEmpty()) {
            attendance_monthly_learning_error.visible()
            attendance_monthly_learning_error.text = getString(R.string.no_gigs_assigned_giger_gigs)
        } else {
            attendance_monthly_learning_error.gone()
        }
    }


    override fun onAttendanceClicked(option: Gig) {
//        navigate(
//                R.id.gigsAttendanceForADayDetailsBottomSheet, bundleOf(
//                GigsAttendanceForADayDetailsBottomSheet.INTENT_GIG_ID to option.gigId
//        )
//        )
    }

    fun showMonthCalendar() {

        val selectedDate: Pair<Int, Int> = Pair(
            currentlySelectedMonthYear.monthValue,
            currentlySelectedMonthYear.year
        )
        val maxDate = Date().time

        val localDate = LocalDateTime.of(2015, 1, 1, 0, 0)
        val zdt: ZonedDateTime = ZonedDateTime.of(localDate, ZoneId.systemDefault())
        val defaultMinTime: Long = zdt.toInstant().toEpochMilli()

        MonthYearPickerDialogFragment.getInstance(
            selectedDate.first - 1,
            selectedDate.second,
            defaultMinTime,
            maxDate
        ).apply {
            setOnDateSetListener { year, monthOfYear ->

                Log.d("AddExp", "End Values Set Month : $monthOfYear")
                Log.d("AddExp", "End Values Set Year : $year")

                val newCal = Calendar.getInstance()
                newCal.set(Calendar.YEAR, year)
                newCal.set(Calendar.MONTH, monthOfYear)
                newCal.set(Calendar.DAY_OF_MONTH, 1)
                newCal.set(Calendar.HOUR_OF_DAY, 0)
                newCal.set(Calendar.MINUTE, 0)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)

                currentlySelectedMonthYear = newCal.time.toLocalDate()
                showMonthYearValueOnViewAndStartFetchingData()
            }
        }.show(childFragmentManager, "MonthYearPickerDialogFragment")
    }

    private fun showMonthYearValueOnViewAndStartFetchingData() {
        val monthName = currentlySelectedMonthYear.month.getDisplayName(
            TextStyle.SHORT,
            Locale.getDefault()
        )
        val year = currentlySelectedMonthYear.year
        dateYearTV.text = "$monthName - $year"

        viewModel.getGigsForMonth(
            gigOrderId = gigOrderId,
            month = currentlySelectedMonthYear.monthValue,
            year = currentlySelectedMonthYear.year
        )
    }
}