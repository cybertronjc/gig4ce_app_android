package com.gigforce.app.modules.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import com.gigforce.app.R
import kotlinx.android.synthetic.main.edit_cover_bottom_sheet.*
import kotlinx.android.synthetic.main.edit_headline_bs.*
import kotlinx.android.synthetic.main.edit_headline_bs.bio
import kotlinx.android.synthetic.main.edit_headline_bs.form_error

class EditHeadlineBottomSheet: ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = EditHeadlineBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        inflateView(R.layout.edit_headline_bs, inflater, container)

        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()
    }

    private fun initialize() {
        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profile ->
            bio.setText(profile.bio)
        })
    }

    private fun setListeners() {
        bio.addTextChangedListener (object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //TODO()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length >= 150) {
                    form_error.visibility = View.VISIBLE
                } else {
                    form_error.visibility = View.GONE
                }
            }
        })

        save.setOnClickListener {
            if (bio.text.toString().length <= 150) {
                profileViewModel.setProfileBio(bio.text.toString())
                this.dismiss()
            } else {
                Toast.makeText(requireContext(), getString(R.string.bio_limit), Toast.LENGTH_LONG).show()
            }
        }

        cancel.setOnClickListener {
            this.dismiss()
        }

        delete.setOnClickListener {
            profileViewModel.setProfileBio("")
            this.dismiss()
        }

    }
}