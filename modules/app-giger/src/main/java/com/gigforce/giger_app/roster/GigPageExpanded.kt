package com.gigforce.giger_app.roster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.giger_app.R
import kotlinx.android.synthetic.main.gig_page_expanded.*

class GigPageExpanded: RosterBaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.gig_page_expanded, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        back_button.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}