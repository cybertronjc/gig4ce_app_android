package com.gigforce.app.modules.gigPage2

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.GigViewModel
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_gig_regularise_attendance.*
import kotlinx.android.synthetic.main.fragment_gig_regularise_attendance_main.*
import java.text.SimpleDateFormat
import java.util.*

class GigRegulariseAttendanceFragment  : BaseFragment() {

    private val viewModel : GigViewModel by viewModels()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    private val punchInTimePicker: TimePickerDialog by lazy {

        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(),
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                val gig = viewModel.currentGig ?: return@OnTimeSetListener
                val gigStartTime = gig.startDateTime!!.toLocalDateTime()

                val newCal = Calendar.getInstance()
                newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCal.set(Calendar.MINUTE, minute)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)

                newCal.set(Calendar.YEAR, gigStartTime.year)
                newCal.set(Calendar.MONTH, gigStartTime.monthValue -1)
                newCal.set(Calendar.DAY_OF_MONTH, gigStartTime.dayOfMonth)

                punchInTime = Timestamp(newCal.time)
                punch_in_time_tv.text = timeFormatter.format(newCal.time)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        )
    }

    private val punchOutTimePicker: TimePickerDialog by lazy {

        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(),
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->

                val gig = viewModel.currentGig ?: return@OnTimeSetListener
                val gigStartTime = gig.startDateTime!!.toLocalDateTime()

                val newCal = Calendar.getInstance()
                newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                newCal.set(Calendar.MINUTE, minute)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)
                newCal.set(Calendar.YEAR, gigStartTime.year)
                newCal.set(Calendar.MONTH, gigStartTime.monthValue -1)
                newCal.set(Calendar.DAY_OF_MONTH, gigStartTime.dayOfMonth)

                punchOutTime = Timestamp(newCal.time)
                punch_out_time_tv.text = timeFormatter.format(newCal.time)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        )
    }

    private lateinit var gigId : String

    private var punchInTime : Timestamp? = null
    private var punchOutTime : Timestamp? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_gig_regularise_attendance, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments,savedInstanceState)
        initView()

        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        savedInstanceState?.let {
            gigId = it.getString(INTENT_EXTRA_GIG_ID) ?: return@let
        }

        if (::gigId.isLateinit.not()) {
            FirebaseCrashlytics.getInstance()
                .setUserId(FirebaseAuth.getInstance().currentUser?.uid!!)
            FirebaseCrashlytics.getInstance().log("GigRegulariseAttendanceFragment: No Gig id found")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_GIG_ID,gigId)
    }

    private fun initView() {
        regularise_slider_btn.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener{

            override fun onSlideComplete(view: SlideToActView) {
                submitRegularisationRequest()
            }
        }

        punch_in_time_tv.setOnClickListener {
            punchInTimePicker.show()
        }

        punch_out_time_tv.setOnClickListener {
            punchOutTimePicker.show()
        }
    }


    private fun initViewModel() {

        viewModel.gigDetails
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showGigAttendanceDetailsLoading()
                    is Lce.Content -> showGigAttendanceDetails(it.content)
                    is Lce.Error -> showErrorInLoadingAttendanceDetails(it.error)
                }
            })

        viewModel.requestAttendanceRegularisation
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lse.Loading -> submittingRegularisationRequest()
                     Lse.Success -> regularisationRequestSubmitted()
                    is Lse.Error -> errorInSubmittingRegularisationRequest(it.error)
                }
            })

        viewModel.getGig(gigId)
    }

    private fun showGigAttendanceDetailsLoading() {
        regularise_main_layout.gone()
        regularise_details_error.gone()
        regularise_details_progress_bar.visible()
    }

    private fun showGigAttendanceDetails(content: Gig) {
        regularise_details_error.gone()
        regularise_details_progress_bar.gone()
        regularise_main_layout.visible()

        dateTV.text = dateFormatter.format(content.startDateTime!!.toDate())
    }

    private fun showErrorInLoadingAttendanceDetails(error: String) {
        regularise_details_progress_bar.gone()
        regularise_main_layout.gone()
        regularise_details_error.visible()

        regularise_details_error.text = error
    }

    private fun submittingRegularisationRequest() {
        regularise_main_layout.gone()
        regularise_details_error.gone()
        regularise_details_progress_bar.visible()
    }

    private fun regularisationRequestSubmitted() {
        showToast("Regularisation request submitted")
        activity?.onBackPressed()
    }

    private fun errorInSubmittingRegularisationRequest(error: String) {
        regularise_details_progress_bar.gone()
        regularise_main_layout.visible()
        regularise_slider_btn.resetSlider()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Unable to submit regularisation request , $error")
            .setPositiveButton(R.string.okay_text){_,_ ->

            }.show()
    }

    private fun submitRegularisationRequest() {
        if(punchInTime == null){
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Select Punch-in time")
                .setPositiveButton(R.string.okay_text){_,_ ->

                }.show()

            regularise_slider_btn.resetSlider()
            return
        }

        if(punchOutTime == null){
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Select Punch-out time")
                .setPositiveButton(R.string.okay_text){_,_ ->

                }.show()

            regularise_slider_btn.resetSlider()
            return
        }

        val punchIn = punchInTime!!.toLocalDateTime()
        val punchOut = punchOutTime!!.toLocalDateTime()

        if(punchIn.isAfter(punchOut) || punchIn.isEqual(punchOut)){
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Punch-out time should be greater than punch-in time")
                .setPositiveButton(R.string.okay_text){_,_ ->
                }.show()

            regularise_slider_btn.resetSlider()
            return
        }

        viewModel.requestRegularisation(gigId, punchInTime!!, punchOutTime!!)
    }

    companion object{
        const val INTENT_EXTRA_GIG_ID = "gig_id"
    }
}