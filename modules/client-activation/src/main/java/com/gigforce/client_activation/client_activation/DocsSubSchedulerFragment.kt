package com.gigforce.client_activation.client_activation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.models.GFMappedUser
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.ncorti.slidetoact.SlideToActView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_docs_sub_scheduler.*
import javax.inject.Inject

@AndroidEntryPoint
class DocsSubSchedulerFragment : Fragment() {
    @Inject
    lateinit var navigation: INavigation
    private val viewModel: DocSubSchedulerViewModel by viewModels()

    private var dateString: String? = null

    //    private var partnerAddress: PartnerSchoolDetails? = null
    private lateinit var mJobProfileId: String
    private lateinit var mTitle: String
    private lateinit var mType: String
    private lateinit var adapterBulletStrings: AdapterBulletStrings

    //    private var selectedTimeSlot: String? = null
    private var isCheckOutDone: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_docs_sub_scheduler, container, false)
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
            Log.e("data", it?.stepId.toString())
            it?.answers?.forEach {
                Log.e("data", it.type!! + it.options?.size.toString())
                if (it.type == "cities") {
                    it.answer?.let {
                        viewModel.getMappedUser(it)
                    }
                }
            }
        })

        viewModel.observableMappedUser.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (!it.number.contains("+91")) {
                    it.numberWithoutnineone = it.number
                    it.number = "+91" + it.number
                } else {
                    it.numberWithoutnineone = it.number
                }
                viewModel.checkIfTeamLeadersProfileExists(it.number)
                initMappedUser(it)

            }


        })
        viewModel.observableProfile.observe(viewLifecycleOwner, Observer {
            contact_card.visible()
            textView147.setOnClickListener { view ->
                val bundle = Bundle()
//                bundle.putString(AppConstants.IMAGE_URL, it.profileAvatarName)
//                bundle.putString(AppConstants.CONTACT_NAME, it.name)

                bundle.putString(
                    StringConstants.INTENT_EXTRA_OTHER_USER_IMAGE.value,
                    it.profileAvatarName
                )
                bundle.putString(StringConstants.INTENT_EXTRA_OTHER_USER_NAME.value, it.name)

                bundle.putString(StringConstants.INTENT_EXTRA_CHAT_HEADER_ID.value, "")
                bundle.putString(StringConstants.INTENT_EXTRA_OTHER_USER_ID.value, it.id)

                bundle.putString(StringConstants.MOBILE_NUMBER.value, it.loginMobile)
                bundle.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, true)
                navigation.navigateTo("chats/chatScreenFragment", bundle)
            }
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
            alert_message.text = it?.alertMessage ?: ""
            doc_title.text = Html.fromHtml(it?.documentTitle ?: "")
            header_title.text = it?.headerTitle ?: ""
            doc_sub_title.text = Html.fromHtml(it?.documentSubTitle ?: "")
            it?.documentInfo?.let {
                adapterBulletStrings.addData(it)
            }


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
        viewModel.getPartnerSchoolDetails(mType, mJobProfileId)


    }

    private fun setupBulletPontsRv() {
        adapterBulletStrings = AdapterBulletStrings()

        rv_bullet_points.adapter = adapterBulletStrings
        rv_bullet_points.layoutManager =
            LinearLayoutManager(requireContext())


    }

    private fun initMappedUser(it: GFMappedUser?) {

        textView144.text = it?.name
        textView145.text = it?.number.toString()
        textView146.text = it?.city

    }

    fun stateChangeSlot() {
        doc_title.gone()
        helpIconIV.gone()
        tv_why_we_need_docs_scheduler.gone()
        driving_license_title.visible()
        tv_all_set.visible()
        tv_change_slot.visible()
    }

    private fun enableCheckoutButton() {
        slider_checkout.isEnabled = true
    }

    private fun disableCheckoutButton() {
        slider_checkout.isEnabled = false
    }

    private fun initClicks() {

        cb_activate.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enableCheckoutButton()
            } else disableCheckoutButton()
        }
        call.setOnClickListener {
            viewModel.gfmappedUserObj?.number?.let {
                val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", it, null))
                startActivity(intent)
            }
        }

        slider_checkout.setOnClickListener {
                    viewModel.gfmappedUserObj?.numberWithoutnineone?.let {
                        navigation.navigateTo(
                            "client_activation/schedule_test",
                            bundleOf(
                                StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
                                StringConstants.TITLE.value to mTitle,
                                StringConstants.TYPE.value to mType,
                                StringConstants.MOBILE_NUMBER.value to it,
                                StringConstants.MOBILE_NUMBERS.value to viewModel.gfmappedUserObj?.numbers
                            )
                        )
                    }

          }

//        tv_change_slot.setOnClickListener {
//            changeSlot()
//
//        }
//        tv_change_slot.paintFlags = tv_change_slot.paintFlags or Paint.UNDERLINE_TEXT_FLAG;


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
            navigation.popBackStack()
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
        setupBulletPontsRv()
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