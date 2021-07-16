package com.gigforce.client_activation.client_activation.explore

import android.app.Activity
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.adapters.ClientActiExploreAdapter
import com.gigforce.client_activation.client_activation.models.JpExplore
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.viewdatamodels.client_activation.JobProfile
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.analytics.ClientActivationEvents
import com.gigforce.common_ui.components.cells.SearchTextChangeListener
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.listeners.AppBarClicks
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.android.play.core.review.testing.FakeReviewManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.client_acti_explore_list_fragment.*
import kotlinx.android.synthetic.main.client_acti_explore_list_fragment.search_item
import java.lang.Exception
import java.util.ArrayList
import javax.inject.Inject

@AndroidEntryPoint
class ClientActiExploreList : Fragment(), IOnBackPressedOverride, OnJobSelectedListener {

    companion object {
        fun newInstance() = ClientActiExploreList()
    }

    @Inject
    lateinit var navigation : INavigation
    @Inject
    lateinit var eventTracker: IEventTracker
    private lateinit var viewModel: ClientActiExploreListViewModel
    private val clientActiExploreAdapter: ClientActiExploreAdapter by lazy {
        ClientActiExploreAdapter(requireContext(), this).apply {
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


        viewModel.observableJobProfile.observe(viewLifecycleOwner, Observer {
            run {
                it?.let {
                    showClientActivations(it)
                }
            }
        })
        viewModel.getJobProfiles()

    }

    private fun showClientActivations(jobProfiles: ArrayList<JpExplore>) {
        if (jobProfiles.isNullOrEmpty()) {
            explore_rv.gone()
            explore_error.visible()
            no_gigs.visible()
            explore_progress_bar.gone()
        } else {
            explore_rv.visible()
            explore_error.gone()
            no_gigs.gone()
            explore_progress_bar.gone()
            //val itemWidth = ((width / 3) * 2).toInt()
            // model will change when integrated with DB
            Log.d("jobProfiles", jobProfiles.toString())

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


//        tb_explore_fragment1.setOnSearchClickListener(object : AppBarClicks.OnSearchClickListener{
//            override fun onSearchClick(v: View) {
//                Log.d("Click", "searchFrag")
//                clientActiExploreAdapter.filter.filter("")
//            }
//
//        })

        search_item.doOnTextChanged { text, start, before, count ->
            clientActiExploreAdapter.filter.filter(text)
        }

        appBar.setOnSearchClickListener(object : AppBarClicks.OnSearchClickListener{
            override fun onSearchClick(v: View) {
                clientActiExploreAdapter.filter.filter("")
            }

        })
        appBar.setOnSearchTextChangeListener(object : SearchTextChangeListener {
            override fun onSearchTextChanged(text: String) {
                clientActiExploreAdapter.filter.filter(text)
            }

        })
        appBar.setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })

//        new_tv.setOnClickListener {
//            if (new_selected){
//                resetSelected(new_tv)
//                new_selected = false
//            }
//            else{
//                setSelected(new_tv)
//                new_selected = true
//                clientActiExploreAdapter.filter.filter("New")
//            }
//
//        }
//
//        approved_tv.setOnClickListener {
//            if (approved_selected){
//                resetSelected(approved_tv)
//                approved_selected = false
//            }
//            else{
//                setSelected(approved_tv)
//                approved_selected = true
//                clientActiExploreAdapter.filter.filter("Approved")
//            }
//
//        }
//
//        pending_tv.setOnClickListener {
//            if (pending_selected){
//                resetSelected(pending_tv)
//                pending_selected = false
//            }
//            else{
//                setSelected(pending_tv)
//                pending_selected = true
//                clientActiExploreAdapter.filter.filter("Pending")
//            }
//
//        }
//
//        applied_tv.setOnClickListener {
//            if (applied_selected){
//                resetSelected(applied_tv)
//                applied_selected = false
//            }
//            else{
//                setSelected(applied_tv)
//                applied_selected = true
//                clientActiExploreAdapter.filter.filter("Applied")
//            }
//
//        }
//
//        rejected_tv.setOnClickListener {
//            if (rejected_selected){
//                resetSelected(rejected_tv)
//                rejected_selected = false
//            }
//            else{
//                setSelected(rejected_tv)
//                rejected_selected = true
//                clientActiExploreAdapter.filter.filter("Rejected")
//            }
//
//        }

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

    override fun onJobSelected(jpExplore: JpExplore) {
        Log.d("id", jpExplore.id)

//                    Log.d("cardId", id)
//        navigate(
//                R.id.fragment_client_activation,
//                bundleOf(StringConstants.JOB_PROFILE_ID.value to id)
//        )
        val id = jpExplore?.id ?: ""
        val title = jpExplore?.jobProfileTitle ?: ""
        Log.d("title", jpExplore.jobProfileTitle)

        eventTracker.pushEvent(
            TrackingEventArgs(
                eventName = jpExplore.jobProfileTitle + "_" + ClientActivationEvents.EVENT_USER_CLICKED,
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



    fun hideKeyboard() {
        search_item?.let {
            it.isFocusableInTouchMode = false
            it.clearFocus()
            val inputMethodManager =
                activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInputFromWindow(
                it.applicationWindowToken,
                InputMethodManager.HIDE_NOT_ALWAYS, 0
            )
        }
    }

    fun showKeyboard() {
        search_item?.let {
            it.isFocusableInTouchMode = true
            it.requestFocus()
            val inputMethodManager =
                activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager!!.toggleSoftInputFromWindow(
                it.applicationWindowToken,
                InputMethodManager.SHOW_FORCED, 0
            )
        }

    }

    override fun onBackPressed(): Boolean {
        if (appBar.isSearchCurrentlyShown) {
            hideSoftKeyboard()
            appBar.hideSearchOption()
            clientActiExploreAdapter.filter.filter("")
            return true
        } else {
            return false
        }

    }


}