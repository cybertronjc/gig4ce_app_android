package com.gigforce.app.modules.profile

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.Builder
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Achievement
import kotlinx.android.synthetic.main.add_achievement_bottom_sheet.*


class AddAchievementBottomSheetFragment: ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = AddAchievementBottomSheetFragment()
    }

    var updates: ArrayList<Achievement> = ArrayList()

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

        cancel_button.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        save_button.setOnClickListener{
            if (validateAchievement()) {
                addNewAchievement()

                profileViewModel!!.setProfileAchievement(updates)
                Toast.makeText(this.context, "Updated Achievement Section", Toast.LENGTH_LONG).show()
                this.findNavController().navigate(R.id.educationExpandedFragment)
            }
        }
    }

    private fun addNewAchievement() {
        hideError(form_error, title, authority, year)
        updates.add(
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