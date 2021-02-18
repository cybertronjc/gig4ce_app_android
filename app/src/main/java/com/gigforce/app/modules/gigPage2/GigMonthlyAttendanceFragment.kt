package com.gigforce.app.modules.gigPage2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.GigViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage2.adapters.GigAttendanceAdapter
import com.gigforce.app.modules.gigPage2.adapters.GigAttendanceAdapterClickListener
import com.gigforce.app.modules.gigPage2.models.GigStatus
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_gig_monthly_attendance.*
import kotlinx.android.synthetic.main.fragment_gig_monthly_attendance_toolbar.*
import java.time.*
import java.time.format.TextStyle
import java.util.*

class GigMonthlyAttendanceFragment : BaseFragment(), GigAttendanceAdapterClickListener {

    private val viewModel: GigViewModel by viewModels()

    private var role: String? = null
    private var companyName: String? = null
    private var companyLogo: String? = null
    private lateinit var gigOrderId: String

    private var currentlySelectedMonthYear: LocalDate = LocalDate.now()

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
        showMonthYearValueOnViewAndStartFetchingData()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            val monthYear = it.getSerializable(INTENT_EXTRA_SELECTED_DATE)
            if (monthYear != null) currentlySelectedMonthYear = monthYear as LocalDate

            role = it.getString(INTENT_EXTRA_ROLE)
            companyName = it.getString(INTENT_EXTRA_COMPANY_NAME)
            companyLogo = it.getString(INTENT_EXTRA_COMPANY_LOGO)
            gigOrderId = it.getString(INTENT_EXTRA_GIG_ORDER_ID) ?: throw IllegalArgumentException(
                    "Gig order id not passed in intent"
            )
        }

        savedInstanceState?.let {
            currentlySelectedMonthYear = it.getSerializable(INTENT_EXTRA_SELECTED_DATE) as LocalDate

            role = it.getString(INTENT_EXTRA_ROLE)
            companyName = it.getString(INTENT_EXTRA_COMPANY_NAME)
            companyLogo = it.getString(INTENT_EXTRA_COMPANY_LOGO)
            gigOrderId = it.getString(INTENT_EXTRA_GIG_ORDER_ID) ?: throw IllegalArgumentException(
                    "Gig order id not passed in saved intent"
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(INTENT_EXTRA_SELECTED_DATE, currentlySelectedMonthYear)

        outState.putString(INTENT_EXTRA_ROLE, role)
        outState.putString(INTENT_EXTRA_COMPANY_NAME, companyName)
        outState.putString(INTENT_EXTRA_COMPANY_LOGO, companyLogo)
        outState.putString(INTENT_EXTRA_GIG_ORDER_ID, gigOrderId)
    }

    private fun initUi() {

        dateYearTV.setOnClickListener {
            showMonthCalendar()
        }

        gig_cross_btn.setOnClickListener {
            activity?.onBackPressed()
        }

        if (!companyLogo.isNullOrBlank()) {
            if (companyLogo!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                        .load(companyLogo)
                        .placeholder(getCircularProgressDrawable())
                        .into(company_logo_iv)
            } else {
                val imageRef = FirebaseStorage.getInstance()
                        .getReference("companies_gigs_images")
                        .child(companyLogo!!)

                GlideApp.with(requireContext())
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

            if (status == GigStatus.COMPLETED) {
                completedGigsCount++
            } else if (status == GigStatus.MISSED) {
                absentGigsCount++
            }
        }

        total_days_tv.text = ": ${completedGigsCount} Days"
        total_working_days_tv.text = ": ${absentGigsCount} Days"

        attendance_monthly_rv.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
        )

        val adapter = GigAttendanceAdapter(
                requireContext(),
                content.sortedBy { it.startDateTime.seconds }
        ).apply {
            setListener(this@GigMonthlyAttendanceFragment)
        }
        attendance_monthly_rv.adapter = adapter
    }


    override fun onAttendanceClicked(option: Gig) {
        navigate(
                R.id.gigsAttendanceForADayDetailsBottomSheet, bundleOf(
                GigsAttendanceForADayDetailsBottomSheet.INTENT_GIG_ID to option.gigId
        )
        )
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


    companion object {
        const val INTENT_EXTRA_ROLE = "role"
        const val INTENT_EXTRA_COMPANY_NAME = "company_name"
        const val INTENT_EXTRA_COMPANY_LOGO = "company_logo"
        const val INTENT_EXTRA_SELECTED_DATE = "selected_month_year"
        const val INTENT_EXTRA_GIG_ORDER_ID = "gig_order_id"
    }

}