package com.gigforce.lead_management.gigeronboarding

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.lead_management.R

class SelectTeamLeaderFragment : Fragment() {

    companion object {
        fun newInstance() = SelectTeamLeaderFragment()
    }

    private lateinit var viewModel: SelectTeamLeaderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.select_team_leader_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SelectTeamLeaderViewModel::class.java)
    }

}