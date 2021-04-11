package com.gigforce.app.modules.gigPage2.bottomsheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage2.models.ContactPerson
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.Lce
import com.gigforce.modules.feature_chat.core.ChatConstants
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_gig_contact_person_details.*
import java.util.*


class GigContactPersonBottomSheet : BottomSheetDialogFragment() {

    private lateinit var contactPersonDetail: ContactPerson
    private val profileViewModel: ProfileViewModel by viewModels()

    private val firebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_contact_person_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initView()
        initViewModel()
        setDataOnView()
    }


    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {

        arguments?.let {
            contactPersonDetail = it.getParcelable(INTENT_GIG_CONTACT_PERSON_DETAILS) ?: return@let
        }

        savedInstanceState?.let {
            contactPersonDetail = it.getParcelable(INTENT_GIG_CONTACT_PERSON_DETAILS) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(INTENT_GIG_CONTACT_PERSON_DETAILS, contactPersonDetail)
    }

    private fun initView() {

        call_card_view.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", contactPersonDetail.contactNumber.toString(), null))
            startActivity(intent)
        }

        message_card_view.setOnClickListener {
            //Start One to one Chat

            if (contactPersonDetail.uid != null) {
                startChatScreen(contactPersonDetail.uid!!)
            } else {
                if (contactPersonDetail.contactNumber != null)
                    profileViewModel.getProfileFromMobileNo(contactPersonDetail.contactNumber!!)
            }
        }

        whatsapp_card_view.setOnClickListener {

            val phoneNumber = if (contactPersonDetail.contactNumber!!.startsWith("+91")) {
                contactPersonDetail.contactNumber!!
            } else {
                "+91" + contactPersonDetail.contactNumber!!
            }

            val uri = Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=")
            val sendIntent = Intent(Intent.ACTION_VIEW, uri)
            requireContext().startActivity(sendIntent)
        }

        ic_cross.setOnClickListener {
            dismiss()
        }

        ic_ellipses.setOnClickListener {

        }
    }

    private fun setDataOnView() {

        user_name_tv.text = contactPersonDetail.name
        designation_tv.text = contactPersonDetail.designation

        rating_tv.gone()
        rating_star_iv.gone()

        //todo show picture here
//        if (contactPersonDetail.profilePicture != null) {
//
//            Glide.with(requireContext())
//                    .load(contactPersonDetail.profilePicture)
//                    .circleCrop()
//                    .into(user_image_iv)
//        } else {
//
//            Glide.with(requireContext())
//                    .load(R.drawable.avatar)
//                    .circleCrop()
//                    .into(user_image_iv)
//        }

        if (!contactPersonDetail.contactNumber.isNullOrBlank()) {
            call_card_view.visible()
            call_tv.visible()
        } else {
            call_card_view.gone()
            call_tv.gone()
        }

        if (!contactPersonDetail.uid.isNullOrBlank()) {
            message_card_view.visible()
            message_tv.visible()
        } else {
            message_card_view.gone()
            message_tv.gone()
        }

        if (!contactPersonDetail.contactNumber.isNullOrBlank()) {
            whatsapp_card_view.visible()
            whatsApp_tv.visible()
        } else {
            whatsapp_card_view.gone()
            whatsApp_tv.gone()
        }
    }

    private fun initViewModel() {
        profileViewModel
                .profile
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lce.Loading -> {
                            message_card_view.invisible()
                            getting_profile_details_pb.visible()
                        }
                        is Lce.Content -> {
                            getting_profile_details_pb.gone()
                            message_card_view.visible()

                            if (it.content != null) {
                                startChatScreen(it.content.id!!)
                            } else {
                                MaterialAlertDialogBuilder(requireContext())
                                        .setTitle("Alert")
                                        .setMessage("Looks like user is not on gig force App")
                                        .setPositiveButton("Okay") { _, _ -> }
                                        .show()
                            }
                        }
                        is Lce.Error -> {
                            getting_profile_details_pb.gone()
                            message_card_view.visible()

                            MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Alert")
                                    .setMessage("Unable to fetch user details, ${it.error}")
                                    .setPositiveButton("Okay") { _, _ -> }
                                    .show()
                        }
                    }
                })
    }

    private fun startChatScreen(id: String) {
        findNavController().navigate(R.id.chatPageFragment, bundleOf(
                ChatPageFragment.INTENT_EXTRA_CHAT_TYPE to ChatConstants.CHAT_TYPE_USER,
                ChatPageFragment.INTENT_EXTRA_CHAT_HEADER_ID to "",
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_ID to id,
                ChatPageFragment.INTENT_EXTRA_OTHER_USER_NAME to contactPersonDetail.name
        ))
    }

    companion object {
        const val INTENT_GIG_CONTACT_PERSON_DETAILS = "gig_contact_person"
    }
}