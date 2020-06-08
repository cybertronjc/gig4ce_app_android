package com.gigforce.app.modules.preferences.location

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.profile.models.AddressModel
import kotlinx.android.synthetic.main.around_current_address_fragment.*
import kotlinx.android.synthetic.main.around_current_address_fragment.areaview
import kotlinx.android.synthetic.main.around_current_address_fragment.cityview
import kotlinx.android.synthetic.main.around_current_address_fragment.imageView10
import kotlinx.android.synthetic.main.around_current_address_fragment.line1view
import kotlinx.android.synthetic.main.around_current_address_fragment.line2view
import kotlinx.android.synthetic.main.around_current_address_fragment.pincodeview
import kotlinx.android.synthetic.main.around_current_address_fragment.workFromHomeSwitch
import kotlinx.android.synthetic.main.around_current_address_fragment.stateview
import kotlinx.android.synthetic.main.earning_fragment.*

class AroundCurrentAddressFragment : BaseFragment() {

    companion object {
        fun newInstance() = AroundCurrentAddressFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private var preferredDistanceActive:Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.around_current_address_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeAll()
        listener()
    }

    private fun listener() {
        arround_current_add_seekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress:Int, fromUser:Boolean) {
                val value = (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                seekbardependent.text =  progress.toString()+" Km"
                seekbardependent.setX(seekBar.getX() + value + seekBar.getThumbOffset() / 2)
                //textView.setY(100); just added a value set this properly using screen with height aspect ratio , if you do not set it by default it will be there below seek bar
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        imageView10.setOnClickListener{
            activity?.onBackPressed()
        }
    }

    private fun initializeAll() {
        workFromHomeSwitch.isChecked = viewModel.getCurrentAddress()?.preferredDistanceActive!!
        preferredDistanceActive = viewModel.getCurrentAddress()?.preferredDistanceActive!!
        arround_current_add_seekbar.progress = viewModel.getCurrentAddress()?.preferred_distance!!
//        arround_current_add_seekbar.setOtherView(seekbardependent,false,"Km")
        populateAddress(viewModel.getCurrentAddress()!!)
    }
    private fun populateAddress(address: AddressModel){
        line1view.text = address.firstLine
        line2view.text = address.secondLine
        areaview.text = address.area
        cityview.text = address.city
        stateview.text = address.state
        pincodeview.text = address.pincode

    }
    override fun onBackPressed(): Boolean {
        if(arround_current_add_seekbar.progress==0){
            showToast("Preferred distance is 0 so you are not able to enable it.")
            viewModel.setCurrentAddressPrferredDistanceData(arround_current_add_seekbar.progress,false)
        }
        else {
            preferredDistanceActive = workFromHomeSwitch.isChecked
            viewModel.setCurrentAddressPrferredDistanceData(
                arround_current_add_seekbar.progress,
                preferredDistanceActive
            )
        }
        return super.onBackPressed()
    }
}