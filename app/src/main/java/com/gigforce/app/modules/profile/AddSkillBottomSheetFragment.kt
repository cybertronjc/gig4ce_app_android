package com.gigforce.app.modules.profile

import android.os.Bundle
import android.os.DropBoxManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Skill
import com.gigforce.app.utils.DropdownAdapter
import kotlinx.android.synthetic.main.add_skill_bottom_sheet.*

class AddSkillBottomSheetFragment : ProfileBaseBottomSheetFragment() {
    companion object {
        fun newInstance() = AddSkillBottomSheetFragment()
    }

    var skills: ArrayList<String> = ArrayList()
    var selectedSkill: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED Profile Education Expanded VIEW")
        inflateView(R.layout.add_skill_bottom_sheet, inflater, container)

        skills.addAll(listOf(
            getString(R.string.driving), getString(R.string.cooking), getString(R.string.shopkeeping), getString(
                            R.string.managing_catalog),
            getString(R.string.cashier), getString(R.string.tele_calling), getString(R.string.waiter), getString(
                            R.string.bartener), getString(R.string.barback),
            getString(R.string.fleet_management), getString(R.string.assembly_dismatling), getString(
                            R.string.e_commerce_delivery),
            getString(R.string.admin_assistant), getString(R.string.store_manager), getString(R.string.in_store_promoter),
            getString(R.string.record_keeper), getString(R.string.barista), getString(R.string.house_keeping), getString(
                            R.string.reception), getString(R.string.artist)))

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        val skillAdapter = DropdownAdapter(this.requireContext(), skills)
        val skillSpinner = add_skill_skill_name
        skillSpinner.setAdapter(skillAdapter)

        add_more_button.setOnClickListener {
            if (validateSkill()) {
                addNewSkill()
                add_skill_skill_name.setText("")
            }
        }

        add_skill_cancel_button.setOnClickListener {
            this.dismiss()
        }

        add_skill_save_button.setOnClickListener {
            if (validateSkill()) {
                addNewSkill()
                this.dismiss()
            }
        }

    }

    private fun addNewSkill() {
        hideError(form_error, add_skill_skill_name)
        profileViewModel.setProfileSkill(
            Skill(
                add_skill_skill_name.text.toString()
            )
        )
    }

    private fun validateSkill(): Boolean {
        Log.d("AddSkill", "validating skill " + add_skill_skill_name.text.toString())
        if (validation!!.isValidSkill(add_skill_skill_name.text.toString()))
            return true
        else {
            add_skill_skill_name.setHintTextColor(resources.getColor(R.color.colorError))
            showError(form_error, add_skill_skill_name)
            return false
        }
    }
}