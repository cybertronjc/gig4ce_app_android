package com.gigforce.app.modules.client_activation

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
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
import com.gigforce.app.modules.client_activation.models.GFMappedUser
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.widgets.GigforceDatePickerDialog
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.*
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.driving_license_title
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.iv_contact
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.iv_location
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.textView136
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.textView137
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.textView138
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.textView139
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.textView142
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.textView143
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.tv_all_set
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.tv_change_slot
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.view7
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.view_select_time_slots


class DocsSubSchedulerFragment : BaseFragment() {
    private val viewModel: DocSubSchedulerViewModel by viewModels()

    private var dateString: String? = null
//    private var partnerAddress: PartnerSchoolDetails? = null
    private lateinit var mJobProfileId: String
    private lateinit var mTitle: String
    private lateinit var mType: String
//    private var selectedTimeSlot: String? = null
    private var isCheckOutDone: Boolean = false


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
        viewModel.observableQuestionnairDocument.observe(viewLifecycleOwner, Observer {
            showToast(it?.stepId.toString())
            Log.e("data",it?.stepId.toString())
            it?.answers?.forEach {
                Log.e("data",it?.type!!+it?.options?.size.toString())
                showToast(it?.type!!+it?.options?.size.toString())
                if(it?.type == "dropdown" && it?.options?.size==1){
                    it?.options?.forEach {
                        Log.e("datamsg",it.answer)
                        viewModel.getMappedUser(it.answer.toString())
                    }
                }
            }
        })

        viewModel.observableMappedUser.observe(viewLifecycleOwner, Observer {
            Log.e("datamsg",it.name)
            initMappedUser(it)
        })

        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observableIsCheckoutDone.observe(viewLifecycleOwner, Observer {
            if (it == false) {
//                initClicks()
            }
        })
        viewModel.observablePartnerSchool.observe(viewLifecycleOwner, Observer {

            doc_details.text = Html.fromHtml(it.headerTitle)


            viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {

                pb_docs_submission.gone()
                if (it == null) return@Observer

//                val selectedPartner = it.partnerSchoolDetails
//
//                partnerAddress = selectedPartner

//                textView137.text =
//                    Html.fromHtml((if (selectedPartner?.name == null) "" else selectedPartner?.name + "<br>") +
//                            (if (selectedPartner?.line1 == null) "" else selectedPartner?.line1 + "<br>") +
//                            (if (selectedPartner?.line2 == null) "" else selectedPartner?.line2 + "<br>") +
//                            (if (selectedPartner?.line3 == null) "" else selectedPartner?.line3 + "<br>") +
//                            if (selectedPartner?.contact.isNullOrEmpty()) "" else
//                                selectedPartner?.contact?.map { "<b><font color=\'#000000\'>" + it.name + "</font></b><br>" }
//                                    ?.reduce { a, o -> a + o }
//                    )

//                if (!partnerAddress?.lat.isNullOrEmpty()) {
//                    iv_location.visible()
//                    iv_location.setOnClickListener {
//                        val uri =
//                            "http://maps.google.com/maps?saddr=" + "&daddr=" + partnerAddress?.lat + "," + partnerAddress?.lon
//                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//                        startActivity(intent)
//                    }
//                } else {
//                    iv_location.gone()
//                }
//                iv_contact.visible()
//                iv_contact.setOnClickListener {
//                    if (!selectedPartner?.contact.isNullOrEmpty()) {
//                        val callIntent = Intent(Intent.ACTION_DIAL);
//                        callIntent.data = Uri.parse("tel: " + selectedPartner?.contact!![0].number);
//                        startActivity(callIntent);
//                    }
//                }
//                textView143.text = it.slotTime
//                selectedTimeSlot = it.slotTime
//                imageView36.gone()
//                textView139.text = it.slotDate
//                dateString = it.slotDate
//                imageView35.gone()
//                imageView34.gone()
//                if (viewModel.observableIsCheckoutDone.value == null || viewModel.observableIsCheckoutDone.value == false) {
////                    slider_checkout.isLocked = false
//                    slider_checkout.visibility = View.VISIBLE
//                } else {
////                    slider_checkout.isLocked = true
//                    slider_checkout.visibility = View.GONE
//                }
//
////            stateChangeSlot()
//                textView136.text = getString(R.string.partner_address)
//                textView142.text = getString(R.string.slot_of_visit)
//                textView138.text = getString(R.string.date_of_visit)
//
            })



            viewModel.getApplication(mJobProfileId, mType, mTitle)
        })
        viewModel.getPartnerSchoolDetails(mType, mJobProfileId);


    }

    private fun initMappedUser(it: GFMappedUser?) {
        textView144.text = it?.name
        textView145.text = it?.number.toString()
        textView146.text = it?.city
    }

    fun stateChangeSlot() {
        doc_details.gone()
        helpIconIV.gone()
        tv_why_we_need_docs_scheduler.gone()
        driving_license_title.visible()
        tv_all_set.visible()
        tv_change_slot.visible()
    }

    private fun initClicks() {

//        tv_change_slot.setOnClickListener {
//            changeSlot()
//
//        }
//        tv_change_slot.paintFlags = tv_change_slot.paintFlags or Paint.UNDERLINE_TEXT_FLAG;


        slider_checkout.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {

                    navigate(
                        R.id.fragment_schedule_test,
                        bundleOf(
                            StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
                            StringConstants.TITLE.value to mTitle,
                            StringConstants.TYPE.value to mType
                        )
                    )
                }
            }

//        view_date_picker.setOnClickListener {
//            val gigforceDatePickerDialog = GigforceDatePickerDialog()
//            gigforceDatePickerDialog
//            gigforceDatePickerDialog.setCallbacks(this)
//            gigforceDatePickerDialog.show(
//                parentFragmentManager,
//                GigforceDatePickerDialog::class.java.name
//            )
//        }
//        view7.setOnClickListener {
//            val newInstance = SelectPartnerSchoolBottomSheet.newInstance(
//                bundleOf(
//                    StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
//                    StringConstants.TYPE.value to mType
//                )
//            )
//            newInstance.setCallbacks(this)
//            newInstance.show(parentFragmentManager, SelectPartnerSchoolBottomSheet.javaClass.name)
//        }
        imageView11.setOnClickListener {
            popBackState()
        }
//        view_select_time_slots.setOnClickListener { view ->
//            val newInstance = TimeSlotsDialog.newInstance()
//            newInstance.arguments = bundleOf(
//                StringConstants.TIME_SLOTS.value to viewModel.observablePartnerSchool.value?.timeSlots
//            )
//            newInstance
//            newInstance.setCallbacks(this)
//            newInstance.show(parentFragmentManager, TimeSlotsDialog::class.java.name)
//        }

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
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }

        arguments?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
        outState.putString(StringConstants.TYPE.value, mType)
        outState.putString(StringConstants.TITLE.value, mTitle)


    }

//    override fun setPartnerAddress(address: PartnerSchoolDetails) {
//        this.partnerAddress = address;
//        textView137.text =
//            Html.fromHtml((if (address?.name == null) "" else address?.name + "<br>") +
//                    (if (address?.line1 == null) "" else address?.line1 + "<br>") +
//                    (if (address?.line2 == null) "" else address?.line2 + "<br>") +
//                    (if (address?.line3 == null) "" else address?.line3 + "<br>") +
//                    if (address?.contact.isNullOrEmpty()) "" else
//                        address?.contact?.map { "<b><font color=\'#000000\'>" + it.name + "</font></b><br>" }
//                            ?.reduce { a, o -> a + o }
//            )
//        if (!partnerAddress?.lat.isNullOrEmpty()) {
//            iv_location.visible()
//            iv_location.setOnClickListener {
//                val uri =
//                    "http://maps.google.com/maps?saddr=" + "&daddr=" + address?.lat + "," + address?.lon
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//                startActivity(intent)
//            }
//        } else {
//            iv_location.gone()
//        }
//
//        iv_contact.setOnClickListener {
//            if (!address.contact.isNullOrEmpty()) {
//                val callIntent = Intent(Intent.ACTION_DIAL);
//                callIntent.data = Uri.parse("tel: " + address.contact[0].number);
//                startActivity(callIntent);
//            }
//        }
////        imageView34.gone()
//        iv_contact.visible()
//
////        checkIfCompleteProcessComplete()
//        textView136.text = getString(R.string.partner_address)
//
//        imageView34.gone()
//
//    }

//    override fun setSelectedTimeSlot(time: String) {
//        this.selectedTimeSlot = time
//        textView143.text = time
//        imageView36.gone()
////        checkIfCompleteProcessComplete()
////        stateChangeSlot()
//        textView142.text = getString(R.string.slot_of_visit)
//
//    }

//    override fun moveToNextStep() {
//        navigate(R.id.fragment_schedule_test)
//    }
//
//    override fun submissionSuccess() {
//
//        popBackState()
////        slider_checkout.visible()
////        slider_checkout.isLocked = false
//    }

//    override fun changeSlot() {
//        partnerAddress = null
//        dateString = ""
//        selectedTimeSlot = ""
//        val newInstance = SelectPartnerSchoolBottomSheet.newInstance(
//            bundleOf(
//                StringConstants.JOB_PROFILE_ID.value to mJobProfileId
//            )
//        )
//        newInstance.setCallbacks(this)
//        newInstance.show(parentFragmentManager, SelectPartnerSchoolBottomSheet.javaClass.name)
//    }

//    override fun selectedDate(date: String) {
//
//
//        this.dateString = date;
//        textView139.text = dateString
//        imageView35.gone()
////        checkIfCompleteProcessComplete()
//        textView138.text = getString(R.string.date_of_visit)
//
//    }

//    private fun checkIfCompleteProcessComplete() {
//        slider_checkout.isLocked =
//            !(!dateString.isNullOrEmpty() && partnerAddress != null && !selectedTimeSlot.isNullOrEmpty())
//        if (!slider_checkout.isLocked) {
//            val confirmationDialogDrivingTest = ConfirmationDialogDrivingTest()
//            confirmationDialogDrivingTest.setCallbacks(this@DocsSubSchedulerFragment)
//            confirmationDialogDrivingTest.arguments = bundleOf(
//                StringConstants.SELECTED_PARTNER.value to partnerAddress,
//                StringConstants.SELECTED_TIME_SLOT.value to selectedTimeSlot,
//                StringConstants.SELECTED_DATE.value to dateString,
//                StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
//                StringConstants.TITLE.value to mTitle,
//                StringConstants.TYPE.value to mType
//            )
//            confirmationDialogDrivingTest.show(
//                parentFragmentManager,
//                ConfirmationDialogDrivingTest::class.java.name
//            )
//        } else {
//            when {
//                partnerAddress == null -> {
//                    view7.performClick()
//                }
//                dateString.isNullOrEmpty() -> {
//                    view_date_picker.performClick()
//                }
//                selectedTimeSlot.isNullOrEmpty() -> {
//                    view_select_time_slots.performClick()
//                }
//            }
//
//        }
//    }


}