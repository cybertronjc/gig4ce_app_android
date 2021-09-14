package com.gigforce.ambassador.ambassador_enrollment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.gigforce.ambassador.R
import com.gigforce.core.datamodels.profile.ErrorWhileSettingUserAsAmbassador
import com.gigforce.core.datamodels.profile.SettingUserAsAmbassador
import com.gigforce.core.datamodels.profile.UserSetAsAmbassadorSuccessfully
import com.gigforce.core.extensions.capitalizeWords
import com.gigforce.core.extensions.inflate
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_apply_for_ambassador.*
import kotlinx.android.synthetic.main.fragment_apply_for_ambassador_main.*

interface AmbassadorEnrolledSuccessfullyDialogFragmentListeners {

    fun onStartingOnBoardingGigersClicked()

    fun onViewGigDetailsClicked()
}

class AmbassadorEnrolledDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "AmbassadorEnrolledSuccessfullyDialogFragment"

        private lateinit var guidelines: List<String>

        fun launch(
            fragmentManager: FragmentManager,
            ambassadorEnrolledSuccessfullyDialogFragmentListener: AmbassadorEnrolledSuccessfullyDialogFragmentListeners
        ) {
            val frag = AmbassadorEnrolledDialogFragment()
            frag.ambassadorEnrolledSuccessfullyDialogFragmentListener =
                ambassadorEnrolledSuccessfullyDialogFragmentListener
            frag.show(fragmentManager, TAG)
        }
    }

    private lateinit var ambassadorEnrolledSuccessfullyDialogFragmentListener: AmbassadorEnrolledSuccessfullyDialogFragmentListeners
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apply_for_ambassador, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        guidelines = resources.getStringArray(R.array.ambassador_guidelines_amb).toList()
        initView()
        initViewModel()

        //setting user as amb
        profileViewModel.setUserAsAmbassador()
    }

    override fun isCancelable(): Boolean {
        return false
    }

    private fun initViewModel() {
        profileViewModel.viewState
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    SettingUserAsAmbassador -> {
                        appliedSuccessfullyLayout.invisible()
                        progressBar.visible()
                    }
                    UserSetAsAmbassadorSuccessfully -> {
                        appliedSuccessfullyLayout.visible()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.ambassador_now_amb),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is ErrorWhileSettingUserAsAmbassador -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.alert_amb))
                            .setMessage(getString(R.string.unable_to_apply_for_ambassador_amb) + " " + it.error)
                            .setPositiveButton(getString(R.string.okay_amb).capitalizeWords()) { _, _ -> }
                            .show()
                    }
                }
            })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {

            setBackgroundDrawableResource(R.drawable.dialog_round_bg)

            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }


    private fun initView() {
        submitBtn.setOnClickListener {
            ambassadorEnrolledSuccessfullyDialogFragmentListener.onStartingOnBoardingGigersClicked()
            dismiss()
        }

        inflateUserGuidelines(guidelines)
    }

    private fun inflateUserGuidelines(guidelines: List<String>) = guidelines.forEach {

        if (it.contains(":")) {
            ambsd_guildliness_on_success_container.inflate(R.layout.gig_requirement_item, true)
            val gigItem: LinearLayout =
                ambsd_guildliness_on_success_container.getChildAt(
                    ambsd_guildliness_on_success_container.childCount - 1
                ) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            ambsd_guildliness_on_success_container.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                ambsd_guildliness_on_success_container.getChildAt(
                    ambsd_guildliness_on_success_container.childCount - 1
                ) as LinearLayout
            val gigTextTV: TextView = gigItem.findViewById(R.id.text)
            gigTextTV.text = fromHtml(it)
        }
    }

    fun fromHtml(html: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
    }


}