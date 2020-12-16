package com.gigforce.app.modules.ambassador_user_enrollment.ambassador_enrollment

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.invisible
import com.gigforce.app.core.visible
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.profile.models.ErrorWhileSettingUserAsAmbassador
import com.gigforce.app.modules.profile.models.SettingUserAsAmbassador
import com.gigforce.app.modules.profile.models.UserSetAsAmbassadorSuccessfully
import com.gigforce.app.modules.roster.inflate
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

        private val guidelines = listOf(
            "Be nice to your customers and colleagues",
            "Be punctual for Job",
            "Work more to earn more"
        )

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
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apply_for_ambassador, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()

        //setting user as amb
        profileViewModel.setUserAsAmbassador()
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
                    Toast.makeText(requireContext(), "You are an Ambassador now", Toast.LENGTH_LONG).show()
                }
                is ErrorWhileSettingUserAsAmbassador -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Alert")
                        .setMessage("Unable to apply for ambassador, ${it.error}")
                        .setPositiveButton("Okay") { _, _ -> }
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
                ambsd_guildliness_on_success_container.getChildAt(ambsd_guildliness_on_success_container.childCount - 1) as LinearLayout
            val gigTitleTV: TextView = gigItem.findViewById(R.id.title)
            val contentTV: TextView = gigItem.findViewById(R.id.content)

            val title = it.substringBefore(":").trim()
            val content = it.substringAfter(":").trim()

            gigTitleTV.text = fromHtml(title)
            contentTV.text = fromHtml(content)
        } else {
            ambsd_guildliness_on_success_container.inflate(R.layout.gig_details_item, true)
            val gigItem: LinearLayout =
                ambsd_guildliness_on_success_container.getChildAt(ambsd_guildliness_on_success_container.childCount - 1) as LinearLayout
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