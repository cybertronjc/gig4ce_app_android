package com.gigforce.giger_gigs.tl_login_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.giger_gigs.R

class TeamLeaderLoginDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = TeamLeaderLoginDetailsFragment()
    }

    private lateinit var viewModel: TeamLeaderLoginDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.team_leader_login_details_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(TeamLeaderLoginDetailsViewModel::class.java)

    }

}