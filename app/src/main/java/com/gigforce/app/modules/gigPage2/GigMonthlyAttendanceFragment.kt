package com.gigforce.app.modules.gigPage2

import android.os.Bundle
import android.os.CountDownTimer
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
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.GigViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.TextDrawable
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_gig_monthly_attendance.*
import kotlinx.android.synthetic.main.fragment_gig_monthly_attendance_toolbar.*
import java.text.SimpleDateFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class GigMonthlyAttendanceFragment : BaseFragment(), GigAttendanceAdapterClickListener {

    private val viewModel: GigViewModel by viewModels()

    private var month: Int = -1
    private var year: Int = -1
    private var role: String? = null
    private var companyName: String? = null
    private var companyLogo: String? = null
    private var rating: Float = 0.0f

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
            month = it.getInt(INTENT_EXTRA_MONTH)
            year = it.getInt(INTENT_EXTRA_YEAR)
            rating = it.getFloat(INTENT_EXTRA_RATING)

            role = it.getString(INTENT_EXTRA_ROLE)
            companyName = it.getString(INTENT_EXTRA_COMPANY_NAME)
            companyLogo = it.getString(INTENT_EXTRA_COMPANY_LOGO)
        }

        savedInstanceState?.let {
            month = it.getInt(INTENT_EXTRA_MONTH)
            year = it.getInt(INTENT_EXTRA_YEAR)
            rating = it.getFloat(INTENT_EXTRA_RATING)

            role = it.getString(INTENT_EXTRA_ROLE)
            companyName = it.getString(INTENT_EXTRA_COMPANY_NAME)
            companyLogo = it.getString(INTENT_EXTRA_COMPANY_LOGO)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(INTENT_EXTRA_MONTH, month)
        outState.putInt(INTENT_EXTRA_YEAR, year)

        outState.putFloat(INTENT_EXTRA_YEAR, rating)

        outState.putString(INTENT_EXTRA_ROLE, role)
        outState.putString(INTENT_EXTRA_COMPANY_NAME, companyName)
        outState.putString(INTENT_EXTRA_COMPANY_LOGO, companyLogo)
    }

    private fun initUi() {

        dateYearTV.setOnClickListener {

        }

        if (!companyLogo.isNullOrBlank()) {
            if (companyLogo!!.startsWith("http", true)) {

                GlideApp.with(requireContext())
                    .load(companyLogo)
                    .placeholder(getCircularProgressDrawable())
                    .into(company_logo_iv)
            } else {
                FirebaseStorage.getInstance()
                    .getReference("companies_gigs_images")
                    .child(companyLogo!!)
                    .downloadUrl
                    .addOnSuccessListener { fileUri ->

                        GlideApp.with(requireContext())
                            .load(fileUri)
                            .placeholder(getCircularProgressDrawable())
                            .into(company_logo_iv)
                    }
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

        val monthName = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
        dateYearTV.text = "$monthName - $year"

        gig_title_tv.text = role
        gig_company_name_tv.text = "\ufeff@ $companyName"
        company_rating_tv.text = if (rating != 0.0f)
            "-"
        else
            rating.toString()

    }

    private fun initViewModel() {
        viewModel.monthlyGigs
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lce.Loading -> {
                    }
                    is Lce.Content -> setGigAttendanceOnView(it.content)
                    is Lce.Error -> {
                    }
                }
            })

        viewModel.getGigsForMonth("Seedworks", 10, 2020)
    }

    private fun setGigAttendanceOnView(content: List<Gig>) {
        attendance_montly_progress_bar.gone()
        attendance_monthly_learning_error.gone()
        attendance_monthly_rv.visible()

        attendance_monthly_rv.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )

        val adapter = GigAttendanceAdapter(
            requireContext(),
            content.sortedBy { it.startDateTime!!.seconds }
        ).apply {
            setListener(this@GigMonthlyAttendanceFragment)
        }
        attendance_monthly_rv.adapter = adapter
    }

    companion object {
        const val INTENT_EXTRA_COMPANY_NAME = "company_name"
        const val INTENT_EXTRA_COMPANY_LOGO = "company_logo"
        const val INTENT_EXTRA_ROLE = "role"
        const val INTENT_EXTRA_RATING = "rating"
        const val INTENT_EXTRA_MONTH = "month"
        const val INTENT_EXTRA_YEAR = "year"
    }

    override fun onAttendanceClicked(option: Gig) {
        navigate(
            R.id.gigsAttendanceForADayDetailsBottomSheet, bundleOf(
                GigsAttendanceForADayDetailsBottomSheet.INTENT_GIG_ID to option.gigId
            )
        )
    }
}