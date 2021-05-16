package com.gigforce.app.modules.preferences.location

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.core.extensions.setDarkStatusBarTheme
import kotlinx.android.synthetic.main.preferred_location_fragment.*


class PreferredLocationFragment : BaseFragment() {
    companion object {
        fun newInstance() =
            PreferredLocationFragment()
        const val DAY_TIME = 2;
        const val LOCATION = 3;
        const val TITLE_OTHER = 5;
        const val TITLE_SIGNOUT = 8;
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<String>
    val arrPrefrancesList : ArrayList<String> = ArrayList<String> ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.preferred_location_fragment, inflater, container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.setDarkStatusBarTheme(false)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
            initializeViews()
            listener()
            observePreferenceData()
    }

    override fun onStart() {
        super.onStart()
        if(viewModel.getCurrentAddress()!!.isEmpty()) {
            val navHostFragment: NavHostFragment? =
                activity?.supportFragmentManager?.findFragmentById(R.id.nav_fragment) as NavHostFragment?
            var fragmentholder: Fragment? =
                navHostFragment!!.childFragmentManager.fragments[navHostFragment!!.childFragmentManager.fragments.size - 1]
            if (!isCurrentAddressScreen(fragmentholder)) {
                popFragmentFromStack(R.id.preferredLocationFragment)
            }
        }
    }
    private fun isCurrentAddressScreen(fragmentholder: Fragment?): Boolean {
        try {
            var isCurrentAddressScreen = (fragmentholder as CurrentAddressEditFragment)
            if (isCurrentAddressScreen != null) return true
        } catch (e: Exception) {
        }
        return false
    }
    private fun listener() {
        back_arrow_iv.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
        editCurrentLocation.setOnClickListener{
        showEditLoactionAlert()
        }
//        imageView9.setOnClickListener(View.OnClickListener { navigate(R.id.profileFragment) })
    }

    private fun showEditLoactionAlert() {
        var customialog:Dialog? = activity?.let { Dialog(it) }
        customialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customialog?.setContentView(R.layout.custom_alert_3)
        val yesBtn = customialog?.findViewById(R.id.okay) as TextView
        yesBtn.setOnClickListener (View.OnClickListener {
            navigate(R.id.currentAddressEditFragment)
            customialog.dismiss()
        })
        customialog?.show()
    }

    private fun initializeViews() {
        initializeRecyclerView()
        initializeCity()
        setPreferenecesList()
    }

    private fun initializeCity() {
        city_radio_button.text = viewModel.getCurrentAddress()?.city
    }

    private fun initializeRecyclerView() {
        recyclerGenericAdapter =
            RecyclerGenericAdapter<String>(
                activity?.applicationContext,
                PFRecyclerViewAdapter.OnViewHolderClick<String> { view, position, item -> },
                RecyclerGenericAdapter.ItemInterface <String?> { obj, viewHolder, position ->
                    getTextView(viewHolder,R.id.title).text = obj
                })!!
        recyclerGenericAdapter.setList(arrPrefrancesList)
        recyclerGenericAdapter.setLayout(R.layout.preferred_location_item)

        prefrences_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        prefrences_rv.hasFixedSize()
        prefrences_rv.itemAnimator = DefaultItemAnimator()
        prefrences_rv.adapter = recyclerGenericAdapter
    }

    private fun setPreferenecesList() {
        arrPrefrancesList.clear()
        arrPrefrancesList.addAll(viewModel.getPreferredLocationList())
        recyclerGenericAdapter.notifyDataSetChanged()

    }
    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
        viewModel.setPreferenceDataModel(preferenceData)
            setPreferenecesList()
        })
    }

}