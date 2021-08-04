package com.gigforce.giger_gigs.tl_login_details

import android.app.DatePickerDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.DateHelper
import com.gigforce.core.utils.Lce
import com.gigforce.giger_gigs.adapters.TLLoginSummaryAdapter
import com.gigforce.giger_gigs.databinding.TeamLeaderLoginDetailsFragmentBinding
import com.gigforce.giger_gigs.models.ListingTLModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TeamLeaderLoginDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = TeamLeaderLoginDetailsFragment()
    }

    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewModel: TeamLeaderLoginDetailsViewModel
    private lateinit var viewBinding: TeamLeaderLoginDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = TeamLeaderLoginDetailsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TeamLeaderLoginDetailsViewModel::class.java)
        initToolbar()
        initializeViews()
        observer()
        listeners()
    }

    private fun initToolbar() = viewBinding.apply {
        appBar.apply {
            hideActionMenu()
            showTitle("Login Summary")
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }
    }

    private fun initializeViews() {
        viewModel.getListingForTL()
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
                viewBinding.searchDate.text = DateHelper.getDateInDDMMYYYY(newCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis
        datePickerDialog
    }

    private fun listeners() = viewBinding.apply {
        addNew.setOnClickListener {
            navigation.navigateTo("gig/addNewLoginSummary")
        }

        searchDate.setOnClickListener {
            datePicker.show()
        }
    }

    private fun observer() {
        viewModel.loginListing.observe(viewLifecycleOwner, Observer {
            val res = it ?: return@Observer
            when(res){
                Lce.Loading -> {

                }

                is Lce.Content -> {
                    setupReyclerView(res.content)
                }
            }


        })
    }

    private fun setupReyclerView(res: List<ListingTLModel>) {
        val adapter = TLLoginSummaryAdapter()
        viewBinding.datecityRv.layoutManager = LinearLayoutManager(context)
        adapter.submitList(res)
        viewBinding.datecityRv.adapter = adapter
    }


}