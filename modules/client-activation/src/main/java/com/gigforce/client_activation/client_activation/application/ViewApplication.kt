package com.gigforce.client_activation.client_activation.application

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.AdapterApplicationClientActivation
import com.gigforce.client_activation.client_activation.ClientActivationNewUserRepo
import com.gigforce.client_activation.client_activation.ClientActivationRepository
import com.gigforce.client_activation.client_activation.adapters.AdapterViewApplication
import com.gigforce.client_activation.client_activation.models.RequiredFeatures
import com.gigforce.common_ui.StringConstants
import com.gigforce.core.utils.NavFragmentsData
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_fragment_client_activation.*
import kotlinx.android.synthetic.main.view_application_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class ViewApplication : Fragment() {

    companion object {
        fun newInstance() = ViewApplication()
    }

    private lateinit var viewModel: ViewApplicationViewModel
    @Inject
    lateinit var navigation: INavigation

    private lateinit var window: Window

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_application_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        changeStatusBar()
        viewModel = ViewModelProvider(this).get(ViewApplicationViewModel::class.java)
        getDataFromIntents(savedInstanceState)
        //observer
        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                updateUI(it)
            }
        })

    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {

    }

    private fun changeStatusBar() {
        window = activity?.window!!
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

// finally change the color
        window.setStatusBarColor(resources.getColor(R.color.status_bar_pink))
    }

    fun updateUI(jpApplication: JpApplication){
        pb_client_activation.gone()
        tv_mark_as_interest_role_details.text = getString(R.string.apply_now)
        run {
            if (jpApplication.status == "") {
                tv_application_status.gone()
            } else {
                tv_application_status.visible()
                tv_application_status.text =
                    if (jpApplication.status == "Interested" || jpApplication.status == "Inprocess") "Pending" else jpApplication.status
                tv_application_status.setCompoundDrawablesWithIntrinsicBounds(
                    if (jpApplication.status == "Interested" || jpApplication.status == "Inprocess" || jpApplication.status == "Submitted") R.drawable.ic_status_pending else if (jpApplication.status == "Activated") R.drawable.ic_applied else R.drawable.ic_application_rejected,
                    0,
                    0,
                    0
                )
                activity?.applicationContext?.let {
                    tv_application_status.setTextColor(
                        ContextCompat.getColor(
                            it,
                            if (jpApplication.status == "Interested" || jpApplication.status == "Inprocess" || jpApplication.status == "Submitted") R.color.pending_color else if (jpApplication.status == "Activated") R.color.activated_color else R.color.rejected_color
                        )
                    )
                }
//                var actionButtonText =
//                    if (jpApplication.status == "Interested") getString(R.string.complete_application) else if (jpApplication.status == "Inprocess") getString(
//                        R.string.complete_activation
//                    ) else if (jpApplication.status == "") getString(R.string.apply_now) else ""
//                if (actionButtonText == "")
//                    tv_mark_as_interest_role_details.gone()
//                else
//                    tv_mark_as_interest_role_details.text = actionButtonText

                //send data to recycler view
                var items = ArrayList<RequiredFeatures>()
                jpApplication.activation.forEach {
                    items.add(RequiredFeatures(it.title, it.type, it.status, it.isDone, it.refresh))
                }
                if (items.size > 0){
                    Log.d("size", ""+ items.size + "items : "+  items.toString())
                    setUpRecyclerView(items)
                }
                else{

                }

            }
        }

        }

    fun setUpRecyclerView(items: ArrayList<RequiredFeatures>){

        var adapter = context?.let { AdapterViewApplication(it) }

        rv_required_details.layoutManager = LinearLayoutManager(requireContext())

        rv_required_details.adapter = adapter
        adapter?.setData(items)

    }

}