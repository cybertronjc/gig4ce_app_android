package com.gigforce.app.modules.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Achievement
import kotlinx.android.synthetic.main.add_achievement_bottom_sheet.*


class AddAchievementBottomSheetFragment : ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = AddAchievementBottomSheetFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        inflateView(R.layout.add_achievement_bottom_sheet, inflater, container)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {

        year.setOnClickListener {
            showNumberPicker(requireContext(), year)
        }
        add_more_button.setOnClickListener{
            if (validateAchievement()) {
                addNewAchievement()
                title.setText("")
                authority.setText("")
                location.setSelection(0)
                year.setText("")
            }
        }

        cancel_button.setOnClickListener {
            this.dismiss()
        }

        save_button.setOnClickListener {
            if (validateAchievement()) {
                addNewAchievement()

                Toast.makeText(this.context, getString(R.string.updated_achievement_sec), Toast.LENGTH_LONG).show()
                this.dismiss()
            }
        }
    }

    private fun addNewAchievement() {
        hideError(form_error, title, authority, year)
        title.requestFocus()
        profileViewModel.setProfileAchievement(
            Achievement(
                title = title.text.toString(),
                issuingAuthority = authority.text.toString(),
                location = location.text.toString(),
                year = year.text.toString()
            )
        )
    }

    private fun validateAchievement(): Boolean {
        if (validation!!.isValidAchievement(
                title,
                authority,
                year
            )) {
            return true
        }
        else {
            showError(form_error, title, authority, year)
            return false
        }
    }
}