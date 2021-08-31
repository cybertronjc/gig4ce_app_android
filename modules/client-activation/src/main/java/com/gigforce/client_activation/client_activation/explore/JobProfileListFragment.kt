package com.gigforce.client_activation.client_activation.explore

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AbsListView
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.adapters.JobProfileListAdapter
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileDVM
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileRequestDataModel
import com.gigforce.client_activation.client_activation.models.JpExplore
import com.gigforce.client_activation.databinding.JobProfileListFragmentBinding
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.components.atoms.ChipGroupComponent
import com.gigforce.common_ui.components.atoms.models.ChipGroupModel
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.listeners.AppBarClicks
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.analytics.ClientActivationEvents
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class JobProfileListFragment : Fragment(), IOnBackPressedOverride, OnJobSelectedListener {

    companion object {
        fun newInstance() = JobProfileListFragment()
    }


    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var buildConfig: IBuildConfig
    @Inject
    lateinit var eventTracker: IEventTracker

    private val viewModel: JobProfileListViewModel by viewModels()
    private lateinit var viewBinding: JobProfileListFragmentBinding
    private val jobProfileListAdapter: JobProfileListAdapter by lazy {
        JobProfileListAdapter(requireContext(), this).apply {
            setOnJobSelectedListener(this@JobProfileListFragment)
        }
    }
    private val uid = FirebaseAuth.getInstance().uid
    private var win: Window? = null
    var isLoading = false
    var currentPage = 1
    val filterChips = arrayListOf<ChipGroupModel>()
    var jobProfilesList = ArrayList<JobProfileDVM>()
    private var jobProfileRequestModelCurrent  = JobProfileRequestDataModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = JobProfileListFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changeStatusBarColor()
        setViews()
        listeners()
        observer()
    }

    private fun setViews() {
        Log.d("Here", "Came here")
        jobProfilesList.clear()

        jobProfileRequestModelCurrent.gigerId = uid
        jobProfileRequestModelCurrent.sortBy = "priority"
        jobProfileRequestModelCurrent.sortOrder = 1
        jobProfileRequestModelCurrent.pageNo = 1
        jobProfileRequestModelCurrent.pageSize = 15
        viewModel.getAllJobProfiles(jobProfileRequestModelCurrent)

        //add filter chips
        filterChips.clear()
        filterChips.add(ChipGroupModel("All", -1, 1))
        filterChips.add(ChipGroupModel("Submitted", -1, 2))
        filterChips.add(ChipGroupModel("Pending", -1, 3))
        filterChips.add(ChipGroupModel("Not Applied", -1, 4))
        filterChips.add(ChipGroupModel("Rejected", -1, 5))

        viewBinding.filtersChipGroup.removeAllViews()
        viewBinding.filtersChipGroup.addChips(filterChips, isSingleSelection = true, true)
        initializeRecyclerView()
    }
    private fun initializeRecyclerView() = viewBinding.apply{
        val layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        exploreRv.layoutManager = layoutManager
        exploreRv.adapter = jobProfileListAdapter

        exploreRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isLoading = true
                }

            }
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //Log.d("Scrolled", "onScrolled $dx ,: $dy")

                val currentItemsLatest = layoutManager.childCount
                val totalItemsLatest = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                //&& (totalItemsLatest <= jobProfileListAdapter.itemCount)
                Log.d("Scrolled", " isLoading: ${isLoading} , currentItemsLatest : $currentItemsLatest, lastVisibleItemPosition: $lastVisibleItemPosition, totalItemsLatest: $totalItemsLatest ")
                if (isLoading && (currentItemsLatest + lastVisibleItemPosition == totalItemsLatest) && (totalItemsLatest <= jobProfilesList.size)  ) {
//                    if ((currentPage < totalPages) && isLoading ){
                    //load next page
                    currentPage += 1
                    isLoading = false
                    progressBarBottom.visibility = View.VISIBLE
                    jobProfileRequestModelCurrent.pageNo = currentPage
                    viewModel.getAllJobProfiles(jobProfileRequestModelCurrent)
                }
            }
        })
    }

    private fun observer() {

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                Lce.Loading -> {
//                    UtilMethods.showLoading(requireContext())
                    viewBinding.exploreProgressBar.visible()

                }
                is Lce.Content -> {
//                    UtilMethods.hideLoading()
                    viewBinding.exploreProgressBar.gone()
                    showClientActivations(state.content as ArrayList<JobProfileDVM>)
                }
            }
        })
    }

    private fun showClientActivations(jobProfiles: ArrayList<JobProfileDVM>) = viewBinding.apply{
        jobProfilesList.addAll(jobProfiles)
        swipeRefresh.isRefreshing = false

        if (jobProfilesList.isNullOrEmpty()) {
            exploreRv.gone()
            exploreError.visible()
            noGigs.visible()
            exploreProgressBar.gone()
        } else {
            exploreRv.visible()
            exploreError.gone()
            noGigs.gone()
            exploreProgressBar.gone()
            progressBarBottom.gone()

            Log.d("jobProfiles", jobProfiles.toString())


            if (currentPage == 1){
                Log.d("pag", "zero $currentPage, list : ${jobProfiles.size}")
                jobProfileListAdapter.submitList(jobProfilesList)
                jobProfileListAdapter.notifyDataSetChanged()
            }else {
                Log.d("pag", "nonzero $currentPage, list : ${jobProfiles.size}" )
                val itemCount = jobProfileListAdapter.itemCount
                jobProfileListAdapter.submitList(jobProfilesList)
                //jobProfileListAdapter.notifyDataSetChanged()
                jobProfileListAdapter.notifyItemChanged(itemCount + 1)

            }

        }

    }


    private fun listeners() = viewBinding.apply {

        swipeRefresh.setOnRefreshListener {
            jobProfilesList.clear()
            currentPage = 1
            jobProfileRequestModelCurrent.pageNo = currentPage
            viewModel.getAllJobProfiles(jobProfileRequestModelCurrent)
        }

        appBar.setOnSearchClickListener(object : AppBarClicks.OnSearchClickListener{
            override fun onSearchClick(v: View) {
                //jobProfileListAdapter.filter.filter("")
                jobProfileRequestModelCurrent.text = ""
                jobProfilesList.clear()
                currentPage = 1
                jobProfileRequestModelCurrent.pageNo = currentPage
                viewModel.getAllJobProfiles(jobProfileRequestModelCurrent)
            }
        })
        lifecycleScope.launch {

            appBar.search_item.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { searchString ->
                    Log.d("Search ", "Searhcingg...$searchString")
                    jobProfileRequestModelCurrent.text = searchString
                    currentPage = 1
                    jobProfileRequestModelCurrent.pageNo = currentPage
                    jobProfilesList.clear()
                    viewModel.getAllJobProfiles(jobProfileRequestModelCurrent)
                }
        }

        appBar.setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })

        filtersChipGroup.setOnCheckedChangeListener(object: ChipGroupComponent.OnCustomCheckedChangeListener {
            override fun onCheckedChangeListener(model: ChipGroupModel) {
                //get the chip text
                val filterText = model.text
                Log.d("chiptext", filterText)
                currentPage = 1
                jobProfileRequestModelCurrent.pageNo = currentPage
                if (filterText.equals("All")){
                    jobProfileRequestModelCurrent.type = ""
                }else{
                    jobProfileRequestModelCurrent.type = filterText
                }

                jobProfilesList.clear()
                viewModel.getAllJobProfiles(jobProfileRequestModelCurrent)
            }

        })
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

    fun takeAction(action: String, id: String, title: String){
        Log.d("action", action)
        Log.d("id", id)
        Log.d("jbTitle", title)
        when(action){
            //navigate to Application
            "Apply Now"  ->  navigation.navigateTo(
                "client_activation/applicationClientActivation", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to id,
                    StringConstants.JOB_PROFILE_TITLE.value to title

                ))
            //share gig for approved
            "Approved" -> shareGig(id)

            //rejected
            "Apply Again" -> navigation.navigateTo(
                "client_activation/applicationClientActivation", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to id,
                    StringConstants.JOB_PROFILE_TITLE.value to title

                ))
            //completed applicaiton
            "Complete Application" -> navigation.navigateTo(
                "client_activation/applicationClientActivation", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to id,
                    StringConstants.JOB_PROFILE_TITLE.value to title

                ))
            //Submitted applicaiton
            "View Application" -> navigation.navigateTo(
                "client_activation/applicationClientActivation", bundleOf(
                    StringConstants.JOB_PROFILE_ID.value to id,
                    StringConstants.JOB_PROFILE_TITLE.value to title

                ))
        }
    }

    private fun shareGig(id: String) {
//        Firebase.dynamicLinks.shortLinkAsync {
//            longLink =
//                Uri.parse(buildDeepLink(Uri.parse("http://www.gig4ce.com/?job_profile_id=$id&invite=${viewModel.getUID()}")).toString())
//        }.addOnSuccessListener { result ->
//            // Short link created
//            val shortLink = result.shortLink
//            shareToAnyApp(shortLink.toString())
//        }.addOnFailureListener {
//            // Error
//            // ...
//            showToast(it.message!!)
//        }
    }

    override fun onBackPressed(): Boolean {
        if (viewBinding.appBar.isSearchCurrentlyShown) {
            hideSoftKeyboard()
            viewBinding.appBar.hideSearchOption()
            jobProfileListAdapter.filter.filter("")
            return true
        } else {
            return false
        }
    }

    override fun onJobSelected(jpExplore: JobProfileDVM) {
        Log.d("id", jpExplore.id)
        val id = jpExplore?.id ?: ""
        val title = jpExplore?.title ?: ""
        Log.d("title", jpExplore.title + "id $id")

        eventTracker.pushEvent(
            TrackingEventArgs(
                eventName = jpExplore.title + "_" + ClientActivationEvents.EVENT_USER_CLICKED,
                props = mapOf(
                    "id" to id,
                    "title" to title,
                    "screen_source" to "Client Explore Job List"
                )
            )
        )
        eventTracker.pushEvent(
            TrackingEventArgs(
                eventName = ClientActivationEvents.EVENT_USER_CLICKED,
                props = mapOf(
                    "id" to id,
                    "title" to title,
                    "screen_source" to "Client Explore Job List"
                )
            )
        )
        navigation.navigateTo("client_activation",
            bundleOf(StringConstants.JOB_PROFILE_ID.value to jpExplore.id)
        )
    }

}