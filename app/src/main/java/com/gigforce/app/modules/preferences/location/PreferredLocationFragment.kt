package com.gigforce.app.modules.preferences.location

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.modules.preferences.PreferencesScreenItem
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.utils.setDarkStatusBarTheme
import com.google.firebase.auth.FirebaseAuth
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

    private fun listener() {
        imageView10.setOnClickListener(View.OnClickListener { activity?.onBackPressed() })
//        imageView9.setOnClickListener(View.OnClickListener { navigate(R.id.profileFragment) })
    }

    private fun initializeViews() {
        initializeRecyclerView()
        setPreferenecesList()
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