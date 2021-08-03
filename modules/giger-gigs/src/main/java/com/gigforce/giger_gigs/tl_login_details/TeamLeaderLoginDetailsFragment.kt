package com.gigforce.giger_gigs.tl_login_details

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.databinding.TeamLeaderLoginDetailsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TeamLeaderLoginDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = TeamLeaderLoginDetailsFragment()
    }

    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewModel: TeamLeaderLoginDetailsViewModel
    private lateinit var viewBinding: TeamLeaderLoginDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = TeamLeaderLoginDetailsFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(TeamLeaderLoginDetailsViewModel::class.java)
        initToolbar()
        initializeViews()
        observer()
        listeners()
    }

    private fun initToolbar() = viewBinding.apply {
        appBar.apply {
            setBackButtonListener(View.OnClickListener {
                activity?.onBackPressed()
            })
        }
    }

    private fun initializeViews() {

    }

    private fun listeners() = viewBinding.apply {
        addNew.setOnClickListener {
            navigation.navigateTo("gig/")
        }
    }

    private fun observer() {

    }


}