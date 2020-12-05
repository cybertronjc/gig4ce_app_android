package com.gigforce.app.modules.client_activation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.widgets.GigforceDatePickerDialog
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.*


class DocsSubSchedulerFragment : BaseFragment(),
        SelectPartnerSchoolBottomSheet.SelectPartnerBsCallbacks,
        TimeSlotsDialog.TimeSlotDialogCallbacks,
        ConfirmationDialogDrivingTest.ConfirmationDialogDrivingTestCallbacks,
        GigforceDatePickerDialog.GigforceDatePickerDialogCallbacks {
    private val viewModel: DocSubSchedulerViewModel by viewModels()

    private var dateString: String? = null
    private var partnerAddress: PartnerSchoolDetails? = null
    private lateinit var mWordOrderID: String
    private lateinit var mTitle: String
    private lateinit var mType: String
    private var selectedTimeSlot: String? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflateView(R.layout.fragment_docs_sub_scheduler, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)

        initViews()
        initClicks()
        initObservers()
    }

    private fun initObservers() {

        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {
            pb_docs_submission.gone()
            if (it == null) return@Observer

            val address = it.partnerSchoolDetails
            partnerAddress = address

            textView137.text =
                    Html.fromHtml(address?.name + "<br>" + address?.landmark + "<br>" + address?.city + "<br>"
                            + address?.timing + "<br>" + address?.contact?.map { "<b><font color=\'#000000\'>" + it.name + "</font></b>" }
                            ?.reduce { a, o -> a + o }
                    )
            iv_location.visible()
            iv_contact.visible()
            iv_location.setOnClickListener {
                val uri =
                        "http://maps.google.com/maps?saddr=" + "&daddr=" + address?.lat + "," + address?.lon
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }
            iv_contact.setOnClickListener {
                if (!address?.contact.isNullOrEmpty()) {
                    val callIntent = Intent(Intent.ACTION_DIAL);
                    callIntent.data = Uri.parse("tel: " + address?.contact!![0].number);
                    startActivity(callIntent);
                }
            }
            textView143.text = it.slotTime
            selectedTimeSlot = it.slotTime
            imageView36.gone()
            textView139.text = it.slotDate
            dateString = it.slotDate
            imageView35.gone()
            imageView34.gone()
            slider_checkout.isLocked = false
            slider_checkout.visibility = View.VISIBLE

        })
        viewModel.getApplication(mWordOrderID, mType, mTitle)

    }

    private fun initClicks() {
        view_select_time_slots.setOnClickListener {
            val newInstance = TimeSlotsDialog.newInstance()
            newInstance
            newInstance.setCallbacks(this)
            newInstance.show(parentFragmentManager, TimeSlotsDialog::class.java.name)
        }
        slider_checkout.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {

                    override fun onSlideComplete(view: SlideToActView) {

                        navigate(
                                R.id.fragment_schedule_test,
                                bundleOf(StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                                        StringConstants.TITLE.value to mTitle,
                                        StringConstants.TYPE.value to mType)
                        )
                    }
                }

        view_date_picker.setOnClickListener {
            var gigforceDatePickerDialog = GigforceDatePickerDialog()
            gigforceDatePickerDialog
            gigforceDatePickerDialog.setCallbacks(this)
            gigforceDatePickerDialog.show(
                    parentFragmentManager,
                    GigforceDatePickerDialog::class.java.name
            )
        }
        view7.setOnClickListener {
            val newInstance = SelectPartnerSchoolBottomSheet.newInstance(
                    bundleOf(
                            StringConstants.WORK_ORDER_ID.value to mWordOrderID
                    )
            )
            newInstance.setCallbacks(this)
            newInstance.show(parentFragmentManager, SelectPartnerSchoolBottomSheet.javaClass.name)
        }


    }

    private fun initViews() {
//        if (partnerAddress != null) {
//            setPartnerAddress(partnerAddress!!)
//        }
    }

    companion object {
        fun newInstance() = DocsSubSchedulerFragment()
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)
        outState.putString(StringConstants.TYPE.value, mType)
        outState.putString(StringConstants.TITLE.value, mTitle)


    }

    override fun setPartnerAddress(address: PartnerSchoolDetails) {
        this.partnerAddress = address;
        textView137.text =
                Html.fromHtml(address.name + "<br>" + address.landmark + "<br>" + address.city + "<br>"
                        + address.timing + "<br>" + address.contact.map { "<b><font color=\'#000000\'>" + it.name + "</font></b>" }
                        .reduce { a, o -> a + o }
                )
        iv_location.setOnClickListener {
            val uri =
                    "http://maps.google.com/maps?saddr=" + "&daddr=" + address?.lat + "," + address?.lon
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
        iv_contact.setOnClickListener {
            if (!address.contact.isNullOrEmpty()) {
                val callIntent = Intent(Intent.ACTION_DIAL);
                callIntent.data = Uri.parse("tel: " + address.contact[0].number);
                startActivity(callIntent);
            }
        }
//        imageView34.gone()
        iv_contact.visible()
        iv_location.visible()
        checkIfCompleteProcessComplete()
        imageView34.gone()

    }

    override fun setSelectedTimeSlot(time: String) {
        this.selectedTimeSlot = time
        textView143.text = time
        imageView36.gone()
        checkIfCompleteProcessComplete()
    }

    override fun moveToNextStep() {
        navigate(R.id.fragment_schedule_test)
    }

    override fun submissionSuccess() {
        slider_checkout.visible()
        slider_checkout.isLocked = false
    }

    override fun selectedDate(date: String) {


        this.dateString = date;
        textView139.text = dateString
        imageView35.gone()
        checkIfCompleteProcessComplete()
    }

    private fun checkIfCompleteProcessComplete() {
        slider_checkout.isLocked =
                !(dateString != null && partnerAddress != null && selectedTimeSlot != null)
        if (!slider_checkout.isLocked) {
            val confirmationDialogDrivingTest = ConfirmationDialogDrivingTest()
            confirmationDialogDrivingTest.setCallbacks(this@DocsSubSchedulerFragment)
            confirmationDialogDrivingTest.arguments = bundleOf(
                    StringConstants.SELECTED_PARTNER.value to partnerAddress,
                    StringConstants.SELECTED_TIME_SLOT.value to selectedTimeSlot,
                    StringConstants.SELECTED_DATE.value to dateString,
                    StringConstants.WORK_ORDER_ID.value to mWordOrderID,
                    StringConstants.TITLE.value to mTitle,
                    StringConstants.TYPE.value to mType
            )
            confirmationDialogDrivingTest.show(
                    parentFragmentManager,
                    ConfirmationDialogDrivingTest::class.java.name
            )
        } else {
            when {
                partnerAddress == null -> {
                    view7.performClick()
                }
                dateString.isNullOrEmpty() -> {
                    view_date_picker.performClick()
                }
                selectedTimeSlot.isNullOrEmpty() -> {
                    view_select_time_slots.performClick()
                }
            }

        }
    }


}