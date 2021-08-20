package com.gigforce.client_activation.client_activation.info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.client_activation.R
import kotlinx.android.synthetic.main.fragment_business_location_hub.*

class BusinessLocationHubFragment : Fragment() {

    var stateAdapter: ArrayAdapter<String>? = null
    var hubAdapter : ArrayAdapter<String>? = null
    private val viewModel : BLocationHubViewModel by viewModels()
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
        initialize()
        observer()
        listener()
    }

    private fun observer() {
        viewModel._states.observe(viewLifecycleOwner, Observer {
            stateList.clear()
            stateList.addAll(it)
            stateList.forEachIndexed{ index,data->
                stateMap.put(data,index)
            }

            stateAdapter?.notifyDataSetChanged()

        })

        viewModel._hub.observe(viewLifecycleOwner, Observer {
            hubList.clear()
            hubList.addAll(it)
            hubAdapter?.notifyDataSetChanged()

        })
    }

    var stateList = arrayListOf<String>()
    var hubList = arrayListOf<String>()
    private fun initialize() {
        viewModel.loadStates("")
        initState()
        initHub()

    }

    fun initState(){
        stateAdapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                stateList
            )
        }
        state.setAdapter(stateAdapter)
        state.setOnFocusChangeListener{ view1, bool->
            if(bool){
                state.showDropDown()
            }
        }
        state.setOnClickListener{
            state.showDropDown()
        }

        state.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(p2 <= stateList.size && state.text.toString().isNotBlank() ){
                    val actualIndex = stateMap.get(state.text.toString().trim())
                    actualIndex?.let {
                        if(it>=0){
                            viewModel.loadHub(stateList.get(it))
                        }
                    }

                }
            }
        }
    }
    fun initHub(){
        hubAdapter = context?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_dropdown_item,
                hubList
            )
        }
        hub.setAdapter(hubAdapter)
        hub.setOnFocusChangeListener{ view1, bool->
            if(bool){
                hub.showDropDown()
            }
        }
        hub.setOnClickListener{
            hub.showDropDown()
        }
    }
    private fun listener() {

    }

}