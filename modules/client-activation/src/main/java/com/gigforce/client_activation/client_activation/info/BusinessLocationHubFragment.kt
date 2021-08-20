package com.gigforce.client_activation.client_activation.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.client_activation.R
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.NavFragmentsData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_business_location_hub.*
import kotlinx.android.synthetic.main.layout_questionnaire_fragment.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BusinessLocationHubFragment : Fragment(), IOnBackPressedOverride {

    var stateAdapter: ArrayAdapter<String>? = null
    var hubAdapter: ArrayAdapter<String>? = null
    private lateinit var mJobProfileId: String
    private var FROM_CLIENT_ACTIVATON: Boolean = false

    private val viewModel: BLocationHubViewModel by viewModels()

    @Inject
    lateinit var navigation: INavigation

    var stateMap = mutableMapOf<String, Int>()

    //    private lateinit var viewBinding = BusinessLocationHubFragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business_location_hub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initialize()
        observer()
        listener()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)
    }


    var allNavigationList = ArrayList<String>()
    var intentBundle: Bundle? = null

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {

            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
        }

        arguments?.let {

            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)
            it.getStringArrayList(StringConstants.NAVIGATION_STRING_ARRAY.value)?.let { arr ->
                allNavigationList = arr
            }
            intentBundle = it
        }
    }
    var serverHubData : HubServerDM? = null
    private fun observer() {

        viewModel.hubLiveData.observe(viewLifecycleOwner, Observer {
            serverHubData = it
            setHubData(it)
        })
        viewModel._states.observe(viewLifecycleOwner, Observer {
            viewModel.loadHubData(mJobProfileId)
            stateList.clear()
            stateList.addAll(it)
            stateList.forEachIndexed { index, data ->
                stateMap.put(data, index)
            }

            stateAdapter?.notifyDataSetChanged()

        })

        viewModel._hub.observe(viewLifecycleOwner, Observer {
            hubList.clear()
            hubList.addAll(it)
            hubAdapter?.notifyDataSetChanged()

            serverHubData?.let {
                if(it.once == true && !it.hubName.isNullOrBlank() && hubList.contains(it.hubName)){
                    hub.setText(it.hubName,false)
                }
            }

        })

        viewModel.observableAddApplicationSuccess.observe(viewLifecycleOwner, Observer {
            if (it) {
                checkForNextDoc()
            }
        })


    }

    private fun setHubData(it: HubServerDM?) {
        it?.stateName?.let {
            state.setText(it, false)
            viewModel.loadHub(it)
        }

    }

    var stateList = arrayListOf<String>()
    var hubList = arrayListOf<String>()
    private fun initialize() {
        viewModel.loadStates("")
        initState()
        initHub()

    }

    fun initState() {
        stateAdapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                stateList
            )
        }
        state.setAdapter(stateAdapter)
        state.threshold = 1
        state.setOnFocusChangeListener { view1, bool ->
            if (bool) {
                state.showDropDown()
            }
        }
        state.setOnClickListener {
            state.showDropDown()
        }

        state.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 <= stateList.size && state.text.toString().isNotBlank()) {
                    val actualIndex = stateMap.get(state.text.toString().trim())
                    actualIndex?.let {
                        if (it >= 0) {
                            viewModel.loadHub(stateList.get(it))
                        }
                    }

                }
            }
        }
    }

    fun initHub() {
        hubAdapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                hubList
            )
        }
        hub.setAdapter(hubAdapter)
        hub.threshold = 1
        hub.setOnFocusChangeListener { view1, bool ->
            if (bool) {
                hub.showDropDown()
            }
        }
        hub.setOnClickListener {
            hub.showDropDown()
        }
    }

    private fun listener() {
        submit_button.setOnClickListener {

            if (state.text.isNullOrBlank() || !stateList.contains(state.text.toString())) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Please select state")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            if (hub.text.isNullOrBlank() || !hubList.contains(hub.text.toString())) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage("Please select hub")
                    .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                    .show()
                return@setOnClickListener
            }

            viewModel.saveHubLocationData(
                state.text.toString(),
                hub.text.toString(),
                type = "jp_hub_location",
                mJobProfileId = mJobProfileId
            )
        }
    }

    private fun checkForNextDoc() {
        if (allNavigationList.size == 0) {
            activity?.onBackPressed()
        } else {
            var navigationsForBundle = emptyList<String>()
            if (allNavigationList.size > 1) {
                navigationsForBundle =
                    allNavigationList.slice(IntRange(1, allNavigationList.size - 1))
                        .filter { it.length > 0 }
            }
            navigation.popBackStack()
            intentBundle?.putStringArrayList(
                StringConstants.NAVIGATION_STRING_ARRAY.value,
                ArrayList(navigationsForBundle)
            )
            navigation.navigateTo(
                allNavigationList.get(0), intentBundle
            )
//            navigation.navigateTo(
//                allNavigationList.get(0),
//                bundleOf(StringConstants.NAVIGATION_STRING_ARRAY.value to navigationsForBundle,if(FROM_CLIENT_ACTIVATON) StringConstants.FROM_CLIENT_ACTIVATON.value to true else StringConstants.FROM_CLIENT_ACTIVATON.value to false)
//            )

        }
    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            var navFragmentsData = activity as NavFragmentsData
            navFragmentsData.setData(
                bundleOf(
                    com.gigforce.core.StringConstants.BACK_PRESSED.value to true

                )
            )
        }
        return false
    }


}