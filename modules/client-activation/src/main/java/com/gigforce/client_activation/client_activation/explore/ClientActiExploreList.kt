package com.gigforce.client_activation.client_activation.explore

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.adapters.ClientActiExploreAdapter
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.client_acti_explore_list_fragment.*
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class ClientActiExploreList : Fragment(), OnJobSelectedListener {

    companion object {
        fun newInstance() = ClientActiExploreList()
    }

    @Inject
    lateinit var navigation : INavigation
    private lateinit var viewModel: ClientActiExploreListViewModel
    private val clientActiExploreAdapter: ClientActiExploreAdapter by lazy {
        ClientActiExploreAdapter(requireContext()).apply {
            setOnJobSelectedListener(this@ClientActiExploreList)
        }
    }
    private var win: Window? = null

    var new_selected = false
    var approved_selected = false
    var pending_selected = false
    var applied_selected = false
    var rejected_selected = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.client_acti_explore_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ClientActiExploreListViewModel::class.java)
        // TODO: Use the ViewModel
        changeStatusBarColor()
        listener()
        observer()
        initClientActivation()
    }

    private fun changeStatusBarColor() {
        win = activity?.window
                // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.status_bar_pink))
    }

    private fun initClientActivation() {
        viewModel.observableJobProfile.observe(viewLifecycleOwner, Observer { jobProfiles ->
            run {
                jobProfiles?.let {
                    showClientActivations(jobProfiles)
                }

            }
        })

        viewModel.getJobProfiles()
    }

    private fun showClientActivations(jobProfiles: ArrayList<JobProfile>) {
        if (jobProfiles.isNullOrEmpty()) {
            explore_rv.gone()
            explore_error.visible()
            explore_progress_bar.gone()
        } else {
            explore_rv.visible()
            explore_error.gone()
            explore_progress_bar.gone()
            //val itemWidth = ((width / 3) * 2).toInt()
            // model will change when integrated with DB

            explore_rv.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
            explore_rv.adapter = clientActiExploreAdapter
            clientActiExploreAdapter.setData(jobProfiles)

        }
    }



    private fun observer() {

    }

    private fun listener() {

        iv_back_explore_fragment.setOnClickListener {
            navigation.popBackStack()
        }

        iv_search_explore.setOnClickListener {
            if (search_item.isVisible){
                search_item.gone()
                explore_text.visible()
                iv_search_explore.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_search_24))
                search_item.setText("")
            }
            else{
                search_item.visible()
                explore_text.gone()
                iv_search_explore.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_close_24))
            }
        }

//        search_item.doOnTextChanged { text, start, before, count ->
//            if (clientActiExploreAdapter.itemCount != 0){
//                clientActiExploreAdapter.filter.filter(text)
//            }
//        }
        search_item.doAfterTextChanged {
            if (clientActiExploreAdapter.itemCount != 0){
                Log.d("text", it.toString() + " count: "+ clientActiExploreAdapter.itemCount)
                clientActiExploreAdapter.filter.filter(it.toString())
            }
        }


        new_tv.setOnClickListener {
            if (new_selected){
                resetSelected(new_tv)
                new_selected = false
            }
            else{
                setSelected(new_tv)
                new_selected = true
                clientActiExploreAdapter.filter.filter("New")
            }

        }

        approved_tv.setOnClickListener {
            if (approved_selected){
                resetSelected(approved_tv)
                approved_selected = false
            }
            else{
                setSelected(approved_tv)
                approved_selected = true
                clientActiExploreAdapter.filter.filter("Approved")
            }

        }

        pending_tv.setOnClickListener {
            if (pending_selected){
                resetSelected(pending_tv)
                pending_selected = false
            }
            else{
                setSelected(pending_tv)
                pending_selected = true
                clientActiExploreAdapter.filter.filter("Pending")
            }

        }

        applied_tv.setOnClickListener {
            if (applied_selected){
                resetSelected(applied_tv)
                applied_selected = false
            }
            else{
                setSelected(applied_tv)
                applied_selected = true
                clientActiExploreAdapter.filter.filter("Applied")
            }

        }

        rejected_tv.setOnClickListener {
            if (rejected_selected){
                resetSelected(rejected_tv)
                rejected_selected = false
            }
            else{
                setSelected(rejected_tv)
                rejected_selected = true
                clientActiExploreAdapter.filter.filter("Rejected")
            }

        }

    }

    private fun resetSelected(option: TextView) {
        context?.let {
            option.background = resources.getDrawable(R.drawable.bg_explore_filter)

        }
    }

    private fun setSelected(option: TextView) {
        context?.let {
            option.background = resources.getDrawable(R.drawable.explore_filter_selected)
        }
    }

    override fun onJobSelected(jobProfile: JobProfile) {
        navigation.navigateTo("client_activation",
            bundleOf(StringConstants.JOB_PROFILE_ID.value to jobProfile.id)
            )

        showToast("Clicked: "+ jobProfile.title)
    }

}