package com.gigforce.user_preferences.location

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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.core.di.interfaces.INavHost
import com.gigforce.core.extensions.setDarkStatusBarTheme
import com.gigforce.core.navigation.INavigation
import com.gigforce.user_preferences.PreferredLocationAdapter
import com.gigforce.user_preferences.R
import com.gigforce.user_preferences.SharedPreferenceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.preferred_location_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class PreferredLocationFragment : Fragment() {
    companion object {
        fun newInstance() =
            PreferredLocationFragment()
        const val DAY_TIME = 2;
        const val LOCATION = 3;
        const val TITLE_OTHER = 5;
        const val TITLE_SIGNOUT = 8;
    }

    private lateinit var viewModel: SharedPreferenceViewModel
//    lateinit var recyclerGenericAdapter: RecyclerGenericAdapter<String>
    val arrPrefrancesList : ArrayList<String> = ArrayList<String> ()
    @Inject lateinit var navigation : INavigation
    @Inject lateinit var navHost : INavHost
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.preferred_location_fragment, container, false)
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

//            val navHostFragment: NavHostFragment? =
//                activity?.supportFragmentManager?.findFragmentById(R.id.nav_fragment) as NavHostFragment?
//            var fragmentholder: Fragment? =
//                navHostFragment!!.childFragmentManager.fragments[navHostFragment!!.childFragmentManager.fragments.size - 1]

            if (!isCurrentAddressScreen(navHost.getFragment())) {
                navigation.popBackStack("preferences/preferredLocationFragment")
//                popFragmentFromStack(R.id.preferredLocationFragment)
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
            navigation.navigateTo("preferences/currentAddressEditFragment")
//            navigate(R.id.currentAddressEditFragment)
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
    var preferredLocationaAdapter : PreferredLocationAdapter? = null
    private fun initializeRecyclerView() {
//        recyclerGenericAdapter =
//            RecyclerGenericAdapter<String>(
//                activity?.applicationContext,
//                PFRecyclerViewAdapter.OnViewHolderClick<String> { view, position, item -> },
//                RecyclerGenericAdapter.ItemInterface <String?> { obj, viewHolder, position ->
//                    getTextView(viewHolder,R.id.title).text = obj
//                })!!

        preferredLocationaAdapter = PreferredLocationAdapter()
        preferredLocationaAdapter?.data = arrPrefrancesList

//        recyclerGenericAdapter.setList(arrPrefrancesList)
//        recyclerGenericAdapter.setLayout(R.layout.preferred_location_item)

        prefrences_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        prefrences_rv.hasFixedSize()
        prefrences_rv.itemAnimator = DefaultItemAnimator()
        prefrences_rv.adapter = preferredLocationaAdapter
    }

    private fun setPreferenecesList() {
        arrPrefrancesList.clear()
        arrPrefrancesList.addAll(viewModel.getPreferredLocationList())
        preferredLocationaAdapter?.notifyDataSetChanged()

    }
    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
        viewModel.setPreferenceDataModel(preferenceData)
            setPreferenecesList()
        })
    }

}