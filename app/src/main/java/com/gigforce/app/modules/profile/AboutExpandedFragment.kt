package com.gigforce.app.modules.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import kotlinx.android.synthetic.main.card_row.view.*
import kotlinx.android.synthetic.main.contact_edit_warning_dialog.*
import kotlinx.android.synthetic.main.delete_confirmation_dialog.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.nav_bar
import kotlinx.android.synthetic.main.profile_card_background.view.*

class AboutExpandedFragment: ProfileBaseFragment() {
    companion object {
        fun newInstance() = AboutExpandedFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requireActivity().onBackPressedDispatcher.addCallback(this) {
//            findNavController().navigate(R.id.profileFragment)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflateView(R.layout.fragment_profile_about_expanded, inflater, container)
        view?.nav_bar?.about_me_active = true
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()
    }

    private fun initialize() {
        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            bio_card.isBottomRemoved = profile.aboutMe.isNotEmpty()
            bio_card.hasContentTitles = false
            bio_card.nextDestination = R.id.addAboutMeBottomSheet
            bio_card.cardTitle = "Bio"
            bio_card.cardContent = if (profile.aboutMe != "") profile.aboutMe
                                    else this.requireContext().getString(R.string.empty_about_me_text)
            bio_card.cardBottom = if (profile.aboutMe != "") ""
                                    else "Add bio"

            var languageString = ""
            profile.languages?.let {
                val languages = it.sortedByDescending { language -> language.speakingSkill }
                for (lang in languages) {
                    languageString += lang.name + "\n"
                    languageString += "Speaking " + getLanguageLevel(lang.speakingSkill.toInt()) + "\n"
                    languageString += "Writing " + getLanguageLevel(lang.writingSkill.toInt()) + "\n\n"
                }
            }
            language_card.nextDestination = R.id.editLanguageBottomSheet
            language_card.cardTitle = "Language"
            language_card.cardContent = languageString
            language_card.cardBottom = "Add languages"

            var contactString = ""
            profile.contact?.let {
                for (contact in it) {
                    contactString += "phone: " + contact.phone + "\n"
                    contactString += "email: " + contact.email + "\n\n"
                }
            }
            contact_card.hasContentTitles = false
            contact_card.cardTitle = "Contact"
            contact_card.cardContent = contactString
            contact_card.cardBottom = "Add contacts"

            if (contact_card.edit_button != null) {
                contact_card.edit_button.setOnClickListener {
                    showAddContactDialog()
                }
            }

            about_top_profile.userName = profile.name
            about_top_profile.imageName = profile.profileAvatarName
        })
    }

    private fun setListeners() {

        bio_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addAboutMeBottomSheet)
        }

        language_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addLanguageBottomSheetFragment)
        }

        contact_card.card_bottom.setOnClickListener {
            showAddContactDialog()
        }

    }

    private fun showAddContactDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.contact_edit_warning_dialog)

        dialog.cancel_button.setOnClickListener {
            dialog .dismiss()
        }

        dialog.submit_button.setOnClickListener {
            Toast.makeText(requireContext(), "CTA NOT IMPLEMENTED", Toast.LENGTH_SHORT).show()
            dialog .dismiss()
        }

        dialog.show()
    }

    private fun getLanguageLevel(level: Int): String {
        return when (level) {
            in 0..25 -> "beginner"
            in 26..75 -> "moderate"
            else -> "advanced"
        }
    }

}
